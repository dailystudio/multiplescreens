package com.dailystudio.multiplescreens.utils

import com.dailystudio.devbricksx.GlobalContextWrapper
import kotlin.math.roundToInt

object MetricsUtils {

    fun dpToPx(dp: Int): Int {
        val context = GlobalContextWrapper.context ?: return 0

        return (dp * context.resources.displayMetrics.density).roundToInt()
    }

    fun pxToDp(px: Int): Int {
        val context = GlobalContextWrapper.context ?: return 0

        return (px / context.resources.displayMetrics.density).roundToInt()
    }

}