package io.agora.chatdemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.navigation.NavigationBarView
import io.agora.chatdemo.base.BaseInitActivity
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.databinding.ActivityMainLayoutBinding
import io.agora.chatdemo.interfaces.IMainResultView
import io.agora.chatdemo.page.contact.ChatContactListFragmentEvent
import io.agora.chatdemo.page.conversation.ConversationListFragment
import io.agora.chatdemo.page.me.fragment.AboutMeFragment
import io.agora.chatdemo.viewmodel.MainViewModel
import io.agora.chatdemo.viewmodel.ProfileInfoViewModel
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.feature.conversation.ChatUIKitConversationListFragment
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.ChatUIKitMessageListener
import io.agora.chat.uikit.interfaces.OnEventResultListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : BaseInitActivity<ActivityMainLayoutBinding>(), NavigationBarView.OnItemSelectedListener,
    OnEventResultListener, IMainResultView {
    override fun getViewBinding(inflater: LayoutInflater): ActivityMainLayoutBinding? {
        return ActivityMainLayoutBinding.inflate(inflater)
    }

    private var mConversationListFragment: Fragment? = null
    private var mContactFragment: Fragment? = null
    private var mAboutMeFragment: Fragment? = null
    private var mCurrentFragment: Fragment? = null
    private val badgeMap = mutableMapOf<Int, TextView>()
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val mProfileViewModel: ProfileInfoViewModel by lazy {
        ViewModelProvider(this)[ProfileInfoViewModel::class.java]
    }

    private val chatMessageListener = object : ChatUIKitMessageListener() {
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            mainViewModel.getUnreadMessageCount()
        }
    }

    companion object {
        fun actionStart(context: Context) {
            Intent(context, MainActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.navView.itemIconTintList = null
        switchToHome()
        checkIfShowSavedFragment(savedInstanceState)
        addTabBadge()
        mainViewModel.getRequestUnreadCount()
    }

    override fun initListener() {
        super.initListener()
        binding.navView.setOnItemSelectedListener(this)
        ChatUIKitClient.addEventResultListener(this)
        ChatUIKitClient.addChatMessageListener(chatMessageListener)
        ChatUIKitClient.addContactListener(contactListener)
    }

    override fun initData() {
        super.initData()
        mainViewModel.attachView(this)
        synchronizeProfile()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this){
            // check unread message count
            mainViewModel.getUnreadMessageCount()
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isNotifyChange) {
                mainViewModel.getRequestUnreadCount()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(this) {
            if (it.isNotifyChange) {
                mainViewModel.getRequestUnreadCount()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD + ChatUIKitEvent.TYPE.CONVERSATION).register(this) {
            if (it.isConversationChange) {
                mainViewModel.getUnreadMessageCount()
            }
        }
    }

    private fun switchToHome() {
        if (mConversationListFragment == null) {
            mConversationListFragment = ChatUIKitConversationListFragment.Builder()
                .useTitleBar(true)
                .enableTitleBarPressBack(false)
                .useSearchBar(true)
                .setCustomFragment(ConversationListFragment())
                .build()
        }
        mConversationListFragment?.let {
            replace(it, "conversation")
        }
    }

    private fun switchToContacts() {
        if (mContactFragment == null) {
            mContactFragment = ChatContactListFragmentEvent.Builder()
                .useTitleBar(true)
                .useSearchBar(true)
                .enableTitleBarPressBack(false)
                .setHeaderItemVisible(true)
                .build()
        }
        mContactFragment?.let {
            replace(it, "contact")
        }
    }

    private fun switchToAboutMe() {
        if (mAboutMeFragment == null) {
            mAboutMeFragment = AboutMeFragment()
        }
        mAboutMeFragment?.let {
            replace(it, "me")
        }
    }

    override fun onDestroy() {
        ChatUIKitClient.removeEventResultListener(this)
        ChatUIKitClient.removeChatMessageListener(chatMessageListener)
        ChatUIKitClient.removeContactListener(contactListener)
        super.onDestroy()
    }

    private fun replace(fragment: Fragment, tag: String) {
        if (mCurrentFragment !== fragment) {
            val t = supportFragmentManager.beginTransaction()
            mCurrentFragment?.let {
                t.hide(it)
            }
            mCurrentFragment = fragment
            if (!fragment.isAdded) {
                t.add(R.id.fl_main_fragment, fragment, tag).show(fragment).commit()
            } else {
                t.show(fragment).commit()
            }
        }
    }

    /**
     * 用于展示是否已经存在的Fragment
     * @param savedInstanceState
     */
    private fun checkIfShowSavedFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val tag = savedInstanceState.getString("tag")
            if (!tag.isNullOrEmpty()) {
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                if (fragment is Fragment) {
                    replace(fragment, tag)
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun addTabBadge() {
        (binding.navView.getChildAt(0) as? BottomNavigationMenuView)?.let { menuView->
            val childCount = menuView.childCount
            for (i in 0 until childCount) {
                val itemView = menuView.getChildAt(i) as BottomNavigationItemView
                val badge = LayoutInflater.from(this).inflate(R.layout.demo_badge_home, menuView, false)
                badgeMap[i] = badge.findViewById(R.id.tv_main_home_msg)
                itemView.addView(badge)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var showNavigation = false
        when (item.itemId) {
            R.id.em_main_nav_home -> {
                switchToHome()
                showNavigation = true
            }

            R.id.em_main_nav_friends -> {
                switchToContacts()
                showNavigation = true
            }

            R.id.em_main_nav_me -> {
                switchToAboutMe()
                showNavigation = true
            }
        }
        invalidateOptionsMenu()
        return showNavigation
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mCurrentFragment != null) {
            outState.putString("tag", mCurrentFragment!!.tag)
        }
    }

    override fun onEventResult(function: String, errorCode: Int, errorMessage: String?) {
        when(function){
            ChatUIKitConstant.API_ASYNC_ADD_CONTACT -> {
                if (errorCode == ChatError.EM_NO_ERROR){
                    runOnUiThread{
                        mContext.showToast(mContext.resources.getString(R.string.em_main_add_contact_success))
                    }
                }else{
                    runOnUiThread{
                        if (errorCode == ChatError.USER_NOT_FOUND){
                            mContext.showToast(mContext.resources.getString(R.string.em_main_add_contact_not_found))
                        }else{
                            mContext.showToast(errorMessage.toString())
                        }
                    }
                }
            }
            else -> {}
        }
    }

    override fun getUnreadCountSuccess(count: String?) {
        if (count.isNullOrEmpty()) {
            badgeMap[0]?.text = ""
            badgeMap[0]?.visibility = View.GONE
        } else {
            badgeMap[0]?.text = count
            badgeMap[0]?.visibility = View.VISIBLE
        }
    }

    override fun getRequestUnreadCountSuccess(count: String?) {
        if (count.isNullOrEmpty()) {
            badgeMap[1]?.text = ""
            badgeMap[1]?.visibility = View.GONE
        } else {
            badgeMap[1]?.text = count
            badgeMap[1]?.visibility = View.VISIBLE
        }
    }

    private val contactListener = object : ChatUIKitContactListener() {
        override fun onContactInvited(username: String?, reason: String?) {
            mainViewModel.getRequestUnreadCount()
        }
    }

    private fun synchronizeProfile(){
        lifecycleScope.launch {
            mProfileViewModel.synchronizeProfile()
                .onCompletion { dismissLoading() }
                .catchChatException { e ->
                    ChatLog.e("MainActivity", " synchronizeProfile fail error message = " + e.description)
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), null)
                .collect {
                    ChatLog.e("MainActivity","synchronizeProfile result $it")
                    it?.let {
                        DemoHelper.getInstance().getDataModel().insertUser(it)
                        ChatUIKitClient.updateCurrentUser(it)
                        CallKitManager.setEaseCallKitUserInfo(it.id)
                        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.CONTACT)
                            .post(lifecycleScope, ChatUIKitEvent(DemoConstant.EVENT_UPDATE_SELF, ChatUIKitEvent.TYPE.CONTACT))
                    }
                }
        }

    }

}
