package com.dailystudio.multiplescreens.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import kotlin.math.roundToInt

class GridScreen: AbsSurfaceView {

    var gridWidthInDp: Int = 0
    var gridHeightInDp: Int = 0
    var xOffsetInDp: Int = 0
    var yOffsetInDp: Int = 0

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun drawingCanvas(canvas: Canvas) {
        canvas.drawColor(Color.BLACK)

        val gwInPx = dpToPx(gridWidthInDp)
        val ghInPx = dpToPx(gridHeightInDp)
        val xStart = dpToPx(xOffsetInDp)
        val yStart = dpToPx(yOffsetInDp)

        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        canvas.drawRect(
            Rect(xStart, yStart,
                xStart + width, yStart + height - 2 * yStart),
            paint)
    }

    fun updateDimension(gridWidthInDp: Int, gridHeightInDp: Int,
                        xOffsetInDp: Int, yOffsetInDp: Int) {
        this.gridWidthInDp = gridWidthInDp
        this.gridHeightInDp = gridHeightInDp
        this.xOffsetInDp = xOffsetInDp
        this.yOffsetInDp = yOffsetInDp

        invalidate()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).roundToInt()
    }

}