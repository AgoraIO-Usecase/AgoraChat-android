package io.agora.chatdemo.callkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chatdemo.callkit.holder.ConferenceMemberSelectViewHolder
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitLayoutGroupSelectContactBinding
import io.agora.chat.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ConferenceInviteAdapter(private val groupId: String?): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {
    private var selectedListener: OnContactSelectListener? = null
    private var existMembers:MutableList<String> = mutableListOf()
    private val checkedList:MutableList<String> = mutableListOf()

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        return ConferenceMemberSelectViewHolder(groupId, checkedList,
                UikitLayoutGroupSelectContactBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            )
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        if (holder is ConferenceMemberSelectViewHolder){
            holder.setSelectedMembers(existMembers)
            holder.setCheckBoxSelectListener(selectedListener)
        }
        super.onBindViewHolder(holder, position)
    }

    fun setExistMembers(existMembers:MutableList<String>){
        this.existMembers = existMembers
        notifyDataSetChanged()
    }

    /**
     * Set the listener for the checkbox selection.
     */
    fun setCheckBoxSelectListener(listener: OnContactSelectListener){
        this.selectedListener = listener
        notifyDataSetChanged()
    }

}