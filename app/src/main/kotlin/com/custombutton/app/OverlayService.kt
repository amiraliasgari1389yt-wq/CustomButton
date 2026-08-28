package com.custombutton.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class OverlayService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private const val CHANNEL_ID = "home_launch_overlay"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var pillView: View? = null

    private val holdHandler = Handler(Looper.getMainLooper())
    private val holdRunnable = Runnable { launchTargetOrPicker() }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        startForeground(NOTIFICATION_ID, buildNotification())
        addPillView()

        getSharedPreferences(PrefsHelper.prefsName(), Context.MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        getSharedPreferences(PrefsHelper.prefsName(), Context.MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(this)
        holdHandler.removeCallbacks(holdRunnable)
        removePillView()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        pillView?.background = PillAppearance.buildDrawable(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PrefsHelper.lengthKey()) {
            updatePillSize()
        }
    }

    @Suppress("ClickableViewAccessibility")
    private fun addPillView() {
        val manager = windowManager ?: return

        val view = View(this)
        view.background = PillAppearance.buildDrawable(this)
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    holdHandler.removeCallbacks(holdRunnable)
                    holdHandler.postDelayed(
                        holdRunnable,
                        PrefsHelper.getHoldDurationMs(this).toLong()
                    )
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    holdHandler.removeCallbacks(holdRunnable)
                    true
                }
                else -> false
            }
        }

        val params = buildLayoutParams()

        try {
            manager.addView(view, params)
            pillView = view
        } catch (e: Exception) {
            // Permission may have been revoked between check and add - fail safe.
            stopSelf()
        }
    }

    private fun updatePillSize() {
        val manager = windowManager ?: return
        val view = pillView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        params.width = PillAppearance.dpToPx(
            this,
            PillAppearance.widthDp(PrefsHelper.getLengthPercent(this))
        ).toInt()
        manager.updateViewLayout(view, params)
    }

    private fun removePillView() {
        val manager = windowManager
        val view = pillView
        if (manager != null && view != null) {
            try {
                manager.removeView(view)
            } catch (e: Exception) {
                // View may already be detached - safe to ignore.
            }
        }
        pillView = null
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val widthPx = PillAppearance.dpToPx(
            this,
            PillAppearance.widthDp(PrefsHelper.getLengthPercent(this))
        ).toInt()
        val heightPx = PillAppearance.dpToPx(this, PillAppearance.heightDp()).toInt()
        val bottomMarginPx = PillAppearance.dpToPx(this, 6f).toInt()

        return WindowManager.LayoutParams(
            widthPx,
            heightPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = bottomMarginPx
        }
    }

    private fun launchTargetOrPicker() {
        val targetPackage = PrefsHelper.getTargetPackage(this)
        val launchIntent = targetPackage?.let { packageManager.getLaunchIntentForPackage(it) }

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
        } else {
            val settingsIntent = Intent(this, SettingsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(SettingsActivity.EXTRA_SHOW_SELECT_PROMPT, true)
            }
            startActivity(settingsIntent)
        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, SettingsActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_content_title))
            .setContentText(getString(R.string.notification_content_text))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
