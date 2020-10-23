package com.dailystudio.multiplescreens

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.multiplescreens.service.*
import com.dailystudio.multiplescreens.ui.GridScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var wsServer1: WSEndpoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val gridScreen: GridScreen? = findViewById(R.id.grid_screen)
        gridScreen?.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
            reportScreenInfo(wsServer1)
        }
    }

    override fun onStart() {
        super.onStart()
        wsServer1 = WSEndpoint("screen01", UUID.randomUUID().toString(), wsEndpointListener)
        wsServer1.connect()
    }

    override fun onStop() {
        super.onStop()

        wsServer1.disconnect()
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
            }
        }
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

    private fun reportScreenInfo(endpoint: WSEndpoint?, debugFactor:Float = 1.0f) {
        val gridScreen: GridScreen = findViewById(R.id.grid_screen) ?: return

        val widthInDp = pxToDp((gridScreen.width * debugFactor).roundToInt())
        val heightInDp = pxToDp((gridScreen.height * debugFactor).roundToInt())

        Logger.debug("[${widthInDp}dp x ${heightInDp}dp]")

        endpoint?.reportScreenInfo(widthInDp, heightInDp)
    }

    fun pxToDp(px: Int): Int {
        return (px / resources.displayMetrics.density).roundToInt()
    }
}
