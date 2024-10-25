package io.agora.chatdemo.callkit.fragment

import android.os.Bundle
import android.view.View
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.callkit.adapter.ConferenceInviteAdapter
import io.agora.uikit.base.EaseBaseRecyclerViewAdapter
import io.agora.uikit.common.EaseConstant
import io.agora.uikit.feature.group.fragments.EaseGroupMemberFragment
import io.agora.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.uikit.interfaces.OnContactSelectedListener
import io.agora.uikit.model.EaseUser

class ConferenceInviteFragment: EaseGroupMemberFragment() {
    companion object {
        fun newInstance(groupId: String, existMembers: MutableList<String>): ConferenceInviteFragment {
            val fragment = ConferenceInviteFragment()
            val bundle = Bundle()
            bundle.putString(EaseConstant.EXTRA_CONVERSATION_ID, groupId)
            bundle.putStringArrayList(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS, ArrayList(existMembers))
            fragment.arguments = bundle
            return fragment
        }
    }

    private var existMembers: MutableList<String> = mutableListOf()
    private var selectedMembers:MutableList<String> = mutableListOf()
    private var contactSelectedListener: OnContactSelectedListener? = null

    override fun initAdapter(): EaseBaseRecyclerViewAdapter<EaseUser> {
        return ConferenceInviteAdapter(groupId)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            existMembers = it.getStringArrayList(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS) ?: mutableListOf()
        }
        binding?.srlContactRefresh?.setEnableLoadMore(false)
    }

    override fun initListener() {
        super.initListener()
        if (mListAdapter is ConferenceInviteAdapter) {
            (mListAdapter as ConferenceInviteAdapter).setCheckBoxSelectListener(object :
                OnContactSelectListener {

                override fun onContactSelectedChanged(
                    v: View,
                    userId: String,
                    isSelected: Boolean
                ) {
                    if (isSelected){
                        if (!selectedMembers.contains(userId)){
                            selectedMembers.add(userId)
                        }
                    }else{
                        if (selectedMembers.contains(userId)){
                            selectedMembers.remove(userId)
                        }
                    }
                    contactSelectedListener?.onContactSelectedChanged(v,selectedMembers)
                }
            })
        }
    }

    override fun initData() {
        super.initData()
        if (mListAdapter is ConferenceInviteAdapter) {
            (mListAdapter as ConferenceInviteAdapter).setExistMembers(existMembers)
        }
    }

    /**
     * Set the listener for the group member selection.
     */
    fun setOnGroupMemberSelectedListener(listener: OnContactSelectedListener?){
        this.contactSelectedListener = listener
    }
}