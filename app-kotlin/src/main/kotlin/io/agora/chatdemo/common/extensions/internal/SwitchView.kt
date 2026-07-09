package io.agora.chatdemo.common.extensions.internal

import io.agora.chat.uikit.widget.ChatUIKitSwitchItemView


internal fun ChatUIKitSwitchItemView.setSwitchDefaultStyle(){
    setSwitchTarckDrawable(io.agora.chat.uikit.R.drawable.uikit_switch_track_selector)
    setSwitchThumbDrawable(io.agora.chat.uikit.R.drawable.uikit_switch_thumb_selector)
}