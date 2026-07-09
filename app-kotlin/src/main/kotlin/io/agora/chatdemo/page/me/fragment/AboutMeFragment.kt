package io.agora.chatdemo.page.me.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chatdemo.DemoApplication
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.PresenceCache
import io.agora.chatdemo.databinding.DemoFragmentAboutMeBinding
import io.agora.chatdemo.feature.presence.controller.PresenceController
import io.agora.chatdemo.feature.presence.interfaces.IPresenceResultView
import io.agora.chatdemo.feature.presence.utils.EasePresenceUtil
import io.agora.chatdemo.feature.presence.viewmodel.PresenceViewModel
import io.agora.chatdemo.page.login.LoginActivity
import io.agora.chatdemo.page.login.viewModel.LoginViewModel
import io.agora.chatdemo.page.me.activity.AboutActivity
import io.agora.chatdemo.page.me.activity.CurrencyActivity
import io.agora.chatdemo.page.me.activity.NotifyActivity
import io.agora.chatdemo.page.me.activity.UserInformationActivity
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatPresence
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.configs.setStatusStyle
import io.agora.chat.uikit.feature.contact.ChatUIKitBlockListActivity
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.widget.ChatUIKitCustomAvatarView
import kotlinx.coroutines.launch

class AboutMeFragment: ChatUIKitBaseFragment<DemoFragmentAboutMeBinding>(), View.OnClickListener,
    ChatUIKitCustomAvatarView.OnPresenceClickListener, IPresenceResultView {

    /**
     * The clipboard manager.
     */
    private val clipboard by lazy { mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private lateinit var loginViewModel: LoginViewModel

    private val presenceViewModel by lazy { ViewModelProvider(this)[PresenceViewModel::class.java] }
    private val presenceController by lazy { PresenceController(mContext,presenceViewModel) }

    companion object{
        private val TAG = AboutMeFragment::class.java.simpleName
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoFragmentAboutMeBinding {
        return DemoFragmentAboutMeBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        initPresence()
        initStatus()
    }
    override fun initViewModel() {
        super.initViewModel()
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        presenceViewModel.attachView(this)
    }

    override fun initListener() {
        super.initListener()
        binding?.run {
            epPresence.setOnPresenceClickListener(this@AboutMeFragment)
            tvNumber.setOnClickListener(this@AboutMeFragment)
            itemPresence.setOnClickListener(this@AboutMeFragment)
            itemInformation.setOnClickListener(this@AboutMeFragment)
            itemCurrency.setOnClickListener(this@AboutMeFragment)
            itemNotify.setOnClickListener(this@AboutMeFragment)
            itemPrivacy.setOnClickListener(this@AboutMeFragment)
            itemAbout.setOnClickListener(this@AboutMeFragment)
            aboutMeLogout.setOnClickListener(this@AboutMeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun initData() {
        super.initData()
        fetchCurrentPresence()
        initEvent()
    }

    private fun initEvent() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(ChatUIKitClient.getCurrentUser()?.id) ) {
                updatePresence()
            }
        }

        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updatePresence(true)
            }
        }
    }

    private fun initPresence(){
        binding?.run {
            var name:String? = ChatClient.getInstance().currentUser
            val id = getString(R.string.main_about_me_id,ChatClient.getInstance().currentUser?:"")
            ChatUIKitClient.getConfig()?.avatarConfig?.setStatusStyle(epPresence.getStatusView(),4.dpToPx(mContext),
                ContextCompat.getColor(mContext, R.color.demo_background))
            epPresence.setPresenceStatusMargin(end = -4, bottom = -4)
            epPresence.setPresenceStatusSize(resources.getDimensionPixelSize(io.agora.chat.uikit.R.dimen.ease_contact_status_icon_size))

            val layoutParams = epPresence.getUserAvatar().layoutParams
            layoutParams.width = 100.dpToPx(mContext)
            layoutParams.height = 100.dpToPx(mContext)
            epPresence.getUserAvatar().layoutParams = layoutParams

            ChatUIKitClient.getCurrentUser()?.let {
                epPresence.setUserAvatarData(it)
                name = it.getRemarkOrName()
            }
            tvName.text = name
            tvNumber.text = id
        }
    }

    private fun updatePresence(isRefreshAvatar:Boolean = false){
        ChatUIKitClient.getCurrentUser()?.let { user->
            val presence = PresenceCache.getUserPresence(user.id)
            presence?.let {
                if (isRefreshAvatar){
                    binding?.epPresence?.setUserAvatarData(user)
                }else{
                    binding?.epPresence?.setUserStatusData(EasePresenceUtil.getPresenceIcon(mContext,it))
                    binding?.epPresence?.getStatusView()?.visibility = View.VISIBLE
                    val subtitle = EasePresenceUtil.getPresenceString(mContext,it)
                    binding?.itemPresence?.setContent(subtitle)
                }
            }?:kotlin.run {
                binding?.epPresence?.setUserAvatarData(user)
            }
            binding?.tvName?.text = user.getNotEmptyName()
        }
    }

    private fun initStatus(){
        val isSilent = ChatUIKitClient.checkMutedConversationList(ChatClient.getInstance().currentUser)
        if (isSilent) {
            binding?.icNotice?.visibility = View.VISIBLE
        }else{
            binding?.icNotice?.visibility = View.GONE
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            loginViewModel.logout()
                .catchChatException { e ->
                    ChatLog.e(TAG, "logout failed: ${e.description}")
                }
                .collect {
                    DemoHelper.getInstance().getDataModel().clearCache()
                    PresenceCache.clear()
                    DemoApplication.getInstance().getLifecycleCallbacks().skipToTarget(
                        LoginActivity::class.java)
                }
        }
    }


    private fun fetchCurrentPresence(){
        presenceViewModel.fetchPresenceStatus(mutableListOf(ChatClient.getInstance().currentUser))
    }

    override fun onPresenceClick(v: View?) {

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.item_presence -> {
                ChatUIKitClient.getCurrentUser()?.id?.let {
                    presenceController.showPresenceStatusDialog(PresenceCache.getUserPresence(it))
                }
            }
            R.id.item_information -> {
                startActivity(Intent(mContext, UserInformationActivity::class.java))
            }
            R.id.item_currency -> {
                startActivity(Intent(mContext, CurrencyActivity::class.java))
            }
            R.id.item_notify -> {
                startActivity(Intent(mContext, NotifyActivity::class.java))
            }
            R.id.item_privacy -> {
                startActivity(Intent(mContext, ChatUIKitBlockListActivity::class.java))
            }
            R.id.item_about -> {
                startActivity(Intent(mContext, AboutActivity::class.java))
            }
            R.id.about_me_logout -> {
                showLogoutDialog()
            }
            R.id.tv_number -> {
                val indexOfSpace = binding?.tvNumber?.text?.indexOf(":")
                indexOfSpace?.let {
                    if (indexOfSpace != -1) {
                        val substring = binding?.tvNumber?.text?.substring(indexOfSpace + 1)
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(
                                null,
                                substring
                            )
                        )
                    }
                }

            }
            else -> {}
        }
    }

    private fun showLogoutDialog(){
        val logoutDialog = CustomDialog(
            context = mContext,
            title = resources.getString(R.string.demo_login_out_hint),
            isEditTextMode = false,
            onLeftButtonClickListener = {

            },
            onRightButtonClickListener = {
                logout()
            }
        )
        logoutDialog.show()
    }

    override fun fetchPresenceStatusSuccess(presence: MutableList<ChatPresence>) {
        updatePresence()
    }

    override fun fetchPresenceStatusFail(code: Int, message: String?) {

    }

}