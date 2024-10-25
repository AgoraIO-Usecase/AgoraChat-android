package io.agora.chatdemo.page.group

import androidx.core.content.ContextCompat
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.extensions.internal.parse
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatGroup
import io.agora.uikit.feature.group.EaseGroupDetailActivity
import io.agora.uikit.model.EaseMenuItem

class ChatGroupDetailActivity : EaseGroupDetailActivity(){

    override fun getDetailItem(): MutableList<EaseMenuItem>? {
        val list = super.getDetailItem()
        val voiceItem = EaseMenuItem(
            title = getString(R.string.menu_voice_call),
            resourceId = io.agora.uikit.R.drawable.ease_phone_pick,
            menuId = R.id.group_item_voice_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 2,
            resourceTintColor = ContextCompat.getColor(this, R.color.color_primary)
        )
        val videoItem = EaseMenuItem(
            title = getString(R.string.menu_video_call),
            resourceId = io.agora.uikit.R.drawable.ease_video_camera,
            menuId = R.id.group_item_video_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 2,
            resourceTintColor = ContextCompat.getColor(this, R.color.color_primary)
        )
        list?.add(voiceItem)
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: EaseMenuItem?, position: Int): Boolean {
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
        EaseIM.updateGroupInfo(listOf(group.parse()))
        super.fetchGroupDetailSuccess(group)
    }
}