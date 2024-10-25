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
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.extensions.dpToPx
import io.agora.uikit.configs.setAvatarStyle
import io.agora.uikit.configs.setStatusStyle
import io.agora.uikit.feature.contact.EaseContactsListFragment
import io.agora.uikit.model.EaseEvent
import io.agora.uikit.model.EaseUser

class ChatContactListFragmentEvent : EaseContactsListFragment() {

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
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX).register(this) {
            if (it.isContactChange && it.message.isNullOrEmpty().not()) {
                binding?.listContact?.loadContactData(false)
            }
        }
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updateProfile()
            }
        }
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(EaseIM.getCurrentUser()?.id) ) {
                updateProfile()
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding?.titleContact?.setLogoClickListener {
            EaseIM.getCurrentUser()?.id?.let {
                presenceController.showPresenceStatusDialog(PresenceCache.getUserPresence(it))
            }
        }
    }

    private fun updateProfile(){
        binding?.titleContact?.let { titlebar->
            EaseIM.getConfig()?.avatarConfig?.setAvatarStyle(titlebar.getLogoView())
            EaseIM.getConfig()?.avatarConfig?.setStatusStyle(titlebar.getStatusView(),2.dpToPx(mContext),
                ContextCompat.getColor(mContext, R.color.demo_background))

            EaseIM.getCurrentUser()?.let { profile->
                val presence = PresenceCache.getUserPresence(profile.id)
                presence?.let {
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,it)
                    titlebar.setLogoStatusMargin(end = -1, bottom = -1)
                    titlebar.setLogoStatus(logoStatus)
                    titlebar.getStatusView().visibility = View.VISIBLE
                    titlebar.setLogoStatusSize(resources.getDimensionPixelSize(R.dimen.em_title_bar_status_icon_size))
                }
                ChatLog.e(TAG,"updateProfile ${profile.id} ${profile.name} ${profile.avatar}")
                titlebar.setLogo(profile.avatar, io.agora.uikit.R.drawable.ease_default_avatar, 32.dpToPx(mContext))
                val layoutParams = titlebar.getLogoView()?.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.marginStart = 12.dpToPx(mContext)
                titlebar.getTitleView().let { text ->
                    text.text = ""
                }
            }
        }
    }

    override fun loadContactListSuccess(userList: MutableList<EaseUser>) {
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

    class Builder:EaseContactsListFragment.Builder() {
        override fun build(): EaseContactsListFragment {
            if (customFragment == null) {
                customFragment = ChatContactListFragmentEvent()
            }
            if (customFragment is ChatContactListFragmentEvent){

            }
            return super.build()
        }
    }

}