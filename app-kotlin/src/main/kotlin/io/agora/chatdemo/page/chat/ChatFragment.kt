package io.agora.chatdemo.page.chat

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PresenceCache
import io.agora.chatdemo.common.helper.MenuFilterHelper
import io.agora.chatdemo.feature.presence.interfaces.IPresenceRequest
import io.agora.chatdemo.feature.presence.interfaces.IPresenceResultView
import io.agora.chatdemo.feature.presence.utils.EasePresenceUtil
import io.agora.chatdemo.feature.presence.viewmodel.PresenceViewModel
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatPresence
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.feature.chat.EaseChatFragment
import io.agora.uikit.feature.chat.enums.EaseChatType
import io.agora.uikit.feature.chat.widgets.EaseChatLayout
import io.agora.uikit.menu.chat.EaseChatMenuHelper
import io.agora.uikit.model.EaseEvent

class ChatFragment: EaseChatFragment() , IPresenceResultView {
    private var presenceViewModel: IPresenceRequest? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.titleBar?.inflateMenu(R.menu.demo_chat_menu)
        updatePresence()
    }

    override fun initEventBus() {
        super.initEventBus()
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX).register(this) {
            if (it.isContactChange && it.message.isNullOrEmpty().not()) {
                val userId = it.message
                if (chatType == EaseChatType.SINGLE_CHAT && userId == conversationId) {
                    setDefaultHeader(true)
                }
                binding?.layoutChat?.chatMessageListLayout?.refreshMessages()
            }
        }
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(conversationId) ) {
                updatePresence()
            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        presenceViewModel = ViewModelProvider(this)[PresenceViewModel::class.java]
        presenceViewModel?.attachView(this)
    }

    override fun initData() {
        super.initData()
        conversationId?.let {
            if (it != EaseIM.getCurrentUser()?.id){
                presenceViewModel?.fetchChatPresence(mutableListOf(it))
                presenceViewModel?.subscribePresences(mutableListOf(it))
            }
        }
    }

    override fun setMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.chat_menu_video_call -> {
                showVideoCall()
                return true
            }
        }
        return super.setMenuItemClick(item)
    }

    private fun showVideoCall() {
        CallKitManager.showSelectDialog(chatType, mContext, conversationId)
    }

    override fun onPreMenu(helper: EaseChatMenuHelper?, message: ChatMessage?) {
        super.onPreMenu(helper, message)
        MenuFilterHelper.filterMenu(helper, message)
    }

    private fun updatePresence(){
        if (chatType == EaseChatType.SINGLE_CHAT){
            conversationId?.let {
                val presence = PresenceCache.getUserPresence(it)
                presence?.let {
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,presence)
                    val subtitle = EasePresenceUtil.getPresenceString(mContext,presence)
                    binding?.run{
                        titleBar.setLogoStatusMargin(end = -1, bottom = -1)
                        titleBar.setLogoStatus(logoStatus)
                        titleBar.setSubtitle(subtitle)
                        titleBar.getStatusView().visibility = View.VISIBLE
                        titleBar.setLogoStatusSize(resources.getDimensionPixelSize(R.dimen.em_title_bar_status_icon_size))
                    }
                }
            }
        }
    }

    override fun onPeerTyping(action: String?) {
        if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_BEGIN)) {
            binding?.titleBar?.setSubtitle(getString(io.agora.uikit.R.string.alert_during_typing))
            binding?.titleBar?.visibility = View.VISIBLE
        } else if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_END)) {
            updatePresence()
        }
    }


    override fun onDestroy() {
        conversationId?.let {
            if (it != EaseIM.getCurrentUser()?.id){
                presenceViewModel?.unsubscribePresences(mutableListOf(it))
            }
        }
        super.onDestroy()
    }

    override fun fetchChatPresenceSuccess(presence: MutableList<ChatPresence>) {
        updatePresence()
    }
}