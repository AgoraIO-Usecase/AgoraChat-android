package io.agora.chatdemo.page.group

import androidx.core.content.ContextCompat
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.extensions.internal.parse
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.feature.group.ChatUIKitGroupDetailActivity
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class ChatGroupDetailActivity : ChatUIKitGroupDetailActivity(){

    override fun getDetailItem(): MutableList<ChatUIKitMenuItem>? {
        val list = super.getDetailItem()
        val voiceItem = ChatUIKitMenuItem(
            title = getString(R.string.menu_voice_call),
            resourceId = io.agora.chat.uikit.R.drawable.uikit_phone_pick,
            menuId = R.id.group_item_voice_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 2,
            resourceTintColor = ContextCompat.getColor(this, R.color.color_primary)
        )
        val videoItem = ChatUIKitMenuItem(
            title = getString(R.string.menu_video_call),
            resourceId = io.agora.chat.uikit.R.drawable.uikit_video_camera,
            menuId = R.id.group_item_video_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 2,
            resourceTintColor = ContextCompat.getColor(this, R.color.color_primary)
        )
        list?.add(voiceItem)
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {menu->
            return when(menu.menuId){
                R.id.group_item_video_call -> {
                    CallKitManager.startConferenceCall(EaseCallType.CONFERENCE_VIDEO_CALL,this, groupId)
                    true
                }
                R.id.group_item_voice_call -> {
                    CallKitManager.startConferenceCall(EaseCallType.CONFERENCE_VOICE_CALL,this, groupId)
                    true
                }
                else -> {
                    super.onMenuItemClick(item, position)
                }
            }
        }
        return false
    }

    override fun fetchGroupDetailSuccess(group: ChatGroup) {
        ChatUIKitClient.updateGroupInfo(listOf(group.parse()))
        super.fetchGroupDetailSuccess(group)
    }
}