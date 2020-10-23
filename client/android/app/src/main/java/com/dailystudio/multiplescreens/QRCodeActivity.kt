package com.dailystudio.multiplescreens

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.development.Logger
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRCodeActivity : AppCompatActivity() {

    companion object {

        const val EXTRA_SID = "sessionId"

    }

    private var sessionId: String? = null
    private var sessionQrCodeView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sessionId = intent.getStringExtra(EXTRA_SID)
        Logger.debug("generating qr code: sid = $sessionId")

        setupViews()
    }

    private fun setupViews() {
        sessionQrCodeView = findViewById(R.id.session_qr_code)
        sessionQrCodeView?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val sid = sessionId ?: return@launch

                val writer = QRCodeWriter()
                val bitmap: Bitmap? = try {
                    val bitMatrix = writer.encode(
                        sid, BarcodeFormat.QR_CODE,
                        MainActivity.QR_CODE_SIZE,
                        MainActivity.QR_CODE_SIZE
                    )

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
    }

}