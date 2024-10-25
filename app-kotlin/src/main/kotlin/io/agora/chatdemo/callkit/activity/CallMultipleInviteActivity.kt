package io.agora.chatdemo.callkit.activity

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import io.agora.chat.callkit.EaseCallKit
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chatdemo.R
import io.agora.chatdemo.base.BaseInitActivity
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.callkit.fragment.ConferenceInviteFragment
import io.agora.chatdemo.databinding.DemoActivityConferenceInviteBinding
import io.agora.chatdemo.utils.ToastUtils.showToast
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatLog
import io.agora.uikit.interfaces.OnContactSelectedListener

class CallMultipleInviteActivity: BaseInitActivity<DemoActivityConferenceInviteBinding>() {
    private val existMembers = mutableListOf<String>()
    private var groupId: String? = null
    private var selectedMembers:MutableList<String> = mutableListOf()
    private var isFirstMeeting:Boolean = true

    companion object {
        private const val TAG = "ConferenceInvite"
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityConferenceInviteBinding? {
        return DemoActivityConferenceInviteBinding.inflate(inflater)
    }

    override fun initIntent(intent: Intent?) {
        super.initIntent(intent)
        isFirstMeeting = true
        intent?.let {
            groupId = it.getStringExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_ID)
            it.getStringArrayExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS)?.let { members->
                if (members.isNotEmpty()) {
                    existMembers.addAll(members)
                }
                if (!existMembers.contains(EaseIM.getCurrentUser()?.id)){
                    existMembers.add(ChatClient.getInstance().currentUser)
                }
            }?: kotlin.run {
                existMembers.add(ChatClient.getInstance().currentUser)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (groupId.isNullOrEmpty()) {
            ChatLog.e(TAG, "groupId is null or empty")
            finish()
            return
        }

        val fragment = ConferenceInviteFragment.newInstance(groupId!!, existMembers)
        fragment.setOnGroupMemberSelectedListener(object : OnContactSelectedListener {
            override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
                this@CallMultipleInviteActivity.selectedMembers = selectedMembers
                resetMenuInfo(selectedMembers.size)
            }
        })
        supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment).commit()

        resetMenuInfo(selectedMembers.size)
    }

    private fun resetMenuInfo(size: Int) {
        binding.titleBar.getToolBar().menu.findItem(R.id.chat_menu_member_call).let {
            it.isEnabled = size  > 0
            it.title = getString(R.string.menu_member_call, size)
            it.title?.let { title->
                if (it.isEnabled) {
                    SpannableStringBuilder(title).let { span ->
                        span.setSpan(ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.color_primary))
                            , 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        it.title = span
                    }
                }
            }
        }
    }

    override fun initListener() {
        super.initListener()

        binding.titleBar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.chat_menu_member_call -> {
                    if (selectedMembers.isEmpty()) {
                        showToast(R.string.tips_select_contacts_first)
                        return@setOnMenuItemClickListener true
                    }
                    isFirstMeeting = false
                    val members = selectedMembers.toTypedArray()
                    val params: Map<String, Any> = mutableMapOf(CallKitManager.KEY_GROUP_ID to groupId!!)
                    EaseCallKit.getInstance().startInviteMultipleCall(EaseCallType.getfrom(CallKitManager.rtcType),members, params)
                    finish()
                    true
                }
                else -> false
            }
        }
        binding.titleBar.setNavigationOnClickListener {
            selectedMembers.clear()
            setResult(RESULT_CANCELED)
            onBackPressed()
            if ((!isFirstMeeting || !CallMultipleBaseActivity().isFinishing) && !CallKitManager.checkChannelNameNullOrEmpty() ){
                EaseCallKit.getInstance().startInviteMultipleCall(EaseCallType.getfrom(CallKitManager.rtcType),null, null)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed()
        }
        return super.onKeyDown(keyCode, event)
    }

}