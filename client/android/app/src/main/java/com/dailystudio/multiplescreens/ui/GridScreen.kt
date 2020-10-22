package com.dailystudio.multiplescreens.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import kotlin.math.floor
import kotlin.math.roundToInt

class GridScreen: AbsSurfaceView {

    var gridWidthInDp: Int = 0
    var gridHeightInDp: Int = 0
    var drawingBoundInDp: Rect = Rect()

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

        val drawingAreaWidth = dpToPx(drawingBoundInDp.width())
        val drawingAreaHeight = dpToPx(drawingBoundInDp.height())
        val drawingAreaX = 0
        val drawingAreaY = ((height - drawingAreaHeight) / 2.0f).roundToInt()

        val gwInPx = dpToPx(gridWidthInDp)
        val ghInPx = dpToPx(gridHeightInDp)

        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        canvas.drawRect(
            Rect(drawingAreaX, drawingAreaY,
                drawingAreaX + drawingAreaWidth, drawingAreaY + drawingAreaHeight),
            paint)

//        val startGridCol = floor(canvasOffsetXInDp / gridWidthInDp.toFloat()).roundToInt()
//        val startGridRow = floor(canvasOffsetYInDp / gridHeightInDp.toFloat()).roundToInt()
//        Logger.debug("start grid: col = $startGridCol, row = $startGridRow")
    }

    fun updateDimension(gridWidthInDp: Int, gridHeightInDp: Int,
                        drawingBoundInDp: Rect?) {
        this.gridWidthInDp = gridWidthInDp
        this.gridHeightInDp = gridHeightInDp
        this.drawingBoundInDp = drawingBoundInDp ?: Rect()

        invalidate()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).roundToInt()
    }

}