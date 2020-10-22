package com.dailystudio.multiplescreens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dailystudio.multiplescreens.service.WSServe

class MainActivity : AppCompatActivity() {

    private val wsServer = WSServe("screen01", "123")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        wsServer.connect()
    }

    override fun onStop() {
        super.onStop()

        wsServer.disconnect()
    }
}
