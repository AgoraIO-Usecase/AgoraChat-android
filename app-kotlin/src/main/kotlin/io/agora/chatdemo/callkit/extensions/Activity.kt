package io.agora.chatdemo.callkit.extensions

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import io.agora.chatdemo.R
import io.agora.uikit.common.utils.StatusBarCompat

/**
 * Check if the current activity is the target activity.
 */
internal fun Activity.isTargetActivity(): Boolean {
    if (TextUtils.equals(title, getString(R.string.demo_activity_label_video_call))
        || TextUtils.equals(title, getString(R.string.demo_activity_label_multi_call))
    ) {
        return true
    }
    return false
}

/**
 * Common settings for activity
 * @param fitSystemForTheme
 */
internal fun Activity.setFitSystemForTheme(fitSystemForTheme: Boolean) {
    val colorResource = ContextCompat.getColor(this, io.agora.uikit.R.color.ease_color_background)
    val isDark = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
    setFitSystemForTheme(fitSystemForTheme, colorResource, isDark)
}


/**
 * Can set the status bar's style and change the background color
 * @param fitSystemForTheme
 * @param color Color
 */
internal fun Activity.setFitSystemForTheme(
    fitSystemForTheme: Boolean,
    @ColorInt color: Int,
    isDark: Boolean
) {
    setFitSystem(fitSystemForTheme)
    StatusBarCompat.compat(this, color)
    StatusBarCompat.setLightStatusBar(this, isDark)
}

/**
 * Can set the status bar's style and change the background color.
 * @param fitSystemForTheme
 * @param color Color string
 */
internal fun Activity.setFitSystemForTheme(fitSystemForTheme: Boolean, color: String?, isDark: Boolean) {
    setFitSystem(fitSystemForTheme)
    StatusBarCompat.compat(this, Color.parseColor(color))
    StatusBarCompat.setLightStatusBar(this, isDark)
}

/**
 * Set status bar style.
 * @param fitSystemForTheme
 */
internal fun Activity.setFitSystem(fitSystemForTheme: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
    if (fitSystemForTheme) {
        val contentFrameLayout = findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val parentView = contentFrameLayout.getChildAt(0)
        if (parentView != null && Build.VERSION.SDK_INT >= 14) {
            parentView.fitsSystemWindows = true
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
}