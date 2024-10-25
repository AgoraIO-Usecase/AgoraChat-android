package io.agora.chatdemo.page.group

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chatdemo.utils.ToastUtils.showToast
import io.agora.chatdemo.viewmodel.ProfileInfoViewModel
import io.agora.uikit.common.ChatGroup
import io.agora.uikit.common.extensions.catchChatException
import io.agora.uikit.feature.group.EaseCreateGroupActivity
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatCreateGroupActivity: EaseCreateGroupActivity() {
    private val profileViewModel by lazy { ViewModelProvider(this)[ProfileInfoViewModel::class.java] }

    override fun createGroupSuccess(group: ChatGroup) {
        lifecycleScope.launch {
            profileViewModel.getGroupAvatar(group.groupId)
                .catchChatException { e ->
                    showToast(e.description)
                }
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .collect {
                    super.createGroupSuccess(group)
                }
        }
    }
}