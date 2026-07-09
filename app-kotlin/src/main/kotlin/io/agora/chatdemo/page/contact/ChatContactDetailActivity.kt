package io.agora.chatdemo.page.contact

import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PresenceCache
import io.agora.chatdemo.common.room.entity.parse
import io.agora.chatdemo.common.room.extensions.parseToDbBean
import io.agora.chatdemo.feature.presence.interfaces.IPresenceResultView
import io.agora.chatdemo.feature.presence.utils.EasePresenceUtil
import io.agora.chatdemo.feature.presence.viewmodel.PresenceViewModel
import io.agora.chatdemo.viewmodel.ProfileInfoViewModel
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatPresence
import io.agora.chat.uikit.common.ChatUserInfoType
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.feature.contact.ChatUIKitContactDetailsActivity
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import kotlinx.coroutines.launch


class ChatContactDetailActivity: ChatUIKitContactDetailsActivity(), IPresenceResultView {
    private lateinit var model: ProfileInfoViewModel
    private lateinit var presenceModel: PresenceViewModel

    companion object {
        private const val TAG = "ChatContactDetailActivity"
    }

    override fun initView() {
        super.initView()
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
        presenceModel = ViewModelProvider(this)[PresenceViewModel::class.java]
        presenceModel.attachView(this)
        updateUserInfo()
    }

    override fun initEvent() {
        super.initEvent()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange ) {
                updatePresence()
            }
        }
    }

    override fun initData() {
        super.initData()
        user?.let {
            presenceModel.fetchChatPresence(mutableListOf(it.userId))
        }
        lifecycleScope.launch {
            user?.let { user->
                model.fetchUserInfoAttribute(listOf(user.userId), listOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                    .catchChatException {
                        ChatLog.e("ContactDetail", "fetchUserInfoAttribute error: ${it.description}")
                    }
                    .collect {
                        it[user.userId]?.parseToDbBean()?.let {u->
                            u.parse().apply {
                                ChatUIKitClient.updateUsersInfo(mutableListOf(this))
                                DemoHelper.getInstance().getDataModel().insertUser(this)
                            }
                            updateUserInfo()
                            notifyUpdateRemarkEvent()
                        }
                    }
            }
        }
    }

    private fun updateUserInfo() {
        DemoHelper.getInstance().getDataModel().getUser(user?.userId)?.let {
            binding.epPresence.setUserAvatarData(it.parse())
            binding.tvName.text = it.parse().getNotEmptyName()
            binding.tvNumber.text = it.userId
        }
    }

    override fun getDetailItem(): MutableList<ChatUIKitMenuItem>? {
        val list = super.getDetailItem()
        val audioItem = ChatUIKitMenuItem(
            title = getString(R.string.detail_item_audio),
            resourceId = io.agora.chat.uikit.R.drawable.uikit_phone_pick,
            menuId = R.id.contact_item_audio_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 2
        )

        val videoItem = ChatUIKitMenuItem(
            title = getString(R.string.detail_item_video),
            resourceId = io.agora.chat.uikit.R.drawable.uikit_video_camera,
            menuId = R.id.contact_item_video_call,
            titleColor = ContextCompat.getColor(this, R.color.color_primary),
            order = 3
        )
        list?.add(audioItem)
        list?.add(videoItem)
        return list
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {
            when(item.menuId){
                R.id.contact_item_audio_call -> {
                    CallKitManager.startSingleAudioCall(user?.userId)
                    return true
                }
                R.id.contact_item_video_call -> {
                    CallKitManager.startSingleVideoCall(user?.userId)
                    return true
                }
                else -> {
                    return super.onMenuItemClick(item, position)
                }
            }
        }
        return false
    }


    private fun notifyUpdateRemarkEvent() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX)
            .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_USER_SUFFIX, ChatUIKitEvent.TYPE.CONTACT, user?.userId))
    }

    private fun updatePresence(){
        val map = PresenceCache.getPresenceInfo
        user?.let { user->
            map.let {
                binding.epPresence.getStatusView().visibility = View.VISIBLE
                binding.epPresence.setUserAvatarData(user.toProfile(),
                    EasePresenceUtil.getPresenceString(mContext,it[user.userId]))
            }
        }
    }

    override fun fetchChatPresenceSuccess(presence: MutableList<ChatPresence>) {
        ChatLog.e(TAG,"fetchChatPresenceSuccess $presence")
        updatePresence()
    }

    override fun fetchChatPresenceFail(code: Int, error: String) {
        ChatLog.e(TAG,"fetchChatPresenceFail $code $error")
    }

}