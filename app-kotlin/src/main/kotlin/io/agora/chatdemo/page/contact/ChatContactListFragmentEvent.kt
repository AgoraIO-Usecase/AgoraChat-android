package io.agora.chatdemo.page.contact

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import io.agora.chatdemo.R
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PresenceCache
import io.agora.chatdemo.feature.presence.controller.PresenceController
import io.agora.chatdemo.feature.presence.utils.EasePresenceUtil
import io.agora.chatdemo.feature.presence.viewmodel.PresenceViewModel
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.configs.setStatusStyle
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatContactListFragmentEvent : ChatUIKitContactsListFragment() {

    private var isFirstLoadData = false
    private val presenceViewModel by lazy { ViewModelProvider(this)[PresenceViewModel::class.java] }
    private val presenceController by lazy { PresenceController(mContext, presenceViewModel) }
    companion object{
        private val TAG = ChatContactListFragmentEvent::class.java.simpleName
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.titleContact?.let {
            it.setTitle("")
            it.setTitleEndDrawable(R.drawable.contact_title)
        }
        updateProfile()
    }

    override fun initData() {
        super.initData()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX).register(this) {
            if (it.isContactChange && it.message.isNullOrEmpty().not()) {
                binding?.listContact?.loadContactData(false)
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updateProfile()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(ChatUIKitClient.getCurrentUser()?.id) ) {
                updateProfile()
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding?.titleContact?.setLogoClickListener {
            ChatUIKitClient.getCurrentUser()?.id?.let {
                presenceController.showPresenceStatusDialog(PresenceCache.getUserPresence(it))
            }
        }
    }

    private fun updateProfile(){
        binding?.titleContact?.let { titlebar->
            ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(titlebar.getLogoView())
            ChatUIKitClient.getConfig()?.avatarConfig?.setStatusStyle(titlebar.getStatusView(),2.dpToPx(mContext),
                ContextCompat.getColor(mContext, R.color.demo_background))

            ChatUIKitClient.getCurrentUser()?.let { profile->
                val presence = PresenceCache.getUserPresence(profile.id)
                presence?.let {
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,it)
                    titlebar.setLogoStatusMargin(end = -1, bottom = -1)
                    titlebar.setLogoStatus(logoStatus)
                    titlebar.getStatusView().visibility = View.VISIBLE
                    titlebar.setLogoStatusSize(resources.getDimensionPixelSize(R.dimen.em_title_bar_status_icon_size))
                }
                ChatLog.e(TAG,"updateProfile ${profile.id} ${profile.name} ${profile.avatar}")
                titlebar.setLogo(profile.avatar, io.agora.chat.uikit.R.drawable.uikit_default_avatar, 32.dpToPx(mContext))
                val layoutParams = titlebar.getLogoView()?.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.marginStart = 12.dpToPx(mContext)
                titlebar.getTitleView().let { text ->
                    text.text = ""
                }
            }
        }
    }

    override fun loadContactListSuccess(userList: MutableList<ChatUIKitUser>) {
        super.loadContactListSuccess(userList)
        if (!isFirstLoadData){
            fetchContactInfo(userList)
            isFirstLoadData = true
        }
    }

    override fun loadContactListFail(code: Int, error: String) {
        super.loadContactListFail(code, error)
        ChatLog.e(TAG,"loadContactListFail: $code $error")
    }

    class Builder:ChatUIKitContactsListFragment.Builder() {
        override fun build(): ChatUIKitContactsListFragment {
            if (customFragment == null) {
                customFragment = ChatContactListFragmentEvent()
            }
            if (customFragment is ChatContactListFragmentEvent){

            }
            return super.build()
        }
    }

}