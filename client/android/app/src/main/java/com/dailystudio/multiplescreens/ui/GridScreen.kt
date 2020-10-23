package com.dailystudio.multiplescreens.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.multiplescreens.R
import java.lang.NumberFormatException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class GridScreen: AbsSurfaceView {

    companion object {
        const val DEBUG_FRAMES = false

        val DRAWING_AREA_PAINT = Paint().apply {
            color = GlobalContextWrapper.context?.getColor(R.color.tomato_red) ?: Color.RED
            style = Paint.Style.FILL
        }

        val GRID_LINE_PAINT = Paint().apply {
            color = GlobalContextWrapper.context?.getColor(R.color.royal_blue) ?: Color.BLUE
            style = Paint.Style.FILL
        }

        const val DRAWING_DELAY = 200L

    }

    var gridWidthInDp: Int = 1
    var gridHeightInDp: Int = 1
    var drawingBoundInDp: Rect = Rect()

    val occupiedGrids: MutableSet<String> = mutableSetOf()

    val drawnGrids: MutableSet<String> = mutableSetOf()

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
        setFramesPerSecond(30)
    }

    override fun drawingCanvas(canvas: Canvas) {
        gridHeightInDp = if (gridHeightInDp == 0) 1 else gridHeightInDp
        gridWidthInDp = if (gridWidthInDp == 0) 1 else gridWidthInDp

        canvas.drawColor(ResourcesCompatUtils.getColor(context,
                R.color.light_black))

        val drawingAreaWidth = dpToPx(drawingBoundInDp.width())
        val drawingAreaHeight = dpToPx(drawingBoundInDp.height())
        val drawingAreaX = 0
        val drawingAreaY = ((height - drawingAreaHeight) / 2.0f).roundToInt()

        val gwInPx = dpToPx(gridWidthInDp)
        val ghInPx = dpToPx(gridHeightInDp)

        if (DEBUG_FRAMES) {
            val drawingAreaRect = Rect(drawingAreaX, drawingAreaY,
                    drawingAreaX + drawingAreaWidth, drawingAreaY + drawingAreaHeight)

            canvas.drawRect(drawingAreaRect, DRAWING_AREA_PAINT)
        }

        val startGridCol = floor(drawingBoundInDp.left / gridWidthInDp.toFloat()).roundToInt()
        val startGridRow = floor(drawingBoundInDp.top / gridHeightInDp.toFloat()).roundToInt()
        val endGridCol = ceil(drawingBoundInDp.right / gridWidthInDp.toFloat()).roundToInt()
        val endGridRow = ceil(drawingBoundInDp.bottom / gridHeightInDp.toFloat()).roundToInt()
        Logger.debug("start grid: col = $startGridCol, row = $startGridRow")
        Logger.debug("end grid: col = $endGridCol, row = $endGridRow")

        val canvasOffsetXInPx = dpToPx(drawingBoundInDp.left)
        val canvasOffsetYInPx = dpToPx(drawingBoundInDp.top)
        Logger.debug("canvasOffsetXInPx = $canvasOffsetXInPx, canvasOffsetYInPx = $canvasOffsetYInPx")

        if (DEBUG_FRAMES) {
            for (col in startGridCol..endGridCol) {
                val lineX = col * gwInPx - canvasOffsetXInPx.toFloat()
                Logger.debug("lineX = $col * $gwInPx - ${canvasOffsetXInPx.toFloat()} = $lineX")
                val lineStartY =
                        startGridRow * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
                val lineEndY = endGridRow * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
                canvas.drawLine(lineX, lineStartY, lineX, lineEndY, GRID_LINE_PAINT)
            }

            for (row in startGridRow..endGridRow) {
                val lineY = row * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY
                val lineStartX =
                        startGridCol * gwInPx - canvasOffsetXInPx.toFloat()
                val lineEndX =
                        endGridCol * gwInPx - canvasOffsetYInPx.toFloat()
                canvas.drawLine(lineStartX, lineY, lineEndX, lineY, GRID_LINE_PAINT)
            }
        }

        val paint = Paint().apply {
            color = ResourcesCompatUtils.getColor(context,
                    R.color.colorAccent)
        }

//        for (grid in occupiedGrids) {
        synchronized(drawnGrids) {
            for (grid in drawnGrids) {
                val point = dumpGrid(grid)

                val lineX = point.x * gwInPx - canvasOffsetXInPx.toFloat()
                val lineY = point.y * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY

                canvas.drawRect(lineX, lineY, lineX + gwInPx, lineY + ghInPx, paint)
            }
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

    fun updateGrids(grids: Array<Array<Int>>) {
        occupiedGrids.clear()

        for (point in grids) {
            occupiedGrids.add("${point[0]}_${point[1]}")
        }

        restartDrawing()
    }

    fun dumpGrid(gridStr: String): Point {
        val parts = gridStr.split("_");

        return try {
            Point(parts[0].toInt(), parts[1].toInt())
        } catch (e: NumberFormatException) {
            Logger.error("failed to convert string [$gridStr] to point: $e")

            Point(0, 0)
        }
    }

    fun restartDrawing() {
        drawnGrids.clear()

        drawIndex = 0

        postDelayed(drawRunnable, DRAWING_DELAY)
    }

    fun drawStep() {
        val list = occupiedGrids.toList()
        if (drawIndex < list.size) {
            synchronized(drawnGrids) {
                drawnGrids.add(list[drawIndex++])
            }

            invalidate()
            postDelayed(drawRunnable, DRAWING_DELAY)
        }

    }

    private var drawIndex = 0

    private val drawRunnable = Runnable {
        drawStep()
    }
}