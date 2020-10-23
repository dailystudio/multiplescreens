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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class GridScreen: AbsSurfaceView {

    var gridWidthInDp: Int = 1
    var gridHeightInDp: Int = 1
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

    init {
        setFramesPerSecond(1)
    }

    override fun drawingCanvas(canvas: Canvas) {
        gridHeightInDp = if (gridHeightInDp == 0) 1 else gridHeightInDp
        gridWidthInDp = if (gridWidthInDp == 0) 1 else gridWidthInDp

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

        val startGridCol = floor(drawingBoundInDp.left / gridWidthInDp.toFloat()).roundToInt()
        val startGridRow = floor(drawingBoundInDp.top / gridHeightInDp.toFloat()).roundToInt()
        val endGridCol = ceil(drawingBoundInDp.right / gridWidthInDp.toFloat()).roundToInt()
        val endGridRow = ceil(drawingBoundInDp.bottom / gridHeightInDp.toFloat()).roundToInt()
        Logger.debug("start grid: col = $startGridCol, row = $startGridRow")
        Logger.debug("end grid: col = $endGridCol, row = $endGridRow")

        paint.apply {
            color = Color.GREEN
        }

        val canvasOffsetXInPx = dpToPx(drawingBoundInDp.left)
        val canvasOffsetYInPx = dpToPx(drawingBoundInDp.top)
        Logger.debug("canvasOffsetXInPx = $canvasOffsetXInPx, canvasOffsetYInPx = $canvasOffsetYInPx")

        for (col in startGridCol .. endGridCol) {
            val lineX = col * gwInPx - canvasOffsetXInPx.toFloat()
            Logger.debug("lineX = $col * $gwInPx - ${canvasOffsetXInPx.toFloat()} = $lineX")
            val lineStartY =
                startGridRow * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
            val lineEndY = endGridRow * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
            canvas.drawLine(lineX, lineStartY, lineX, lineEndY, paint)
        }

        for (row in startGridRow .. endGridRow) {
            val lineY = row * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
            val lineStartX =
                startGridCol * gwInPx - canvasOffsetXInPx.toFloat()
            val lineEndX =
                endGridCol * gwInPx - canvasOffsetYInPx.toFloat()
            canvas.drawLine(lineStartX, lineY, lineEndX, lineY, paint)
        }
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