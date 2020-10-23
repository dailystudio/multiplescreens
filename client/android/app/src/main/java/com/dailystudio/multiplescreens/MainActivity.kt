package com.dailystudio.multiplescreens

import android.graphics.Rect
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.multiplescreens.service.*
import com.dailystudio.multiplescreens.ui.GridScreen
import com.dailystudio.multiplescreens.utils.MetricsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    companion object {
        val RANDOM = Random(System.currentTimeMillis())
    }

    private var startBtn: Button? = null
    private var gridScreen: GridScreen? = null

    private var wsServer: WSEndpoint? = null

//    private var sessionId: String = "ms-${RANDOM.nextInt()}"
    private var sessionId: String = "ms-01"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        gridScreen = findViewById(R.id.grid_screen)
        gridScreen?.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            connect()
        }

        startBtn = findViewById(R.id.btn_start)
        startBtn?.setOnClickListener {
            startDrawing()
        }
    }

    override fun onStop() {
        super.onStop()

        disconnect()
    }

    private fun connect() {
        wsServer = WSEndpoint(sessionId,
                UUID.randomUUID().toString(),
                wsEndpointListener)
        wsServer?.connect()
    }

    private fun disconnect() {
        wsServer?.disconnect()
    }

    private val wsEndpointListener = object: WSEndpointListener {

        override fun onConnected(endpoint: WSEndpoint) {
            reportScreenInfo(endpoint)
        }

        override fun onDisconnect(endpoint: WSEndpoint, code: Int) {
        }

        override fun onCommand(endpoint: WSEndpoint, command: Command) {
            Logger.debug("new command: $command")
            when (command) {
                is CmdUpdateScreenInfo -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateScreenGrids(
                            command.gridWidthInDp,
                            command.gridHeightInDp,
                            command.drawingBoundInDp
                        )
                    }
                }

                is CmdGridsMap -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateGridsMap(command.map)
                    }
                }

                is CmdDrawPoint -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        drawPoint(command.point)
                    }
                }
            }
        }
    }

    private fun startDrawing() {
        wsServer?.startDrawing()
    }

    private fun updateScreenGrids(gridWidthInDp: Int,
                                  gridHeightInDp: Int,
                                  drawingBoundInDp: Rect?
    ) {
        val gridScreen: GridScreen = findViewById(R.id.grid_screen) ?: return

        gridScreen.updateDimension(
            gridWidthInDp, gridHeightInDp,
            drawingBoundInDp)
    }

    private fun updateGridsMap(map: Array<Array<Int>>) {
        val gridScreen: GridScreen = findViewById(R.id.grid_screen) ?: return

        gridScreen.updateGrids(map)
    }

    private fun drawPoint(point: Array<Int>) {
        val gridScreen: GridScreen = findViewById(R.id.grid_screen) ?: return

        gridScreen.drawPoint(point)
    }

    private fun reportScreenInfo(endpoint: WSEndpoint?, debugFactor:Float = 1.0f) {
        val gridScreen: GridScreen = findViewById(R.id.grid_screen) ?: return

        val widthInDp = MetricsUtils.pxToDp((gridScreen.width * debugFactor).roundToInt())
        val heightInDp = MetricsUtils.pxToDp((gridScreen.height * debugFactor).roundToInt())

        Logger.debug("[${widthInDp}dp x ${heightInDp}dp]")

        endpoint?.reportScreenInfo(widthInDp, heightInDp)
    }
}
