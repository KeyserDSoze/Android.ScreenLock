package com.keysersoze.screenlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class ScreenLockOverlayService : Service() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: LockOverlayView? = null

    private val pendingLockRunnable = Runnable { showOverlay() }

    override fun onCreate() {
        super.onCreate()
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureForeground()

        when (intent?.action) {
            ACTION_LOCK -> {
                cancelPendingLock()
                showOverlay()
            }

            ACTION_UNLOCK -> finishLock()
            ACTION_TOGGLE -> toggleLock()
            else -> finishLock()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(pendingLockRunnable)
        removeOverlay()
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
        super.onDestroy()
    }

    private fun toggleLock() {
        when {
            overlay != null -> finishLock()
            ScreenLockRuntime.state == ScreenLockRuntime.LockState.ARMING -> finishLock()
            else -> scheduleLock()
        }
    }

    private fun scheduleLock() {
        if (!LockPreferences.canDrawOverlays(this)) {
            finishLock()
            return
        }

        val delayMs = LockPreferences.activationDelaySeconds(this) * 1_000L
        if (delayMs <= 0L) {
            showOverlay()
            return
        }

        handler.removeCallbacks(pendingLockRunnable)
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.ARMING)
        updateNotification()
        handler.postDelayed(pendingLockRunnable, delayMs)
    }

    private fun cancelPendingLock() {
        handler.removeCallbacks(pendingLockRunnable)
        if (ScreenLockRuntime.state == ScreenLockRuntime.LockState.ARMING) {
            ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
        }
    }

    private fun showOverlay() {
        handler.removeCallbacks(pendingLockRunnable)
        if (overlay != null) return

        if (!LockPreferences.canDrawOverlays(this)) {
            finishLock()
            return
        }

        val view = LockOverlayView(
            context = this,
            unlockDurationMs = LockPreferences.unlockSeconds(this) * 1_000L,
            dimPercent = LockPreferences.dimPercent(this),
            showHint = LockPreferences.showHint(this),
            haptics = LockPreferences.haptics(this),
            unlockRadiusDp = LockPreferences.unlockRadiusDp(this),
            unlockPosition = LockPreferences.unlockPosition(this),
            onUnlock = ::finishLock,
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT,
        ).apply {
            if (Build.VERSION.SDK_INT >= 28) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
        }

        runCatching {
            windowManager.addView(view, params)
            overlay = view
            ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.LOCKED)
            updateNotification()
        }.onFailure {
            overlay = null
            ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun finishLock() {
        handler.removeCallbacks(pendingLockRunnable)
        removeOverlay()
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeOverlay() {
        overlay?.let { view ->
            view.prepareForRemoval()
            runCatching { windowManager.removeView(view) }
        }
        overlay = null
    }

    private fun ensureForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            201,
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
            ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val text = when (ScreenLockRuntime.state) {
            ScreenLockRuntime.LockState.ARMING -> getString(R.string.notification_arming)
            ScreenLockRuntime.LockState.LOCKED -> getString(R.string.notification_locked)
            ScreenLockRuntime.LockState.IDLE -> getString(R.string.notification_idle)
        }

        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setSmallIcon(R.drawable.ic_lock)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return

        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private class LockOverlayView(
        context: Context,
        private val unlockDurationMs: Long,
        private val dimPercent: Int,
        private val showHint: Boolean,
        private val haptics: Boolean,
        unlockRadiusDp: Int,
        private val unlockPosition: UnlockTargetPosition,
        private val onUnlock: () -> Unit,
    ) : View(context) {

        private val density = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val targetRadius = unlockRadiusDp.coerceIn(40, 120) * density
        private val travelTolerance = maxOf(28f * density, targetRadius * 0.38f)
        private val tracker = UnlockGestureTracker(targetRadius, travelTolerance)
        private val activatedAt = SystemClock.uptimeMillis()
        private var holdStartedAt = 0L

        private val unlockRunnable = Runnable {
            if (tracker.isHolding) {
                tracker.cancel()
                if (haptics) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onUnlock()
            }
        }

        init {
            isClickable = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (showHint) postInvalidateDelayed(HINT_FULL_VISIBILITY_MS)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (tracker.isHolding) cancelHold()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val (cx, cy) = targetCenter()

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (tracker.onDown(event.x, event.y, cx, cy)) {
                        startHold()
                    } else {
                        cancelHold()
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> cancelHold()

                MotionEvent.ACTION_MOVE -> {
                    if (tracker.isHolding && !tracker.onMove(event.x, event.y, cx, cy)) {
                        cancelHold()
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelHold()
            }
            return true
        }

        fun prepareForRemoval() {
            removeCallbacks(unlockRunnable)
            tracker.cancel()
        }

        private fun startHold() {
            holdStartedAt = SystemClock.uptimeMillis()
            if (haptics) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            removeCallbacks(unlockRunnable)
            postDelayed(unlockRunnable, unlockDurationMs)
            postInvalidateOnAnimation()
        }

        private fun cancelHold() {
            tracker.cancel()
            removeCallbacks(unlockRunnable)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = SystemClock.uptimeMillis()
            val (cx, cy) = targetCenter()
            val radius = targetRadius

            val dimAlpha = (255 * (dimPercent / 100f)).toInt().coerceIn(0, 255)
            canvas.drawColor(Color.argb(dimAlpha, 0, 0, 0))

            val introVisible = showHint && now - activatedAt < HINT_FULL_VISIBILITY_MS
            val ambientAlpha = when {
                tracker.isHolding -> 215
                introVisible -> 150
                showHint -> 34
                else -> 0
            }

            if (ambientAlpha > 0) {
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(ambientAlpha, 8, 12, 18)
                canvas.drawCircle(cx, cy, radius, paint)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = if (tracker.isHolding) 3f * density else 2f * density
                paint.color = Color.argb(
                    if (tracker.isHolding) 220 else ambientAlpha.coerceAtMost(115),
                    184,
                    243,
                    210,
                )
                canvas.drawCircle(cx, cy, radius, paint)
            }

            if (tracker.isHolding) {
                val elapsed = (now - holdStartedAt).coerceAtLeast(0L)
                val progress = (elapsed.toFloat() / unlockDurationMs).coerceIn(0f, 1f)
                paint.strokeWidth = 7f * density
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = Color.rgb(184, 243, 210)
                val ring = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(ring, -90f, 360f * progress, false, paint)
                paint.strokeCap = Paint.Cap.BUTT
                if (progress < 1f) postInvalidateOnAnimation()
            }

            if (tracker.isHolding || ambientAlpha > 0) {
                drawLock(
                    canvas = canvas,
                    cx = cx,
                    cy = cy - 8f * density,
                    alpha = if (tracker.isHolding) 255 else ambientAlpha.coerceAtLeast(50),
                )
            }

            if (showHint && (introVisible || tracker.isHolding)) {
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(230, 255, 255, 255)
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 13f * resources.displayMetrics.scaledDensity
                val seconds = unlockDurationMs / 1_000L
                canvas.drawText(
                    if (tracker.isHolding) {
                        context.getString(R.string.overlay_continue_press)
                    } else {
                        context.getString(R.string.overlay_hold_to_unlock_format, seconds)
                    },
                    cx,
                    cy + 46f * density,
                    paint,
                )
            }

            if (introVisible && !tracker.isHolding) {
                postInvalidateDelayed(80L)
            }
        }

        private fun targetCenter(): Pair<Float, Float> {
            val sidePadding = 24f * density
            val topPadding = 48f * density
            val bottomPadding = 64f * density

            val left = (targetRadius + sidePadding).coerceAtMost(width / 2f)
            val centerX = width / 2f
            val right = (width - targetRadius - sidePadding).coerceAtLeast(width / 2f)

            val top = (targetRadius + topPadding).coerceAtMost(height / 2f)
            val centerY = height / 2f
            val bottom = (height - targetRadius - bottomPadding).coerceAtLeast(height / 2f)

            val x = when (unlockPosition) {
                UnlockTargetPosition.TOP_LEFT,
                UnlockTargetPosition.CENTER_LEFT,
                UnlockTargetPosition.BOTTOM_LEFT -> left

                UnlockTargetPosition.TOP_CENTER,
                UnlockTargetPosition.CENTER,
                UnlockTargetPosition.BOTTOM_CENTER -> centerX

                UnlockTargetPosition.TOP_RIGHT,
                UnlockTargetPosition.CENTER_RIGHT,
                UnlockTargetPosition.BOTTOM_RIGHT -> right
            }

            val y = when (unlockPosition) {
                UnlockTargetPosition.TOP_LEFT,
                UnlockTargetPosition.TOP_CENTER,
                UnlockTargetPosition.TOP_RIGHT -> top

                UnlockTargetPosition.CENTER_LEFT,
                UnlockTargetPosition.CENTER,
                UnlockTargetPosition.CENTER_RIGHT -> centerY

                UnlockTargetPosition.BOTTOM_LEFT,
                UnlockTargetPosition.BOTTOM_CENTER,
                UnlockTargetPosition.BOTTOM_RIGHT -> bottom
            }

            return x to y
        }

        private fun drawLock(canvas: Canvas, cx: Float, cy: Float, alpha: Int) {
            val stroke = 4f * density
            paint.color = Color.argb(alpha.coerceIn(0, 255), 184, 243, 210)
            paint.strokeWidth = stroke
            paint.strokeCap = Paint.Cap.ROUND
            paint.style = Paint.Style.STROKE

            val shackle = RectF(
                cx - 14f * density,
                cy - 24f * density,
                cx + 14f * density,
                cy + 4f * density,
            )
            canvas.drawArc(shackle, 180f, 180f, false, paint)

            paint.style = Paint.Style.FILL
            val body = RectF(
                cx - 20f * density,
                cy - 4f * density,
                cx + 20f * density,
                cy + 26f * density,
            )
            canvas.drawRoundRect(body, 7f * density, 7f * density, paint)

            paint.color = Color.argb(alpha.coerceIn(0, 255), 8, 12, 18)
            canvas.drawCircle(cx, cy + 9f * density, 4f * density, paint)
            canvas.drawRect(
                cx - 2f * density,
                cy + 9f * density,
                cx + 2f * density,
                cy + 17f * density,
                paint,
            )
        }
    }

    companion object {
        const val ACTION_LOCK = "com.keysersoze.screenlock.action.LOCK"
        const val ACTION_UNLOCK = "com.keysersoze.screenlock.action.UNLOCK"
        const val ACTION_TOGGLE = "com.keysersoze.screenlock.action.TOGGLE"

        private const val NOTIFICATION_CHANNEL_ID = "screen_lock_active"
        private const val NOTIFICATION_ID = 1001
        private const val HINT_FULL_VISIBILITY_MS = 1_600L

        fun start(context: Context, action: String) {
            val intent = Intent(context, ScreenLockOverlayService::class.java).setAction(action)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
