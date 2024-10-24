package io.agora.chatdemo.callkit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chatdemo.callkit.holder.ConferenceMemberSelectViewHolder
import io.agora.uikit.base.EaseBaseRecyclerViewAdapter
import io.agora.uikit.databinding.EaseLayoutGroupSelectContactBinding
import io.agora.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.uikit.model.EaseUser

class ConferenceInviteAdapter(private val groupId: String?): EaseBaseRecyclerViewAdapter<EaseUser>() {
    private var selectedListener: OnContactSelectListener? = null
    private var existMembers:MutableList<String> = mutableListOf()
    private val checkedList:MutableList<String> = mutableListOf()

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<EaseUser> {
        return ConferenceMemberSelectViewHolder(groupId, checkedList,
                EaseLayoutGroupSelectContactBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            )
    }

    override fun onBindViewHolder(holder: ViewHolder<EaseUser>, position: Int) {
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