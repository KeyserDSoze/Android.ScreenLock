package com.keysersoze.screenlock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenLockApp(
    accessibilityEnabled: Boolean,
    showAccessibilityHelp: Boolean,
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    dimPercent: Int,
    showHint: Boolean,
    haptics: Boolean,
    tileMessage: String?,
    onEnableAccessibility: () -> Unit,
    onDismissAccessibilityHelp: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onAddQuickTile: () -> Unit,
    onTestLock: () -> Unit,
    onActivationDelayChanged: (Int) -> Unit,
    onUnlockSecondsChanged: (Int) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
) {
    if (showAccessibilityHelp) {
        AccessibilityHelpDialog(
            onDismiss = onDismissAccessibilityHelp,
            onContinue = onOpenAccessibilitySettings,
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
                item { Hero(accessibilityEnabled) }
                item {
                    SetupCard(
                        enabled = accessibilityEnabled,
                        tileMessage = tileMessage,
                        onEnableAccessibility = onEnableAccessibility,
                        onAddQuickTile = onAddQuickTile,
                        onTestLock = onTestLock,
                    )
                }
                item {
                    SettingsCard(
                        activationDelaySeconds = activationDelaySeconds,
                        unlockSeconds = unlockSeconds,
                        dimPercent = dimPercent,
                        showHint = showHint,
                        haptics = haptics,
                        onActivationDelayChanged = onActivationDelayChanged,
                        onUnlockSecondsChanged = onUnlockSecondsChanged,
                        onDimPercentChanged = onDimPercentChanged,
                        onShowHintChanged = onShowHintChanged,
                        onHapticsChanged = onHapticsChanged,
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
        Box(
            modifier = Modifier
                .size(74.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            LockGlyph(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Screen Lock",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.2f).sp,
            )
            Text(
                text = "Blocca i tocchi, non la chiamata.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Perfetto quando il telefono passa in mani piccole: Telegram resta visibile e attivo, mentre i tocchi sul contenuto vengono assorbiti.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
            )
        }

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
            text = if (enabled) "Screen Lock abilitato" else "Passaggio 1 da completare",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetupCard(
    enabled: Boolean,
    tileMessage: String?,
    onEnableAccessibility: () -> Unit,
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
                "Passaggio 1 di 2 · Android deve autorizzare Screen Lock a mostrare il blocco sopra le altre app. Ti indichiamo esattamente cosa toccare prima di aprire le Impostazioni."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(2.dp))

        if (!enabled) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onEnableAccessibility,
            ) {
                Text("1 · Abilita Screen Lock")
            }
            Text(
                text = "Non devi concedere accesso ai contenuti dello schermo: Screen Lock è configurato per non leggerli.",
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
private fun AccessibilityHelpDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Attiva Screen Lock in Android",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Si aprirà la schermata Accessibilità di Android. I nomi possono cambiare leggermente in base al telefono.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "1. Cerca “Screen Lock” (a volte è dentro “App scaricate” o “Servizi installati”).\n\n2. Tocca Screen Lock.\n\n3. Attiva “Usa Screen Lock” o l’interruttore equivalente.\n\n4. Conferma la richiesta di Android e torna qui.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Per sicurezza Android non consente all’app di attivare questo permesso da sola. Screen Lock non legge il contenuto dello schermo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text("Apri Accessibilità")
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
private fun SettingsCard(
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    dimPercent: Int,
    showHint: Boolean,
    haptics: Boolean,
    onActivationDelayChanged: (Int) -> Unit,
    onUnlockSecondsChanged: (Int) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
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
            text = "Nessun permesso Internet. Nessun account. Nessuna analisi del contenuto dello schermo. Il servizio di accessibilità è configurato con lettura del contenuto disattivata: serve solo per l’overlay che cattura i tocchi.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Quando Android lo consente, Screen Lock richiude anche la tendina notifiche se viene aperta durante il blocco. Power, emergenze e alcune UI di sistema restano comunque sotto il controllo di Android.",
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

@Composable
private fun LockGlyph(
    modifier: Modifier = Modifier,
    color: Color,
) {
    val keyholeColor = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.085f
        val bodyTop = size.height * 0.42f
        val bodyLeft = size.width * 0.18f
        val bodyWidth = size.width * 0.64f
        val bodyHeight = size.height * 0.45f

        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.30f, size.height * 0.08f),
            size = Size(size.width * 0.40f, size.height * 0.52f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.11f),
        )
        drawCircle(
            color = keyholeColor,
            radius = size.minDimension * 0.065f,
            center = Offset(size.width / 2f, size.height * 0.62f),
        )
        drawRect(
            color = keyholeColor,
            topLeft = Offset(size.width * 0.47f, size.height * 0.62f),
            size = Size(size.width * 0.06f, size.height * 0.13f),
        )
    }
}
