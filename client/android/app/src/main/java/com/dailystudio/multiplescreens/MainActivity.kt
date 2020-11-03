package com.dailystudio.multiplescreens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.multiplescreens.api.*
import com.dailystudio.multiplescreens.fragment.AboutFragment
import com.dailystudio.multiplescreens.fragment.MultipleScreensSettingsDialogFragment
import com.dailystudio.multiplescreens.ui.Screen
import com.dailystudio.multiplescreens.utils.MetricsUtils
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.android.Intents
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    companion object {
        const val QR_CODE_SIZE = 512
    }

    private var hostBtn: Button? = null
    private var joinBtn: Button? = null
    private var startBtn: MaterialButton? = null
    private var shareBtn: MaterialButton? = null
    private var exitBtn: Button? = null
    private var aboutBtn: Button? = null
    private var settingsBtn: View? = null

    private var groupStart: View? = null
    private var groupPlay: View? = null

    private var screen: Screen? = null
    private var sessionQrCodeView: ImageView? = null
    private var qrCodeLayout: View? = null

    private var wsServer: WSEndpoint? = null

    private val mySessionId = "ms-${(1..1000).shuffled().first()}"
    private val uuid: String = UUID.randomUUID().toString()

    private var isDrawing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        screen = findViewById(R.id.grid_screen)

        qrCodeLayout = findViewById(R.id.qr_code_layout)
        qrCodeLayout?.setOnClickListener {
            it.visibility = View.GONE
        }

        sessionQrCodeView = findViewById(R.id.session_qr_code)
        sessionQrCodeView?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val writer = QRCodeWriter()
                val bitmap: Bitmap? = try {
                    val bitMatrix = writer.encode(
                            mySessionId, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE)

                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val pixels = IntArray(width * height)
                    for (y in 0 until height) {
                        val offset = y * width
                        for (x in 0 until width) {
                            pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                        }
                    }

                    Bitmap.createBitmap(width, height,
                            Bitmap.Config.ARGB_8888).apply {
                        setPixels(pixels, 0, width, 0, 0, width, height)
                    }
                } catch (e: WriterException) {
                    Logger.error("generate session qr code failed: $e")

                    null
                }

                withContext(Dispatchers.Main) {
                    sessionQrCodeView?.setImageBitmap(bitmap)
                }
            }
        }

        groupStart = findViewById(R.id.group_start)
        groupPlay = findViewById(R.id.group_play)

        hostBtn = findViewById(R.id.btn_host)
        hostBtn?.setOnClickListener {
            connect(mySessionId)
            screen?.resetDrawing()

            groupStart?.visibility = View.GONE
            groupPlay?.visibility = View.VISIBLE
            startBtn?.isEnabled = true
            shareBtn?.isEnabled = true
        }

        joinBtn = findViewById(R.id.btn_join)
        joinBtn?.setOnClickListener {
            scanSessionId()
        }

        exitBtn = findViewById(R.id.btn_exit)
        exitBtn?.setOnClickListener {
            stopDrawing()
            disconnect()
            screen?.resetDrawing()

            groupStart?.visibility = View.VISIBLE
            groupPlay?.visibility = View.GONE
        }

        startBtn = findViewById(R.id.btn_start)
        startBtn?.setOnClickListener {
            if (!isDrawing) {
                startDrawing()
            } else {
                stopDrawing()
            }
        }

        shareBtn = findViewById(R.id.btn_share)
        shareBtn?.setOnClickListener {
            qrCodeLayout?.visibility = View.VISIBLE
        }

        aboutBtn = findViewById(R.id.btn_about)
        aboutBtn?.setOnClickListener {
            val fragment = AboutFragment()

            fragment.show(supportFragmentManager, "about")
        }

        settingsBtn = findViewById(R.id.btn_settings)
        settingsBtn?.setOnClickListener {
            val fragment = MultipleScreensSettingsDialogFragment()

            fragment.show(supportFragmentManager, "settings")
        }
    }

    override fun onStop() {
        super.onStop()

        screen?.resetDrawing()
        disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            val urlFromQRCode: String? = scanResult.contents
            Logger.debug("session id from QR code: $urlFromQRCode")
            urlFromQRCode?.let {
                connect(urlFromQRCode)
                screen?.resetDrawing()

                groupStart?.visibility = View.GONE
                groupPlay?.visibility = View.VISIBLE
                startBtn?.isEnabled = false
                shareBtn?.isEnabled = false
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun connect(sid: String) {
        wsServer = WSEndpoint(sid,
                uuid,
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

                is CmdDrawPoint -> {
                    lifecycleScope.launch(Dispatchers.Main) {
                        drawPoint(command.point)
                    }
                }
            }
        }
    }

    private fun scanSessionId() {
        val intent = Intent(this,
                CaptureActivityPortrait::class.java)

        intent.action = Intents.Scan.ACTION
        intent.putExtra(Intents.Scan.ORIENTATION_LOCKED, true)
        intent.putExtra(Intents.Scan.BEEP_ENABLED, true)
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE,
                getString(R.string.prompt_join_session))

        startActivityForResult(intent, IntentIntegrator.REQUEST_CODE)
    }

    private fun startDrawing() {
        wsServer?.startDrawing()
        isDrawing = true
        startBtn?.text = getString(R.string.label_stop)
        startBtn?.icon = ResourcesCompatUtils.getDrawable(this,
                R.drawable.ic_stop)
    }

    private fun stopDrawing() {
        wsServer?.stopDrawing()

        isDrawing = false
        startBtn?.text = getString(R.string.label_start)
        startBtn?.icon = ResourcesCompatUtils.getDrawable(this,
                R.drawable.ic_play)
    }

    private fun updateScreenGrids(gridWidthInDp: Int,
                                  gridHeightInDp: Int,
                                  drawingBoundInDp: Rect?
    ) {
        val screen: Screen = findViewById(R.id.grid_screen) ?: return

        screen.updateDimension(
                gridWidthInDp, gridHeightInDp,
                drawingBoundInDp)
    }

    private fun drawPoint(point: Array<Int>) {
        val screen: Screen = findViewById(R.id.grid_screen) ?: return

        screen.drawPoint(point)
    }

    private fun reportScreenInfo(endpoint: WSEndpoint?, debugFactor: Float = 1.0f) {
        val screen: Screen = findViewById(R.id.grid_screen) ?: return

        val widthInDp = MetricsUtils.pxToDp((screen.width * debugFactor).roundToInt())
        val heightInDp = MetricsUtils.pxToDp((screen.height * debugFactor).roundToInt())

        Logger.debug("[${widthInDp}dp x ${heightInDp}dp]")

        endpoint?.reportScreenInfo(widthInDp, heightInDp)
    }
}
