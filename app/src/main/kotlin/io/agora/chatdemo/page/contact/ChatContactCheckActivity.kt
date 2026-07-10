package io.agora.chatdemo.page.contact

import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.room.entity.parse
import io.agora.chatdemo.common.room.extensions.parseToDbBean
import io.agora.chatdemo.viewmodel.ProfileInfoViewModel
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUserInfoType
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.feature.contact.ChatUIKitContactCheckActivity
import io.agora.chat.uikit.model.ChatUIKitEvent
import kotlinx.coroutines.launch

class ChatContactCheckActivity: ChatUIKitContactCheckActivity() {
    private lateinit var model: ProfileInfoViewModel

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
        lifecycleScope.launch {
            user?.let { user->
                model.fetchUserInfoAttribute(listOf(user.userId), listOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                    .catchChatException {
                        ChatLog.e("ChatContactCheckActivity", "fetchUserInfoAttribute error: ${it.description}")
                    }
                    .collect {
                        it[user.userId]?.parseToDbBean()?.let { u->
                            u.parse().apply {
                                ChatUIKitClient.updateUsersInfo(mutableListOf(this))
                                DemoHelper.getInstance().getDataModel().insertUser(this)
                            }
                            updateUserInfo()
                        }
                    }
            }
        }
    }

    private fun updateUserInfo() {
        DemoHelper.getInstance().getDataModel().getUser(user?.userId)?.let {
            val ph = AppCompatResources.getDrawable(this, io.agora.chat.uikit.R.drawable.uikit_default_avatar)
            val ep = AppCompatResources.getDrawable(this, io.agora.chat.uikit.R.drawable.uikit_default_avatar)
            binding.ivAvatar.load(it.parse().avatar ?: ph) {
                placeholder(ph)
                error(ep)
            }
            binding.tvName.text = it.name?.ifEmpty { it.userId } ?: it.userId
        }
    }

}