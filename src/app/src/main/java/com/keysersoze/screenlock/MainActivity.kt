package com.keysersoze.screenlock

import android.app.DownloadManager
import android.app.StatusBarManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.keysersoze.screenlock.ui.ScreenLockApp
import com.keysersoze.screenlock.ui.theme.ScreenLockTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var overlayEnabled by mutableStateOf(false)
    private var showOverlayHelp by mutableStateOf(false)
    private var tileMessage by mutableStateOf<String?>(null)
    private var pendingTilePrompt = false

    private var immersiveShield by mutableStateOf(false)
    private var autoUpdates by mutableStateOf(true)
    private var updateInfo by mutableStateOf<UpdateInfo?>(null)
    private var updateChecking by mutableStateOf(false)
    private var updateDownloading by mutableStateOf(false)
    private var updateMessage by mutableStateOf<String?>(null)
    private var pendingInstallAfterPermission = false
    private var downloadReceiverRegistered = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (completedId != UpdateManager.currentDownloadId(this@MainActivity)) return

            updateDownloading = false
            if (UpdateManager.isComplete(this@MainActivity, completedId)) {
                updateMessage =
                    "Aggiornamento scaricato. Android richiederà la conferma prima di installarlo."
            } else {
                updateMessage = "Download aggiornamento non riuscito. Puoi riprovare."
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        UpdateManager.reconcileInstalledVersion(this)
        overlayEnabled = LockPreferences.canDrawOverlays(this)
        immersiveShield = LockPreferences.immersiveShield(this)
        autoUpdates = LockPreferences.autoUpdates(this)
        updateDownloading = UpdateManager.isInProgress(this)

        setContent {
            ScreenLockTheme {
                ScreenLockApp(
                    overlayEnabled = overlayEnabled,
                    showOverlayHelp = showOverlayHelp,
                    activationDelaySeconds = LockPreferences.activationDelaySeconds(this),
                    unlockSeconds = LockPreferences.unlockSeconds(this),
                    dimPercent = LockPreferences.dimPercent(this),
                    showHint = LockPreferences.showHint(this),
                    haptics = LockPreferences.haptics(this),
                    immersiveShield = immersiveShield,
                    autoUpdates = autoUpdates,
                    currentVersion = BuildConfig.VERSION_NAME,
                    updateInfo = updateInfo,
                    updateChecking = updateChecking,
                    updateDownloading = updateDownloading,
                    updateReady = UpdateManager.isComplete(this),
                    updateMessage = updateMessage,
                    tileMessage = tileMessage,
                    onEnableOverlay = {
                        showOverlayHelp = true
                    },
                    onDismissOverlayHelp = {
                        showOverlayHelp = false
                    },
                    onOpenOverlaySettings = {
                        showOverlayHelp = false
                        openOverlaySettings()
                    },
                    onAddQuickTile = ::requestQuickTile,
                    onTestLock = ::startTestLock,
                    onActivationDelayChanged = {
                        LockPreferences.setActivationDelaySeconds(this, it)
                    },
                    onUnlockSecondsChanged = { LockPreferences.setUnlockSeconds(this, it) },
                    onDimPercentChanged = { LockPreferences.setDimPercent(this, it) },
                    onShowHintChanged = { LockPreferences.setShowHint(this, it) },
                    onHapticsChanged = { LockPreferences.setHaptics(this, it) },
                    onImmersiveShieldChanged = {
                        immersiveShield = it
                        LockPreferences.setImmersiveShield(this, it)
                    },
                    onAutoUpdatesChanged = {
                        autoUpdates = it
                        LockPreferences.setAutoUpdates(this, it)
                        if (it) checkForUpdates(autoDownload = true)
                    },
                    onCheckUpdates = { checkForUpdates(autoDownload = false) },
                    onDownloadUpdate = {
                        updateInfo?.let(::downloadUpdate)
                    },
                    onInstallUpdate = ::installDownloadedUpdate,
                )
            }
        }

        if (autoUpdates) checkForUpdates(autoDownload = true)
    }

    override fun onStart() {
        super.onStart()
        if (!downloadReceiverRegistered) {
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            if (Build.VERSION.SDK_INT >= 33) {
                registerReceiver(downloadReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                registerReceiver(downloadReceiver, filter)
            }
            downloadReceiverRegistered = true
        }
    }

    override fun onStop() {
        if (downloadReceiverRegistered) {
            runCatching { unregisterReceiver(downloadReceiver) }
            downloadReceiverRegistered = false
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        val wasEnabled = overlayEnabled
        val enabledNow = LockPreferences.canDrawOverlays(this)
        overlayEnabled = enabledNow

        if (!wasEnabled && enabledNow) {
            tileMessage =
                "Perfetto: il permesso è attivo. Ora aggiungiamo Screen Lock alla tendina."
            pendingTilePrompt = true
        }

        updateDownloading = UpdateManager.isInProgress(this)

        if (pendingInstallAfterPermission && UpdateManager.canInstallPackages(this)) {
            pendingInstallAfterPermission = false
            UpdateManager.installDownloaded(this)
        } else if (UpdateManager.isComplete(this)) {
            updateMessage =
                "Aggiornamento scaricato e pronto. Tocca “Installa aggiornamento”."
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && pendingTilePrompt) {
            pendingTilePrompt = false
            requestQuickTile()
        }
    }

    private fun startTestLock() {
        if (immersiveShield) {
            startActivity(
                TouchShieldActivity.intent(this, TouchShieldActivity.ACTION_LOCK),
            )
        } else {
            ScreenLockOverlayService.start(
                this,
                ScreenLockOverlayService.ACTION_LOCK,
            )
        }
    }

    private fun checkForUpdates(autoDownload: Boolean) {
        if (updateChecking) return
        updateChecking = true
        updateMessage = if (autoDownload) null else "Controllo aggiornamenti…"

        lifecycleScope.launch {
            val result = runCatching { UpdateManager.checkLatest() }
            updateChecking = false

            result.onSuccess { info ->
                updateInfo = info
                if (info == null) {
                    if (!autoDownload) {
                        updateMessage = "Hai già l’ultima versione."
                    }
                } else if (
                    autoDownload &&
                    !UpdateManager.isComplete(this@MainActivity) &&
                    !UpdateManager.isInProgress(this@MainActivity)
                ) {
                    downloadUpdate(info)
                } else if (UpdateManager.isInProgress(this@MainActivity)) {
                    updateDownloading = true
                    updateMessage = "Download di Screen Lock ${info.version} in corso…"
                } else {
                    updateMessage = "Nuova versione ${info.version} disponibile."
                }
            }.onFailure {
                updateMessage =
                    if (autoDownload) null else "Non riesco a controllare GitHub in questo momento."
            }
        }
    }

    private fun downloadUpdate(info: UpdateInfo) {
        if (
            updateDownloading ||
            UpdateManager.isInProgress(this) ||
            UpdateManager.isComplete(this)
        ) return
        runCatching {
            UpdateManager.enqueue(this, info)
        }.onSuccess {
            updateDownloading = true
            updateMessage = "Download di Screen Lock ${info.version} in corso…"
        }.onFailure {
            updateDownloading = false
            updateMessage = "Download non riuscito. Puoi riprovare."
        }
    }

    private fun installDownloadedUpdate() {
        if (!UpdateManager.isComplete(this)) {
            updateMessage = "L’aggiornamento non è ancora pronto."
            return
        }

        if (!UpdateManager.canInstallPackages(this)) {
            pendingInstallAfterPermission = true
            updateMessage =
                "Consenti a Screen Lock di installare aggiornamenti, poi tornerai qui."
            UpdateManager.openInstallPermission(this)
            return
        }

        if (!UpdateManager.installDownloaded(this)) {
            updateMessage = "Non riesco ad aprire l’installer Android."
        }
    }

    private fun openOverlaySettings() {
        val appSpecific = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)

        runCatching { startActivity(appSpecific) }
            .recoverCatching { startActivity(fallback) }
            .onFailure {
                tileMessage =
                    "Apri Impostazioni e cerca “Mostra sopra altre app”, poi abilita Screen Lock."
            }
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT < 33) {
            tileMessage =
                "Apri la tendina, tocca Modifica e trascina “Screen Lock” tra i pulsanti attivi."
            return
        }

        val manager = getSystemService(StatusBarManager::class.java)
        manager.requestAddTileService(
            ComponentName(this, ScreenLockTileService::class.java),
            getString(R.string.quick_tile_label),
            Icon.createWithResource(this, R.drawable.ic_lock),
            mainExecutor,
        ) { result ->
            tileMessage = when (result) {
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ->
                    "Fatto: Screen Lock è nella tendina dei Comandi rapidi."
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                    "Screen Lock è già nella tendina dei Comandi rapidi."
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED ->
                    "Non è stato aggiunto. Puoi riprovare dal pulsante qui sotto."
                StatusBarManager.TILE_ADD_REQUEST_ERROR_APP_NOT_IN_FOREGROUND ->
                    "Riprova: Android richiede che Screen Lock sia visibile in primo piano."
                else ->
                    "Se non compare il popup, apri la tendina → Modifica e aggiungi Screen Lock manualmente."
            }
        }
    }
}
