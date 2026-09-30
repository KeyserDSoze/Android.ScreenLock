package com.keysersoze.screenlock

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import kotlin.math.hypot
import kotlin.math.min

class ScreenLockAccessibilityService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }
    private var overlay: LockOverlayView? = null
    private var receiverRegistered = false

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_LOCK -> showOverlay()
                ACTION_UNLOCK -> hideOverlay()
                ACTION_TOGGLE -> if (overlay == null) showOverlay() else hideOverlay()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
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
        LockPreferences.setLocked(this, false)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(commandReceiver) }
            receiverRegistered = false
        }
        hideOverlay()
        super.onDestroy()
    }

    private fun showOverlay() {
        if (overlay != null) return

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
            LockPreferences.setLocked(this, true)
        }.onFailure {
            overlay = null
            LockPreferences.setLocked(this, false)
        }
    }

    private fun hideOverlay() {
        overlay?.let { view -> runCatching { windowManager.removeView(view) } }
        overlay = null
        LockPreferences.setLocked(this, false)
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
        private val targetRadius = 78f * density
        private val travelTolerance = 42f * density
        private var holding = false
        private var holdStartedAt = 0L
        private var downX = 0f
        private var downY = 0f

        private val unlockRunnable = Runnable {
            if (holding) {
                holding = false
                if (haptics) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onUnlock()
            }
        }

        init {
            isClickable = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    if (isInsideTarget(event.x, event.y)) startHold() else cancelHold()
                }

                MotionEvent.ACTION_POINTER_DOWN -> cancelHold()

                MotionEvent.ACTION_MOVE -> {
                    if (holding) {
                        val moved = hypot(event.x - downX, event.y - downY)
                        if (moved > travelTolerance || !isInsideTarget(event.x, event.y)) {
                            cancelHold()
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> cancelHold()
            }
            return true
        }

        private fun isInsideTarget(x: Float, y: Float): Boolean {
            val cx = width / 2f
            val cy = height / 2f
            return hypot(x - cx, y - cy) <= targetRadius
        }

        private fun startHold() {
            if (holding) return
            holding = true
            holdStartedAt = SystemClock.uptimeMillis()
            if (haptics) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            removeCallbacks(unlockRunnable)
            postDelayed(unlockRunnable, unlockDurationMs)
            postInvalidateOnAnimation()
        }

        private fun cancelHold() {
            if (!holding) return
            holding = false
            removeCallbacks(unlockRunnable)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val radius = min(targetRadius, min(width, height) * 0.22f)

            val alpha = (255 * (dimPercent / 100f)).toInt().coerceIn(0, 255)
            canvas.drawColor(Color.argb(alpha, 0, 0, 0))

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(if (holding) 210 else 145, 8, 12, 18)
            canvas.drawCircle(cx, cy, radius, paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f * density
            paint.color = Color.argb(210, 184, 243, 210)
            canvas.drawCircle(cx, cy, radius, paint)

            if (holding) {
                val elapsed = (SystemClock.uptimeMillis() - holdStartedAt).coerceAtLeast(0L)
                val progress = (elapsed.toFloat() / unlockDurationMs).coerceIn(0f, 1f)
                paint.strokeWidth = 7f * density
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = Color.rgb(184, 243, 210)
                val ring = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
                canvas.drawArc(ring, -90f, 360f * progress, false, paint)
                paint.strokeCap = Paint.Cap.BUTT
                if (progress < 1f) postInvalidateOnAnimation()
            }

            drawLock(canvas, cx, cy - 8f * density)

            if (showHint) {
                paint.style = Paint.Style.FILL
                paint.color = Color.argb(225, 255, 255, 255)
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 13f * resources.displayMetrics.scaledDensity
                val seconds = unlockDurationMs / 1_000L
                canvas.drawText(
                    if (holding) "Continua a premere…" else "Tieni premuto ${seconds}s per sbloccare",
                    cx,
                    cy + 46f * density,
                    paint,
                )
            }
        }

        private fun drawLock(canvas: Canvas, cx: Float, cy: Float) {
            val stroke = 4f * density
            paint.color = Color.rgb(184, 243, 210)
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

            paint.color = Color.rgb(8, 12, 18)
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

        fun sendCommand(context: Context, action: String) {
            context.sendBroadcast(Intent(action).setPackage(context.packageName))
        }
    }
}
