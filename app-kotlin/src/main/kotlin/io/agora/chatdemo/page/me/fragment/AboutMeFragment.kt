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
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseFragment
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatPresence
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.dialog.CustomDialog
import io.agora.uikit.common.extensions.catchChatException
import io.agora.uikit.common.extensions.dpToPx
import io.agora.uikit.configs.setStatusStyle
import io.agora.uikit.feature.contact.EaseBlockListActivity
import io.agora.uikit.model.EaseEvent
import io.agora.uikit.widget.EaseCustomAvatarView
import kotlinx.coroutines.launch

class AboutMeFragment: EaseBaseFragment<DemoFragmentAboutMeBinding>(), View.OnClickListener,
    EaseCustomAvatarView.OnPresenceClickListener, IPresenceResultView {

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
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name).register(this) {
            if (it.isPresenceChange && it.message.equals(EaseIM.getCurrentUser()?.id) ) {
                updatePresence()
            }
        }

        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT).register(this) {
            if (it.isContactChange && it.event == DemoConstant.EVENT_UPDATE_SELF) {
                updatePresence(true)
            }
        }
    }

    private fun initPresence(){
        binding?.run {
            var name:String? = ChatClient.getInstance().currentUser
            val id = getString(R.string.main_about_me_id,ChatClient.getInstance().currentUser?:"")
            EaseIM.getConfig()?.avatarConfig?.setStatusStyle(epPresence.getStatusView(),4.dpToPx(mContext),
                ContextCompat.getColor(mContext, R.color.demo_background))
            epPresence.setPresenceStatusMargin(end = -4, bottom = -4)
            epPresence.setPresenceStatusSize(resources.getDimensionPixelSize(io.agora.uikit.R.dimen.ease_contact_status_icon_size))

            val layoutParams = epPresence.getUserAvatar().layoutParams
            layoutParams.width = 100.dpToPx(mContext)
            layoutParams.height = 100.dpToPx(mContext)
            epPresence.getUserAvatar().layoutParams = layoutParams

            EaseIM.getCurrentUser()?.let {
                epPresence.setUserAvatarData(it)
                name = it.getRemarkOrName()
            }
            tvName.text = name
            tvNumber.text = id
        }
    }

    private fun updatePresence(isRefreshAvatar:Boolean = false){
        EaseIM.getCurrentUser()?.let { user->
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
        val isSilent = EaseIM.checkMutedConversationList(ChatClient.getInstance().currentUser)
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
                EaseIM.getCurrentUser()?.id?.let {
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
                startActivity(Intent(mContext, EaseBlockListActivity::class.java))
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