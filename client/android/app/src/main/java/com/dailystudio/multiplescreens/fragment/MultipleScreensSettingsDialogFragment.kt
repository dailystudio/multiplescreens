package com.dailystudio.multiplescreens.fragment

import android.content.Context
import android.graphics.drawable.Drawable
import com.dailystudio.devbricksx.settings.AbsSetting
import com.dailystudio.devbricksx.settings.AbsSettingsDialogFragment
import com.dailystudio.devbricksx.settings.EditSetting
import com.dailystudio.devbricksx.settings.SwitchSetting
import com.dailystudio.devbricksx.utils.ResourcesCompatUtils
import com.dailystudio.multiplescreens.MultipleScreensSettings
import com.dailystudio.multiplescreens.MultipleScreensSettingsPrefs
import com.dailystudio.multiplescreens.R

class MultipleScreensSettingsDialogFragment : AbsSettingsDialogFragment() {

    override fun createSettings(context: Context): Array<AbsSetting> {
        val debugFramesSetting = object : SwitchSetting(
                context,
                MultipleScreensSettingsPrefs.PREF_DEBUG_FRAMES,
                R.drawable.ic_setting_debug_frames,
                R.string.setting_debug_frames
        ) {
            override fun isOn(): Boolean {
                return MultipleScreensSettingsPrefs.instance.debugFrames
            }

            override fun setOn(on: Boolean) {
                MultipleScreensSettingsPrefs.instance.debugFrames = on
            }
        }

        val wsUrlSetting = object: EditSetting(
                context,
                MultipleScreensSettingsPrefs.PREF_WS_URL,
                R.drawable.ic_setting_ws_url,
                R.string.setting_ws_url
        ) {

            override fun getEditHint(context: Context): CharSequence? {
                return context.getString(R.string.setting_ws_url_hint)
            }

            override fun getEditButtonDrawable(context: Context): Drawable? {
                val drawable = ResourcesCompatUtils.getDrawable(context,
                        R.drawable.ic_action_reset)
                val tintColor = ResourcesCompatUtils.getColor(context,
                        R.color.colorPrimary)

                drawable?.setTint(tintColor)

                return drawable
            }

            override fun getEditText(context: Context): CharSequence? {
                return MultipleScreensSettingsPrefs.instance.wsUrl
            }

            override fun setEditText(context: Context, text: CharSequence?) {
                MultipleScreensSettingsPrefs.instance.wsUrl = text.toString()
            }

            override fun onEditButtonClicked(context: Context) {
                MultipleScreensSettingsPrefs.instance.wsUrl = MultipleScreensSettings.DEFAULT_WS_URL
            }

        }

        return arrayOf(debugFramesSetting,
                wsUrlSetting)
    }

    override fun getDialogThumbImageDrawable(): Drawable? {
        return ResourcesCompatUtils.getDrawable(requireContext(),
                R.drawable.app_thumb)
    }

}