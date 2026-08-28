package com.custombutton.app

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

object PillAppearance {

    private const val BASE_WIDTH_DP = 134f
    private const val HEIGHT_DP = 5f

    fun heightDp(): Float = HEIGHT_DP

    fun widthDp(lengthPercent: Int): Float = BASE_WIDTH_DP * (lengthPercent / 100f)

    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    fun pillColor(context: Context): Int {
        return if (isDarkMode(context)) Color.WHITE else Color.BLACK
    }

    fun buildDrawable(context: Context): GradientDrawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = dpToPx(context, HEIGHT_DP) / 2f
        drawable.setColor(pillColor(context))
        return drawable
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
