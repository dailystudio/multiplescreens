package com.dailystudio.multiplescreens

import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.multiplescreens.service.CmdUpdateScreenInfo
import com.dailystudio.multiplescreens.service.Command
import com.dailystudio.multiplescreens.service.WSEndpoint
import com.dailystudio.multiplescreens.service.WSEndpointListener
import com.dailystudio.multiplescreens.ui.GridScreen
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var wsServer1: WSEndpoint
    private lateinit var wsServer2: WSEndpoint

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val gridScreen: GridScreen = findViewById(R.id.grid_screen)
        gridScreen?.addOnLayoutChangeListener { view, i, i2, i3, i4, i5, i6, i7, i8 ->
            reportScreenInfo(wsServer1)
//            reportScreenInfo(wsServer2, 0.8f)
        }
    }

    override fun onStart() {
        super.onStart()
        wsServer1 = WSEndpoint("screen01", UUID.randomUUID().toString(), wsEndpointListener)
        wsServer1.connect()
//        wsServer2 = WSEndpoint("screen01", "d2", wsEndpointListener)
//        wsServer2.connect()
    }

    override fun onStop() {
        super.onStop()

        wsServer1.disconnect()
//        wsServer2.disconnect()
    }

    private val wsEndpointListener = object: WSEndpointListener {

        override fun onConnected(endpoint: WSEndpoint) {
            if (endpoint == wsServer1) {
                reportScreenInfo(endpoint)
//            } else {
//                reportScreenInfo(endpoint, 0.8f)
            }
        }

        override fun onDisconnect(endpoint: WSEndpoint, code: Int) {
        }

        override fun onCommand(endpoint: WSEndpoint, command: Command) {
            Logger.debug("new command: $command")
            when (command) {
                is CmdUpdateScreenInfo -> {
                    if (endpoint == wsServer1) {
                        updateScreenGrids(
                            command.gridWidthInDp,
                            command.gridHeightInDp,
                            command.drawingBoundInDp
                        )
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
