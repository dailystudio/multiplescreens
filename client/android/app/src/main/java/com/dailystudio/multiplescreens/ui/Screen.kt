package com.dailystudio.multiplescreens.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.dailystudio.devbricksx.GlobalContextWrapper
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.ui.AbsSurfaceView
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.multiplescreens.MultipleScreensSettingsPrefs
import com.dailystudio.multiplescreens.R
import com.dailystudio.multiplescreens.utils.MetricsUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt


class Screen: AbsSurfaceView {

    companion object {
        val DRAWING_AREA_PAINT = Paint().apply {
            color = GlobalContextWrapper.context?.getColor(R.color.light_black) ?: Color.RED
            style = Paint.Style.FILL
        }

        val GRID_LINE_PAINT = Paint().apply {
            color = GlobalContextWrapper.context?.getColor(R.color.colorPrimary) ?: Color.BLUE
            style = Paint.Style.FILL
        }


        val CENTRAL_LINE_PAINT = Paint().apply {
            color = GlobalContextWrapper.context?.getColor(R.color.light_gray) ?: Color.GRAY
            style = Paint.Style.FILL
            strokeWidth = MetricsUtils.dpToPx(2).toFloat()
            pathEffect = DashPathEffect(
                    floatArrayOf(MetricsUtils.dpToPx(15).toFloat(), MetricsUtils.dpToPx(5).toFloat()),
                    0f)
        }
    }

    var gridWidthInDp: Int = 0
    var gridHeightInDp: Int = 0
    var drawingBoundInDp: Rect = Rect()

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
        setZOrderOnTop(false)
    }

    override fun drawingCanvas(canvas: Canvas) {
        val canvas = canvas ?: return
        val debugFrames = MultipleScreensSettingsPrefs.instance.debugFrames

        val empty = synchronized(drawingBoundInDp) { drawingBoundInDp.isEmpty }
        val widthInDp =  synchronized(gridWidthInDp) { if (gridWidthInDp == 0) width else gridWidthInDp }
        val heightInDp = synchronized(gridHeightInDp) { if (gridHeightInDp == 0) height else gridHeightInDp }
        val boundInDp = synchronized(drawingBoundInDp) {
            if (drawingBoundInDp.isEmpty) {
                Rect(0, 0, width, height)
            } else {
                drawingBoundInDp
            }
        }
        val grids = synchronized(drawnGrids) { drawnGrids.toList() }

        canvas.drawColor(ResourcesCompatUtils.getColor(context,
                R.color.mainColor))

        val drawingAreaWidth = MetricsUtils.dpToPx(boundInDp.width())
        val drawingAreaHeight = MetricsUtils.dpToPx(boundInDp.height())
        val drawingAreaX = 0
        val drawingAreaY = ((height - drawingAreaHeight) / 2.0f).roundToInt()

        val gwInPx = MetricsUtils.dpToPx(widthInDp)
        val ghInPx = MetricsUtils.dpToPx(heightInDp)

        if (debugFrames && !empty) {
            val drawingAreaRect = Rect(drawingAreaX, drawingAreaY,
                    drawingAreaX + drawingAreaWidth, drawingAreaY + drawingAreaHeight)

            canvas.drawRect(drawingAreaRect, DRAWING_AREA_PAINT)
        }

        canvas.drawLine(drawingAreaX.toFloat(),
                drawingAreaY + drawingAreaHeight / 2.0f,
                drawingAreaX.toFloat() + drawingAreaWidth,
                drawingAreaY + drawingAreaHeight / 2.0f, CENTRAL_LINE_PAINT)

        Logger.debug("drawingBoundInDp = $boundInDp, widthInDp = $widthInDp, heightInDp = $heightInDp")

        val startGridCol = floor(boundInDp.left / widthInDp.toFloat()).roundToInt()
        val startGridRow = floor(boundInDp.top / heightInDp.toFloat()).roundToInt()
        val endGridCol = ceil(boundInDp.right / widthInDp.toFloat()).roundToInt()
        val endGridRow = ceil(boundInDp.bottom / heightInDp.toFloat()).roundToInt()
//        Logger.debug("start grid: col = $startGridCol, row = $startGridRow")
//        Logger.debug("end grid: col = $endGridCol, row = $endGridRow")

        val canvasOffsetXInPx = MetricsUtils.dpToPx(boundInDp.left)
        val canvasOffsetYInPx = MetricsUtils.dpToPx(boundInDp.top)
//        Logger.debug("canvasOffsetXInPx = $canvasOffsetXInPx, canvasOffsetYInPx = $canvasOffsetYInPx")

        if (debugFrames && !empty) {
            for (col in startGridCol..endGridCol) {
                val lineX = col * gwInPx - canvasOffsetXInPx.toFloat()
//                Logger.debug("lineX = $col * $gwInPx - ${canvasOffsetXInPx.toFloat()} = $lineX")
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

        grids.forEach {
            val point = dumpGrid(it)

            val lineX = point.x * gwInPx - canvasOffsetXInPx.toFloat()
            val lineY = point.y * ghInPx - canvasOffsetYInPx.toFloat() + drawingAreaY

            canvas.drawRect(lineX, lineY, lineX + gwInPx, lineY + ghInPx, paint)
        }
    }

    fun clearDimension() {
        updateDimension(0, 0, null)
    }

    fun updateDimension(gridWidthInDp: Int, gridHeightInDp: Int,
                        drawingBoundInDp: Rect?) {
        this.gridWidthInDp = synchronized(this.gridWidthInDp) { gridWidthInDp }
        this.gridHeightInDp = synchronized(this.gridHeightInDp) { gridHeightInDp }
        this.drawingBoundInDp = synchronized(this.drawingBoundInDp) { drawingBoundInDp ?: Rect() }

        invalidate()
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

    fun drawPoint(point: Array<Int>) {
        synchronized(drawnGrids) {
            drawnGrids.add("${point[0]}_${point[1]}")
        }

        invalidate()
    }

    fun clearGrids() {
        synchronized(drawnGrids) {
            drawnGrids.clear()
        }
    }

    fun resetDrawing() {
        clearGrids()
        clearDimension()

        invalidate()
    }
}