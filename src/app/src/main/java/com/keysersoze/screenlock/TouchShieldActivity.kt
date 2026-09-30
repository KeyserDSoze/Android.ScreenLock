package com.keysersoze.screenlock

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import kotlin.math.min

/**
 * Experimental stronger lock mode.
 *
 * A translucent Activity sits above the call, absorbs touches and uses Android
 * immersive mode to hide system bars. Android intentionally still allows the
 * user to reveal transient system bars with edge gestures on personal devices,
 * so this is a mitigation rather than kiosk/device-owner mode.
 */
class TouchShieldActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var shieldView: ShieldView
    private var armed = false

    private val armRunnable = Runnable {
        armed = true
        shieldView.setArmed(true)
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.LOCKED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.action == ACTION_UNLOCK) {
            finish()
            return
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )

        hideSystemBars()

        shieldView = ShieldView(
            context = this,
            unlockDurationMs = LockPreferences.unlockSeconds(this) * 1_000L,
            dimPercent = LockPreferences.dimPercent(this),
            showHint = LockPreferences.showHint(this),
            haptics = LockPreferences.haptics(this),
            onUnlock = ::finish,
        )
        setContentView(shieldView)

        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.ARMING)
        val delayMs = LockPreferences.activationDelaySeconds(this) * 1_000L
        if (delayMs <= 0L) {
            armRunnable.run()
        } else {
            handler.postDelayed(armRunnable, delayMs)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == ACTION_UNLOCK) {
            finish()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    @Deprecated("Back is intentionally blocked while Screen Lock is active.")
    override fun onBackPressed() = Unit

    override fun onDestroy() {
        handler.removeCallbacks(armRunnable)
        if (::shieldView.isInitialized) shieldView.prepareForRemoval()
        ScreenLockRuntime.publish(this, ScreenLockRuntime.LockState.IDLE)
        super.onDestroy()
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(
                    WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars(),
                )
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    private class ShieldView(
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
        private var activatedAt = SystemClock.uptimeMillis()
        private var holdStartedAt = 0L
        private var armed = false

        private val unlockRunnable = Runnable {
            if (armed && tracker.isHolding) {
                tracker.cancel()
                if (haptics) performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onUnlock()
            }
        }

        init {
            isClickable = true
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        fun setArmed(value: Boolean) {
            armed = value
            activatedAt = SystemClock.uptimeMillis()
            invalidate()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (!armed) return true

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

            val dimAlpha = (255 * (dimPercent / 100f)).toInt().coerceIn(0, 255)
            canvas.drawColor(Color.argb(dimAlpha, 0, 0, 0))
            if (!armed) return

            val now = SystemClock.uptimeMillis()
            val cx = width / 2f
            val cy = height / 2f
            val radius = min(targetRadius, min(width, height) * 0.20f)
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
                paint.style = Paint.Style.STROKE
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
                    canvas,
                    cx,
                    cy - 8f * density,
                    if (tracker.isHolding) 255 else ambientAlpha.coerceAtLeast(50),
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
                        "Continua a premere…"
                    } else {
                        "Tieni premuto ${seconds}s per sbloccare"
                    },
                    cx,
                    cy + 46f * density,
                    paint,
                )
            }

            if (introVisible && !tracker.isHolding) postInvalidateDelayed(80L)
        }

        private fun drawLock(canvas: Canvas, cx: Float, cy: Float, alpha: Int) {
            paint.color = Color.argb(alpha.coerceIn(0, 255), 184, 243, 210)
            paint.strokeWidth = 4f * density
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
        const val ACTION_LOCK = "com.keysersoze.screenlock.action.SHIELD_LOCK"
        const val ACTION_UNLOCK = "com.keysersoze.screenlock.action.SHIELD_UNLOCK"
        private const val HINT_FULL_VISIBILITY_MS = 1_600L

        fun intent(context: Context, action: String): Intent =
            Intent(context, TouchShieldActivity::class.java)
                .setAction(action)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
