package com.dailystudio.multiplescreens

import com.dailystudio.devbricksx.annotations.data.BooleanField
import com.dailystudio.devbricksx.annotations.data.DataStoreCompanion
import com.dailystudio.devbricksx.annotations.data.StringField

@DataStoreCompanion
class MultipleScreensSettings(@BooleanField(false)
                              val debugFrames: Boolean = false,
                              @StringField(DEFAULT_WS_URL)
                              val wsUrl: String = DEFAULT_WS_URL) {

    companion object {

        const val DEFAULT_WS_URL = "wss://multiplescreens.orangelabschina.cn:1809/screen"

    }
}