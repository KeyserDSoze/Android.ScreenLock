package com.keysersoze.screenlock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keysersoze.screenlock.R
import com.keysersoze.screenlock.UpdateInfo

@Composable
fun ScreenLockApp(
    overlayEnabled: Boolean,
    showOverlayHelp: Boolean,
    showShadeProtectionHelp: Boolean,
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    dimPercent: Int,
    showHint: Boolean,
    haptics: Boolean,
    shadeProtectionEnabled: Boolean,
    shadeAccessibilityEnabled: Boolean,
    shadeProtectionMessage: String?,
    autoUpdates: Boolean,
    currentVersion: String,
    updateInfo: UpdateInfo?,
    updateChecking: Boolean,
    updateDownloading: Boolean,
    updateReady: Boolean,
    updateMessage: String?,
    tileMessage: String?,
    onEnableOverlay: () -> Unit,
    onDismissOverlayHelp: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onDismissShadeProtectionHelp: () -> Unit,
    onOpenShadeAccessibility: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onAddQuickTile: () -> Unit,
    onTestLock: () -> Unit,
    onActivationDelayChanged: (Int) -> Unit,
    onUnlockSecondsChanged: (Int) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onShadeProtectionChanged: (Boolean) -> Unit,
    onAutoUpdatesChanged: (Boolean) -> Unit,
    onCheckUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    if (showOverlayHelp) {
        OverlayHelpDialog(
            onDismiss = onDismissOverlayHelp,
            onContinue = onOpenOverlaySettings,
        )
    }

    if (showShadeProtectionHelp) {
        ShadeProtectionHelpDialog(
            onDismiss = onDismissShadeProtectionHelp,
            onOpenAccessibility = onOpenShadeAccessibility,
            onOpenAppInfo = onOpenAppInfo,
        )
    }

    val background = Brush.verticalGradient(
        0f to Color(0xFF111C18),
        0.34f to Color(0xFF0B1114),
        1f to Color(0xFF07090D),
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = scaffoldPadding.calculateTopPadding() + 28.dp,
                    bottom = scaffoldPadding.calculateBottomPadding() + 36.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Hero(overlayEnabled) }
                item {
                    SetupCard(
                        enabled = overlayEnabled,
                        tileMessage = tileMessage,
                        onEnableOverlay = onEnableOverlay,
                        onAddQuickTile = onAddQuickTile,
                        onTestLock = onTestLock,
                    )
                }
                item {
                    UpdateCard(
                        currentVersion = currentVersion,
                        updateInfo = updateInfo,
                        checking = updateChecking,
                        downloading = updateDownloading,
                        ready = updateReady,
                        autoUpdates = autoUpdates,
                        message = updateMessage,
                        onAutoUpdatesChanged = onAutoUpdatesChanged,
                        onCheck = onCheckUpdates,
                        onDownload = onDownloadUpdate,
                        onInstall = onInstallUpdate,
                    )
                }
                item {
                    SettingsCard(
                        activationDelaySeconds = activationDelaySeconds,
                        unlockSeconds = unlockSeconds,
                        dimPercent = dimPercent,
                        showHint = showHint,
                        haptics = haptics,
                        shadeProtectionEnabled = shadeProtectionEnabled,
                        shadeAccessibilityEnabled = shadeAccessibilityEnabled,
                        shadeProtectionMessage = shadeProtectionMessage,
                        onActivationDelayChanged = onActivationDelayChanged,
                        onUnlockSecondsChanged = onUnlockSecondsChanged,
                        onDimPercentChanged = onDimPercentChanged,
                        onShowHintChanged = onShowHintChanged,
                        onHapticsChanged = onHapticsChanged,
                        onShadeProtectionChanged = onShadeProtectionChanged,
                    )
                }
                item { PrivacyCard() }
                item {
                    Text(
                        text = "Screen Lock · semplice, locale, senza account",
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun Hero(enabled: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.screen_lock_brand_icon),
                contentDescription = null,
                modifier = Modifier.size(76.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Screen Lock",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2f).sp,
                )
                Text(
                    text = "Blocca i tocchi, non la chiamata.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.screen_lock_home_hero),
            contentDescription = "Illustrazione di Screen Lock durante una videochiamata",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(26.dp)),
            contentScale = ContentScale.Crop,
        )

        Text(
            text = "Perfetto quando il telefono passa in mani piccole: Telegram resta visibile e attivo, mentre i tocchi sul contenuto vengono assorbiti.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
        )

        StatusPill(enabled)
    }
}

@Composable
private fun StatusPill(enabled: Boolean) {
    Row(
        modifier = Modifier
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(
                    if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    CircleShape,
                ),
        )
        Text(
            text = if (enabled) "Permesso overlay pronto" else "Passaggio 1 da completare",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetupCard(
    enabled: Boolean,
    tileMessage: String?,
    onEnableOverlay: () -> Unit,
    onAddQuickTile: () -> Unit,
    onTestLock: () -> Unit,
) {
    AppCard {
        Text(
            text = "Configurazione guidata",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (enabled) {
                "Passaggio 2 di 2 · Aggiungi il pulsante Screen Lock alla tendina dei Comandi rapidi. Su Android 13 o successivi comparirà direttamente la richiesta di sistema."
            } else {
                "Passaggio 1 di 2 · Consenti a Screen Lock di comparire sopra le altre app. È il solo permesso speciale necessario per intercettare i tocchi."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(2.dp))

        if (!enabled) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEnableOverlay,
            ) {
                Text("1 · Consenti sopra le altre app")
            }
            Text(
                text = "Non serve più abilitare Screen Lock in Accessibilità: così evitiamo anche il blocco “Controlled by Restricted Setting” degli APK installati da browser.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddQuickTile,
            ) {
                Text("2 · Aggiungi pulsante alla tendina")
            }
            Text(
                text = "Poi, durante una chiamata, abbassa la tendina e tocca Screen Lock per bloccare i tocchi.",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTestLock,
            ) {
                Text("Prova il blocco adesso")
            }
        }

        AnimatedVisibility(visible = tileMessage != null) {
            Text(
                text = tileMessage.orEmpty(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun OverlayHelpDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Consenti l’overlay di Screen Lock",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Si aprirà la schermata Android per le app che possono comparire sopra le altre app. Il nome della voce può cambiare leggermente in base al telefono.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "1. Se Android mostra un elenco, scegli “Screen Lock”.\n\n2. Attiva “Consenti visualizzazione sopra altre app”, “Mostra sopra altre app” o la voce equivalente.\n\n3. Torna a Screen Lock.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Android richiede che sia tu ad attivare questo permesso. Screen Lock lo usa solo per mettere una superficie touch sopra la chiamata; non legge ciò che c’è sullo schermo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Apri impostazione Android")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Non ora")
            }
        },
    )
}

@Composable
private fun ShadeProtectionHelpDialog(
    onDismiss: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Protezione tendina avanzata",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Questa protezione è opzionale. Mentre Screen Lock è attivo, usa un servizio di Accessibilità limitato alla UI di sistema per chiedere ad Android di richiudere la tendina notifiche appena viene aperta.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Il servizio non legge il contenuto dello schermo e non analizza Telegram. Android lo mostra comunque nella sezione Accessibilità perché solo da lì è disponibile l’azione di chiusura della tendina.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "Nella schermata Android cerca “Screen Lock · Protezione tendina”, aprila e attiva l’interruttore. Poi torna qui.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Se la voce è grigia e compare “Controlled by Restricted Setting”, Android sta applicando la protezione per app installate fuori dallo store. In Info app cerca “Consenti impostazioni con limitazioni”. Su alcune ROM questa opzione può non essere disponibile.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenAccessibility) {
                Text("Apri Accessibilità")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onOpenAppInfo) {
                    Text("Info app")
                }
                TextButton(onClick = onDismiss) {
                    Text("Non ora")
                }
            }
        },
    )
}

@Composable
private fun UpdateCard(
    currentVersion: String,
    updateInfo: UpdateInfo?,
    checking: Boolean,
    downloading: Boolean,
    ready: Boolean,
    autoUpdates: Boolean,
    message: String?,
    onAutoUpdatesChanged: (Boolean) -> Unit,
    onCheck: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
) {
    var automatic by remember(autoUpdates) { mutableStateOf(autoUpdates) }

    AppCard {
        Text(
            text = "Aggiornamenti",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Versione installata: $currentVersion",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SwitchRow(
            title = "Aggiornamenti automatici",
            subtitle = "Controlla GitHub all’avvio e scarica automaticamente una release più recente. Android chiederà comunque conferma prima dell’installazione.",
            checked = automatic,
            onCheckedChange = {
                automatic = it
                onAutoUpdatesChanged(it)
            },
        )

        when {
            ready -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onInstall,
                ) {
                    Text("Installa aggiornamento")
                }
            }

            downloading -> {
                Text(
                    text = "Download in corso…",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            updateInfo != null -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDownload,
                ) {
                    Text("Scarica Screen Lock ${updateInfo.version}")
                }
            }

            else -> {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !checking,
                    onClick = onCheck,
                ) {
                    Text(if (checking) "Controllo…" else "Controlla aggiornamenti")
                }
            }
        }

        AnimatedVisibility(visible = message != null) {
            Text(
                text = message.orEmpty(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Text(
            text = "Il controllo usa solo l’API pubblica delle release GitHub. Nessun account, analytics o tracking.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingsCard(
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    dimPercent: Int,
    showHint: Boolean,
    haptics: Boolean,
    shadeProtectionEnabled: Boolean,
    shadeAccessibilityEnabled: Boolean,
    shadeProtectionMessage: String?,
    onActivationDelayChanged: (Int) -> Unit,
    onUnlockSecondsChanged: (Int) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onShadeProtectionChanged: (Boolean) -> Unit,
) {
    var delay by remember(activationDelaySeconds) { mutableIntStateOf(activationDelaySeconds) }
    var seconds by remember(unlockSeconds) { mutableIntStateOf(unlockSeconds) }
    var dim by remember(dimPercent) { mutableFloatStateOf(dimPercent.toFloat()) }
    var hint by remember(showHint) { mutableStateOf(showHint) }
    var vibration by remember(haptics) { mutableStateOf(haptics) }

    AppCard {
        Text(
            text = "Comportamento",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Dalla tile puoi lasciare qualche secondo per richiudere la tendina e sistemare il telefono prima che i tocchi vengano bloccati.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SettingLabel(
            title = "Ritardo di attivazione",
            value = if (delay == 0) "Subito" else "$delay secondi",
        )
        Slider(
            value = delay.toFloat(),
            onValueChange = { delay = it.toInt().coerceIn(0, 5) },
            onValueChangeFinished = { onActivationDelayChanged(delay) },
            valueRange = 0f..5f,
            steps = 4,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        Text(
            text = "Sblocco",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "La pressione deve iniziare al centro e restare quasi ferma. Un secondo dito, uno spostamento eccessivo o il rilascio annullano il conteggio.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SettingLabel(
            title = "Pressione prolungata",
            value = "$seconds secondi",
        )
        Slider(
            value = seconds.toFloat(),
            onValueChange = { seconds = it.toInt().coerceIn(3, 12) },
            onValueChangeFinished = { onUnlockSecondsChanged(seconds) },
            valueRange = 3f..12f,
            steps = 8,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        SettingLabel(
            title = "Oscuramento overlay",
            value = "${dim.toInt()}%",
        )
        Slider(
            value = dim,
            onValueChange = { dim = it },
            onValueChangeFinished = { onDimPercentChanged(dim.toInt()) },
            valueRange = 0f..35f,
        )
        Text(
            text = "Con 0–10% la videochiamata resta praticamente invariata.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodySmall,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        SwitchRow(
            title = "Protezione tendina avanzata",
            subtitle = if (shadeAccessibilityEnabled) {
                "Quando Screen Lock è attivo, prova a richiudere subito notifiche e Comandi rapidi usando l’azione di sistema di Accessibilità."
            } else {
                "Richiede una configurazione opzionale in Accessibilità. Tocca per vedere cosa attivare e perché serve."
            },
            checked = shadeProtectionEnabled,
            onCheckedChange = onShadeProtectionChanged,
        )

        AnimatedVisibility(visible = shadeProtectionMessage != null) {
            Text(
                text = shadeProtectionMessage.orEmpty(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        SwitchRow(
            title = "Mostra istruzione al centro",
            subtitle = "Mostra il punto di sblocco all’attivazione; dopo poco diventa quasi invisibile e riappare durante la pressione.",
            checked = hint,
            onCheckedChange = {
                hint = it
                onShowHintChanged(it)
            },
        )
        SwitchRow(
            title = "Feedback aptico",
            subtitle = "Un piccolo feedback quando parte e termina lo sblocco.",
            checked = vibration,
            onCheckedChange = {
                vibration = it
                onHapticsChanged(it)
            },
        )
    }
}

@Composable
private fun PrivacyCard() {
    AppCard {
        Text(
            text = "Privacy by design",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Nessun account e nessuna analisi del contenuto dello schermo. Il permesso Internet serve solo per controllare e scaricare le release ufficiali da GitHub. L’overlay locale cattura i tocchi senza leggere ciò che c’è sullo schermo.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Il lock normale resta sotto le finestre critiche di sistema. La Protezione tendina avanzata può chiedere ad Android di richiudere notifiche e Comandi rapidi, ma Power, emergenze e altre UI di sicurezza restano sempre sotto il controllo del sistema.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingLabel(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
