package io.agora.chatdemo.common.extensions.internal

import io.agora.uikit.widget.EaseSwitchItemView


internal fun EaseSwitchItemView.setSwitchDefaultStyle(){
    setSwitchTarckDrawable(io.agora.uikit.R.drawable.ease_switch_track_selector)
    setSwitchThumbDrawable(io.agora.uikit.R.drawable.ease_switch_thumb_selector)
}