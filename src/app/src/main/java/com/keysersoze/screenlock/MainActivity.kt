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

    private var languageCode by mutableStateOf(AppLocaleManager.SYSTEM)
    private var overlayEnabled by mutableStateOf(false)
    private var showOverlayHelp by mutableStateOf(false)
    private var tileMessage by mutableStateOf<String?>(null)
    private var pendingTilePrompt = false

    private var shadeProtectionEnabled by mutableStateOf(false)
    private var shadeAccessibilityEnabled by mutableStateOf(false)
    private var showShadeProtectionHelp by mutableStateOf(false)
    private var pendingShadeProtectionEnable = false
    private var shadeProtectionMessage by mutableStateOf<String?>(null)

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
                    getString(R.string.msg_update_downloaded)
            } else {
                updateMessage = getString(R.string.msg_update_download_failed)
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLocaleManager.syncFrameworkLocale(this)
        languageCode = AppLocaleManager.selectedLanguage(this)
        enableEdgeToEdge()

        UpdateManager.reconcileInstalledVersion(this)
        overlayEnabled = LockPreferences.canDrawOverlays(this)
        shadeAccessibilityEnabled =
            LockPreferences.isShadeProtectionAccessibilityEnabled(this)
        shadeProtectionEnabled =
            LockPreferences.shadeProtection(this) && shadeAccessibilityEnabled
        if (!shadeAccessibilityEnabled) {
            LockPreferences.setShadeProtection(this, false)
        }

        autoUpdates = LockPreferences.autoUpdates(this)
        updateDownloading = UpdateManager.isInProgress(this)

        setContent {
            ScreenLockTheme {
                ScreenLockApp(
                    overlayEnabled = overlayEnabled,
                    showOverlayHelp = showOverlayHelp,
                    showShadeProtectionHelp = showShadeProtectionHelp,
                    activationDelaySeconds = LockPreferences.activationDelaySeconds(this),
                    unlockSeconds = LockPreferences.unlockSeconds(this),
                    unlockRadiusDp = LockPreferences.unlockRadiusDp(this),
                    unlockPosition = LockPreferences.unlockPosition(this),
                    dimPercent = LockPreferences.dimPercent(this),
                    showHint = LockPreferences.showHint(this),
                    haptics = LockPreferences.haptics(this),
                    shadeProtectionEnabled = shadeProtectionEnabled,
                    shadeAccessibilityEnabled = shadeAccessibilityEnabled,
                    shadeProtectionMessage = shadeProtectionMessage,
                    autoUpdates = autoUpdates,
                    currentVersion = BuildConfig.VERSION_NAME,
                    updateInfo = updateInfo,
                    updateChecking = updateChecking,
                    updateDownloading = updateDownloading,
                    updateReady = UpdateManager.isComplete(this),
                    updateMessage = updateMessage,
                    tileMessage = tileMessage,
                    selectedLanguage = languageCode,
                    languageOptions = AppLocaleManager.supportedLanguages,
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
                    onDismissShadeProtectionHelp = {
                        showShadeProtectionHelp = false
                        pendingShadeProtectionEnable = false
                    },
                    onOpenShadeAccessibility = {
                        showShadeProtectionHelp = false
                        pendingShadeProtectionEnable = true
                        openAccessibilitySettings()
                    },
                    onOpenAppInfo = {
                        showShadeProtectionHelp = false
                        pendingShadeProtectionEnable = false
                        openAppInfo()
                    },
                    onAddQuickTile = ::requestQuickTile,
                    onTestLock = ::startTestLock,
                    onActivationDelayChanged = {
                        LockPreferences.setActivationDelaySeconds(this, it)
                    },
                    onUnlockSecondsChanged = { LockPreferences.setUnlockSeconds(this, it) },
                    onUnlockRadiusChanged = { LockPreferences.setUnlockRadiusDp(this, it) },
                    onUnlockPositionChanged = { LockPreferences.setUnlockPosition(this, it) },
                    onDimPercentChanged = { LockPreferences.setDimPercent(this, it) },
                    onShowHintChanged = { LockPreferences.setShowHint(this, it) },
                    onHapticsChanged = { LockPreferences.setHaptics(this, it) },
                    onShadeProtectionChanged = ::setShadeProtection,
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
                    onLanguageChanged = { code ->
                        if (code != languageCode) {
                            AppLocaleManager.setLanguage(this, code)
                            languageCode = code
                            recreate()
                        }
                    },
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

        val wasOverlayEnabled = overlayEnabled
        overlayEnabled = LockPreferences.canDrawOverlays(this)

        if (!wasOverlayEnabled && overlayEnabled) {
            tileMessage =
                getString(R.string.msg_overlay_permission_ready)
            pendingTilePrompt = true
        }

        val accessibilityNow =
            LockPreferences.isShadeProtectionAccessibilityEnabled(this)
        shadeAccessibilityEnabled = accessibilityNow

        if (pendingShadeProtectionEnable && accessibilityNow) {
            pendingShadeProtectionEnable = false
            shadeProtectionEnabled = true
            LockPreferences.setShadeProtection(this, true)
            shadeProtectionMessage =
                getString(R.string.msg_shade_ready)
        } else if (!accessibilityNow && shadeProtectionEnabled) {
            shadeProtectionEnabled = false
            LockPreferences.setShadeProtection(this, false)
            shadeProtectionMessage =
                getString(R.string.msg_shade_disabled_accessibility)
        }

        updateDownloading = UpdateManager.isInProgress(this)

        if (pendingInstallAfterPermission && UpdateManager.canInstallPackages(this)) {
            pendingInstallAfterPermission = false
            UpdateManager.installDownloaded(this)
        } else if (UpdateManager.isComplete(this)) {
            updateMessage =
                getString(R.string.msg_update_ready)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && pendingTilePrompt) {
            pendingTilePrompt = false
            requestQuickTile()
        }
    }

    private fun setShadeProtection(enabled: Boolean) {
        if (!enabled) {
            pendingShadeProtectionEnable = false
            shadeProtectionEnabled = false
            LockPreferences.setShadeProtection(this, false)
            shadeProtectionMessage = getString(R.string.msg_shade_disabled)
            return
        }

        shadeAccessibilityEnabled =
            LockPreferences.isShadeProtectionAccessibilityEnabled(this)

        if (shadeAccessibilityEnabled) {
            shadeProtectionEnabled = true
            LockPreferences.setShadeProtection(this, true)
            shadeProtectionMessage =
                getString(R.string.msg_shade_ready_active_only)
        } else {
            shadeProtectionEnabled = false
            pendingShadeProtectionEnable = true
            showShadeProtectionHelp = true
        }
    }

    private fun startTestLock() {
        ScreenLockOverlayService.start(
            this,
            ScreenLockOverlayService.ACTION_LOCK,
        )
    }

    private fun openAccessibilitySettings() {
        runCatching {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }.onFailure {
            shadeProtectionMessage =
                getString(R.string.msg_accessibility_fallback)
        }
    }

    private fun openAppInfo() {
        runCatching {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName"),
                ),
            )
        }.onFailure {
            shadeProtectionMessage =
                getString(R.string.msg_app_info_fallback)
        }
    }

    private fun checkForUpdates(autoDownload: Boolean) {
        if (updateChecking) return
        updateChecking = true
        updateMessage = if (autoDownload) null else getString(R.string.msg_checking_updates)

        lifecycleScope.launch {
            val result = runCatching { UpdateManager.checkLatest() }
            updateChecking = false

            result.onSuccess { info ->
                updateInfo = info
                if (info == null) {
                    if (!autoDownload) {
                        updateMessage = getString(R.string.msg_latest_version)
                    }
                } else if (
                    autoDownload &&
                    !UpdateManager.isComplete(this@MainActivity) &&
                    !UpdateManager.isInProgress(this@MainActivity)
                ) {
                    downloadUpdate(info)
                } else if (UpdateManager.isInProgress(this@MainActivity)) {
                    updateDownloading = true
                    updateMessage = getString(R.string.msg_download_version_progress, info.version)
                } else {
                    updateMessage = getString(R.string.msg_new_version_available, info.version)
                }
            }.onFailure {
                updateMessage =
                    if (autoDownload) null else getString(R.string.msg_github_check_failed)
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
            updateMessage = getString(R.string.msg_download_version_progress, info.version)
        }.onFailure {
            updateDownloading = false
            updateMessage = getString(R.string.msg_download_failed_retry)
        }
    }

    private fun installDownloadedUpdate() {
        if (!UpdateManager.isComplete(this)) {
            updateMessage = getString(R.string.msg_update_not_ready)
            return
        }

        if (!UpdateManager.canInstallPackages(this)) {
            pendingInstallAfterPermission = true
            updateMessage =
                getString(R.string.msg_allow_install_updates)
            UpdateManager.openInstallPermission(this)
            return
        }

        if (!UpdateManager.installDownloaded(this)) {
            updateMessage = getString(R.string.msg_installer_failed)
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
                    getString(R.string.msg_overlay_settings_fallback)
            }
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT < 33) {
            tileMessage =
                getString(R.string.msg_tile_manual_add)
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
                    getString(R.string.msg_tile_added)
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                    getString(R.string.msg_tile_already_added)
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED ->
                    getString(R.string.msg_tile_not_added)
                StatusBarManager.TILE_ADD_REQUEST_ERROR_APP_NOT_IN_FOREGROUND ->
                    getString(R.string.msg_tile_foreground)
                else ->
                    getString(R.string.msg_tile_popup_fallback)
            }
        }
    }
}
