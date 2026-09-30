package com.keysersoze.screenlock.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.keysersoze.screenlock.AppLanguageOption
import com.keysersoze.screenlock.R
import com.keysersoze.screenlock.UnlockTargetPosition
import com.keysersoze.screenlock.UpdateInfo

@Composable
fun ScreenLockApp(
    overlayEnabled: Boolean,
    showOverlayHelp: Boolean,
    showShadeProtectionHelp: Boolean,
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    unlockRadiusDp: Int,
    unlockPosition: UnlockTargetPosition,
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
    selectedLanguage: String,
    languageOptions: List<AppLanguageOption>,
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
    onUnlockRadiusChanged: (Int) -> Unit,
    onUnlockPositionChanged: (UnlockTargetPosition) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onShadeProtectionChanged: (Boolean) -> Unit,
    onAutoUpdatesChanged: (Boolean) -> Unit,
    onCheckUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onLanguageChanged: (String) -> Unit,
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
                    LanguageCard(
                        selectedLanguage = selectedLanguage,
                        languageOptions = languageOptions,
                        onLanguageChanged = onLanguageChanged,
                    )
                }
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
                        unlockRadiusDp = unlockRadiusDp,
                        unlockPosition = unlockPosition,
                        dimPercent = dimPercent,
                        showHint = showHint,
                        haptics = haptics,
                        shadeProtectionEnabled = shadeProtectionEnabled,
                        shadeAccessibilityEnabled = shadeAccessibilityEnabled,
                        shadeProtectionMessage = shadeProtectionMessage,
                        onActivationDelayChanged = onActivationDelayChanged,
                        onUnlockSecondsChanged = onUnlockSecondsChanged,
                        onUnlockRadiusChanged = onUnlockRadiusChanged,
                        onUnlockPositionChanged = onUnlockPositionChanged,
                        onDimPercentChanged = onDimPercentChanged,
                        onShowHintChanged = onShowHintChanged,
                        onHapticsChanged = onHapticsChanged,
                        onShadeProtectionChanged = onShadeProtectionChanged,
                    )
                }
                item { PrivacyCard() }
                item {
                    Text(
                        text = stringResource(R.string.footer),
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
        Image(
            painter = painterResource(R.drawable.screen_lock_brand_icon),
            contentDescription = null,
            modifier = Modifier.size(82.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.2f).sp,
            )
            Text(
                text = stringResource(R.string.hero_tagline),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.hero_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
            )
        }

        Image(
            painter = painterResource(R.drawable.screen_lock_home_hero),
            contentDescription = stringResource(R.string.hero_image_content_description),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop,
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
            text = if (enabled) stringResource(R.string.status_overlay_ready) else stringResource(R.string.status_step1_pending),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LanguageCard(
    selectedLanguage: String,
    languageOptions: List<AppLanguageOption>,
    onLanguageChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val systemLabel = stringResource(R.string.language_system)
    val selectedLabel = if (selectedLanguage == "system") {
        systemLabel
    } else {
        languageOptions.firstOrNull { it.code == selectedLanguage }?.nativeName ?: systemLabel
    }

    AppCard {
        Text(
            text = stringResource(R.string.language_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.language_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Box {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { expanded = true },
            ) {
                Text(stringResource(R.string.language_selected_format, selectedLabel))
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(systemLabel) },
                    onClick = {
                        expanded = false
                        onLanguageChanged("system")
                    },
                )
                languageOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.nativeName) },
                        onClick = {
                            expanded = false
                            onLanguageChanged(option.code)
                        },
                    )
                }
            }
        }
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
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (enabled) {
                stringResource(R.string.setup_step2_desc)
            } else {
                stringResource(R.string.setup_step1_desc)
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
                Text(stringResource(R.string.setup_allow_overlay))
            }
            Text(
                text = stringResource(R.string.setup_overlay_base_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddQuickTile,
            ) {
                Text(stringResource(R.string.setup_add_tile))
            }
            Text(
                text = stringResource(R.string.setup_after_tile),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onTestLock,
            ) {
                Text(stringResource(R.string.setup_test_lock))
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
                text = stringResource(R.string.overlay_dialog_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.overlay_dialog_intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.overlay_dialog_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.overlay_dialog_note),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onContinue) {
                Text(stringResource(R.string.open_android_setting))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.not_now))
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
                text = stringResource(R.string.shade_dialog_title),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.shade_dialog_intro),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.shade_dialog_privacy),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.shade_dialog_steps),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(R.string.shade_dialog_restricted),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onOpenAccessibility) {
                Text(stringResource(R.string.open_accessibility))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onOpenAppInfo) {
                    Text(stringResource(R.string.app_info))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.not_now))
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
            text = stringResource(R.string.updates_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.installed_version_format, currentVersion),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SwitchRow(
            title = stringResource(R.string.auto_updates_title),
            subtitle = stringResource(R.string.auto_updates_desc),
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
                    Text(stringResource(R.string.install_update))
                }
            }

            downloading -> {
                Text(
                    text = stringResource(R.string.download_in_progress),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            updateInfo != null -> {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDownload,
                ) {
                    Text(stringResource(R.string.download_version_format, updateInfo.version))
                }
            }

            else -> {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !checking,
                    onClick = onCheck,
                ) {
                    Text(if (checking) stringResource(R.string.checking) else stringResource(R.string.check_updates))
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
            text = stringResource(R.string.update_github_note),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingsCard(
    activationDelaySeconds: Int,
    unlockSeconds: Int,
    unlockRadiusDp: Int,
    unlockPosition: UnlockTargetPosition,
    dimPercent: Int,
    showHint: Boolean,
    haptics: Boolean,
    shadeProtectionEnabled: Boolean,
    shadeAccessibilityEnabled: Boolean,
    shadeProtectionMessage: String?,
    onActivationDelayChanged: (Int) -> Unit,
    onUnlockSecondsChanged: (Int) -> Unit,
    onUnlockRadiusChanged: (Int) -> Unit,
    onUnlockPositionChanged: (UnlockTargetPosition) -> Unit,
    onDimPercentChanged: (Int) -> Unit,
    onShowHintChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onShadeProtectionChanged: (Boolean) -> Unit,
) {
    var delay by remember(activationDelaySeconds) { mutableIntStateOf(activationDelaySeconds) }
    var seconds by remember(unlockSeconds) { mutableIntStateOf(unlockSeconds) }
    var radius by remember(unlockRadiusDp) { mutableIntStateOf(unlockRadiusDp) }
    var position by remember(unlockPosition) { mutableStateOf(unlockPosition) }
    var dim by remember(dimPercent) { mutableFloatStateOf(dimPercent.toFloat()) }
    var hint by remember(showHint) { mutableStateOf(showHint) }
    var vibration by remember(haptics) { mutableStateOf(haptics) }

    AppCard {
        Text(
            text = stringResource(R.string.behavior_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.behavior_intro),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SettingLabel(
            title = stringResource(R.string.activation_delay),
            value = if (delay == 0) stringResource(R.string.immediately) else stringResource(R.string.seconds_format, delay),
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
            text = stringResource(R.string.unlock_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.unlock_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )

        SettingLabel(
            title = stringResource(R.string.long_press),
            value = stringResource(R.string.seconds_format, seconds),
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
            title = stringResource(R.string.unlock_radius_title),
            value = stringResource(R.string.dp_format, radius),
        )
        Slider(
            value = radius.toFloat(),
            onValueChange = {
                radius = ((it / 5f).roundToInt() * 5).coerceIn(40, 120)
            },
            onValueChangeFinished = { onUnlockRadiusChanged(radius) },
            valueRange = 40f..120f,
            steps = 15,
        )

        Text(
            text = stringResource(R.string.unlock_position_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.unlock_position_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
        UnlockPositionGrid(
            position = position,
            onPositionChanged = {
                position = it
                onUnlockPositionChanged(it)
            },
        )
        Text(
            text = stringResource(
                R.string.unlock_position_selected_format,
                unlockPositionLabel(position),
            ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        SettingLabel(
            title = stringResource(R.string.overlay_dimming),
            value = stringResource(R.string.percent_format, dim.toInt()),
        )
        Slider(
            value = dim,
            onValueChange = { dim = it },
            onValueChangeFinished = { onDimPercentChanged(dim.toInt()) },
            valueRange = 0f..35f,
        )
        Text(
            text = stringResource(R.string.video_call_unchanged),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
            style = MaterialTheme.typography.bodySmall,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        SwitchRow(
            title = stringResource(R.string.shade_protection_title),
            subtitle = if (shadeAccessibilityEnabled) {
                stringResource(R.string.shade_protection_enabled_desc)
            } else {
                stringResource(R.string.shade_protection_disabled_desc)
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
            title = stringResource(R.string.show_hint_title),
            subtitle = stringResource(R.string.show_hint_desc),
            checked = hint,
            onCheckedChange = {
                hint = it
                onShowHintChanged(it)
            },
        )
        SwitchRow(
            title = stringResource(R.string.haptics_title),
            subtitle = stringResource(R.string.haptics_desc),
            checked = vibration,
            onCheckedChange = {
                vibration = it
                onHapticsChanged(it)
            },
        )
    }
}

@Composable
private fun UnlockPositionGrid(
    position: UnlockTargetPosition,
    onPositionChanged: (UnlockTargetPosition) -> Unit,
) {
    val rows = listOf(
        listOf(
            UnlockTargetPosition.TOP_LEFT,
            UnlockTargetPosition.TOP_CENTER,
            UnlockTargetPosition.TOP_RIGHT,
        ),
        listOf(
            UnlockTargetPosition.CENTER_LEFT,
            UnlockTargetPosition.CENTER,
            UnlockTargetPosition.CENTER_RIGHT,
        ),
        listOf(
            UnlockTargetPosition.BOTTOM_LEFT,
            UnlockTargetPosition.BOTTOM_CENTER,
            UnlockTargetPosition.BOTTOM_RIGHT,
        ),
    )

    rows.forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { option ->
                val symbol = when (option) {
                    UnlockTargetPosition.TOP_LEFT -> "↖"
                    UnlockTargetPosition.TOP_CENTER -> "↑"
                    UnlockTargetPosition.TOP_RIGHT -> "↗"
                    UnlockTargetPosition.CENTER_LEFT -> "←"
                    UnlockTargetPosition.CENTER -> "•"
                    UnlockTargetPosition.CENTER_RIGHT -> "→"
                    UnlockTargetPosition.BOTTOM_LEFT -> "↙"
                    UnlockTargetPosition.BOTTOM_CENTER -> "↓"
                    UnlockTargetPosition.BOTTOM_RIGHT -> "↘"
                }

                if (option == position) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { onPositionChanged(option) },
                    ) {
                        Text(symbol, style = MaterialTheme.typography.titleLarge)
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = { onPositionChanged(option) },
                    ) {
                        Text(symbol, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun unlockPositionLabel(position: UnlockTargetPosition): String =
    stringResource(
        when (position) {
            UnlockTargetPosition.TOP_LEFT -> R.string.unlock_position_top_left
            UnlockTargetPosition.TOP_CENTER -> R.string.unlock_position_top_center
            UnlockTargetPosition.TOP_RIGHT -> R.string.unlock_position_top_right
            UnlockTargetPosition.CENTER_LEFT -> R.string.unlock_position_center_left
            UnlockTargetPosition.CENTER -> R.string.unlock_position_center
            UnlockTargetPosition.CENTER_RIGHT -> R.string.unlock_position_center_right
            UnlockTargetPosition.BOTTOM_LEFT -> R.string.unlock_position_bottom_left
            UnlockTargetPosition.BOTTOM_CENTER -> R.string.unlock_position_bottom_center
            UnlockTargetPosition.BOTTOM_RIGHT -> R.string.unlock_position_bottom_right
        },
    )

@Composable
private fun PrivacyCard() {
    AppCard {
        Text(
            text = stringResource(R.string.privacy_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.privacy_body1),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.privacy_body2),
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
