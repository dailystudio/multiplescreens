package com.dailystudio.multiplescreens

import com.dailystudio.devbricksx.annotations.PreferenceValue
import com.dailystudio.devbricksx.annotations.SharedPreference

@SharedPreference
class MultipleScreensSettings(@PreferenceValue(defaultValueStr = "false")
                              val debugFrames: Boolean = false,
                              @PreferenceValue(defaultValueStr = DEFAULT_WS_URL)
                              val wsUrl: String = DEFAULT_WS_URL) {

    companion object {

        const val DEFAULT_WS_URL = "wss://multiplescreens.orangelabschina.cn:1809/screen"

    }
}