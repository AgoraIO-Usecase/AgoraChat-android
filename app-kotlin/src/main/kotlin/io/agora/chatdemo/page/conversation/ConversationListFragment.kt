package io.agora.chatdemo.page.conversation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.chatdemo.DemoHelper
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
import io.agora.uikit.feature.conversation.EaseConversationListFragment
import io.agora.uikit.model.EaseConversation
import io.agora.uikit.model.EaseEvent

class ConversationListFragment: EaseConversationListFragment() {

    private var isFirstLoadData = false
    private val presenceViewModel by lazy { ViewModelProvider(this)[PresenceViewModel::class.java] }
    private val presenceController by lazy { PresenceController(mContext,presenceViewModel) }

    override fun initData() {
        super.initData()
        initEventBus()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        binding?.titleConversations?.let {
            EaseIM.getConfig()?.avatarConfig?.setAvatarStyle(it.getLogoView())
            EaseIM.getConfig()?.avatarConfig?.setStatusStyle(it.getStatusView(),2.dpToPx(mContext),
                ContextCompat.getColor(mContext, R.color.demo_background))
            updateProfile()
            it.setTitleEndDrawable(R.drawable.conversation_title)
        }
    }

    private fun initEventBus() {
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

        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT + DemoConstant.EVENT_UPDATE_USER_SUFFIX).register(this) {
            if (it.isContactChange && it.message.isNullOrEmpty().not()) {
                binding?.listConversation?.notifyDataSetChanged()
            }
        }
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.ADD.name).register(viewLifecycleOwner) {
            if (it.isContactChange) {
                refreshData()
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding?.titleConversations?.setLogoClickListener {
            EaseIM.getCurrentUser()?.id?.let {
                presenceController.showPresenceStatusDialog(PresenceCache.getUserPresence(it))
            }
        }
    }

    private fun updateProfile(){
        binding?.titleConversations?.let { titlebar->
            EaseIM.getCurrentUser()?.let { profile->
                val presence = PresenceCache.getUserPresence(profile.id)
                presence?.let {
                    val logoStatus = EasePresenceUtil.getPresenceIcon(mContext,it)
                    ChatLog.d("ConversationListFragment","logoStatus $logoStatus")
                    titlebar.setLogoStatusMargin(end = -1, bottom = -1)
                    titlebar.setLogoStatus(logoStatus)
                    titlebar.getStatusView().visibility = View.VISIBLE
                    titlebar.setLogoStatusSize(resources.getDimensionPixelSize(R.dimen.em_title_bar_status_icon_size))
                }
                ChatLog.e("ConversationListFragment","updateProfile ${profile.id} ${profile.name} ${profile.avatar}")
                titlebar.setLogo(profile.avatar, io.agora.uikit.R.drawable.ease_default_avatar, 32.dpToPx(mContext))
                val layoutParams = titlebar.getLogoView()?.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.marginStart = 12.dpToPx(mContext)
                titlebar.getTitleView().let { text ->
                    text.text = ""
                }
            }
        }
    }


    override fun loadConversationListSuccess(userList: List<EaseConversation>) {
        if (!isFirstLoadData){
            fetchFirstVisibleData()
            isFirstLoadData = true
        }
    }

    private fun fetchFirstVisibleData(){
        binding?.listConversation?.let { layout->
            (layout.conversationList.layoutManager as? LinearLayoutManager)?.let { manager->
                layout.post {
                    val firstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
                    val visibleList = layout.getListAdapter()?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    val fetchList = visibleList?.filter { conv ->
                        val u = DemoHelper.getInstance().getDataModel().getUser(conv.conversationId)
                        (u == null || u.updateTimes == 0) && (u?.name.isNullOrEmpty() || u?.avatar.isNullOrEmpty())
                    }
                    fetchList?.let {
                        layout.fetchConvUserInfo(it)
                    }
                }
            }
        }
    }


}