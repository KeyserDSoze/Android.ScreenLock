package com.keysersoze.screenlock

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.quicksettings.TileService
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.min

class ScreenLockAccessibilityService : AccessibilityService() {

    enum class LockState {
        IDLE,
        ARMING,
        LOCKED,
    }

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private val handler = Handler(Looper.getMainLooper())
    private var overlay: LockOverlayView? = null
    private var receiverRegistered = false
    private var lastShadeDismissAt = 0L

    private val pendingLockRunnable = Runnable { showOverlay() }

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_LOCK -> {
                    cancelPendingLock()
                    showOverlay()
                }

                ACTION_UNLOCK -> hideOverlay()
                ACTION_TOGGLE -> toggleLock()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        publishState(this, LockState.IDLE)

        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(ACTION_LOCK)
            addAction(ACTION_UNLOCK)
            addAction(ACTION_TOGGLE)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(commandReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(commandReceiver, filter)
        }
        receiverRegistered = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (runtimeState() != LockState.LOCKED || event == null) return
        if (Build.VERSION.SDK_INT < 31) return
        if (event.packageName?.toString() != SYSTEM_UI_PACKAGE) return

        val now = SystemClock.uptimeMillis()
        if (now - lastShadeDismissAt < SHADE_DISMISS_THROTTLE_MS) return

        lastShadeDismissAt = now
        performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        cancelPendingLock()
        if (receiverRegistered) {
            runCatching { unregisterReceiver(commandReceiver) }
            receiverRegistered = false
        }
        hideOverlay()
        publishState(this, LockState.IDLE)
        super.onDestroy()
    }

    private fun toggleLock() {
        when {
            overlay != null -> hideOverlay()
            runtimeState() == LockState.ARMING -> hideOverlay()
            else -> scheduleLock()
        }
    }

    private fun scheduleLock() {
        val delayMs = LockPreferences.activationDelaySeconds(this) * 1_000L
        if (delayMs <= 0L) {
            showOverlay()
            return
        }

        cancelPendingLock()
        publishState(this, LockState.ARMING)
        handler.postDelayed(pendingLockRunnable, delayMs)
    }

    private fun cancelPendingLock() {
        handler.removeCallbacks(pendingLockRunnable)
        if (runtimeState() == LockState.ARMING) {
            publishState(this, LockState.IDLE)
        }
    }

    private fun showOverlay() {
        if (overlay != null) return
        handler.removeCallbacks(pendingLockRunnable)

        val view = LockOverlayView(
            context = this,
            unlockDurationMs = LockPreferences.unlockSeconds(this) * 1_000L,
            dimPercent = LockPreferences.dimPercent(this),
            showHint = LockPreferences.showHint(this),
            haptics = LockPreferences.haptics(this),
            onUnlock = ::hideOverlay,
        )

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
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
            publishState(this, LockState.LOCKED)
        }.onFailure {
            overlay = null
            publishState(this, LockState.IDLE)
        }
    }

    private fun hideOverlay() {
        handler.removeCallbacks(pendingLockRunnable)
        overlay?.let { view ->
            view.prepareForRemoval()
            runCatching { windowManager.removeView(view) }
        }
        overlay = null
        publishState(this, LockState.IDLE)
    }

    private class LockOverlayView(
        context: Context,
        private val unlockDurationMs: Long,
        private val dimPercent: Int,
        private val showHint: Boolean,
        private val haptics: Boolean,
        private val onUnlock: () -> Unit,
    ) : View(context) {

        private val density = resources.displayMetrics.density
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val targetRadius = 70f * density
        private val travelTolerance = 28f * density
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
            val cx = width / 2f
            val cy = height / 2f

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
            val cx = width / 2f
            val cy = height / 2f
            val radius = min(targetRadius, min(width, height) * 0.20f)

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
                    if (tracker.isHolding) "Continua a premere…" else "Tieni premuto ${seconds}s per sbloccare",
                    cx,
                    cy + 46f * density,
                    paint,
                )
            }

            if (introVisible && !tracker.isHolding) {
                postInvalidateDelayed(80L)
            }
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

        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val SHADE_DISMISS_THROTTLE_MS = 400L
        private const val HINT_FULL_VISIBILITY_MS = 1_600L

        @Volatile
        private var state: LockState = LockState.IDLE

        fun runtimeState(): LockState = state

        private fun publishState(context: Context, newState: LockState) {
            state = newState
            runCatching {
                TileService.requestListeningState(
                    context,
                    ComponentName(context, ScreenLockTileService::class.java),
                )
            }
        }

        fun sendCommand(context: Context, action: String) {
            context.sendBroadcast(Intent(action).setPackage(context.packageName))
        }
    }
}
