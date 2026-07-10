package io.agora.chatdemo.uikit

import android.content.Context
import android.content.Intent
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.extensions.internal.toProfile
import io.agora.chatdemo.page.chat.ChatActivity
import io.agora.chatdemo.page.contact.ChatContactCheckActivity
import io.agora.chatdemo.page.contact.ChatContactDetailActivity
import io.agora.chatdemo.page.contact.ChatNewRequestsActivity
import io.agora.chatdemo.page.group.ChatCreateGroupActivity
import io.agora.chatdemo.page.group.ChatGroupDetailActivity
import io.agora.chatdemo.repository.ProfileInfoRepository
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUserInfoType
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.common.impl.OnValueSuccess
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.feature.contact.ChatUIKitContactCheckActivity
import io.agora.chat.uikit.feature.contact.ChatUIKitContactDetailsActivity
import io.agora.chat.uikit.feature.group.ChatUIKitCreateGroupActivity
import io.agora.chat.uikit.feature.group.ChatUIKitGroupDetailActivity
import io.agora.chat.uikit.feature.invitation.ChatUIKitNewRequestsActivity
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.provider.ChatUIKitCustomActivityRoute
import io.agora.chat.uikit.provider.ChatUIKitGroupProfileProvider
import io.agora.chat.uikit.provider.ChatUIKitSettingsProvider
import io.agora.chat.uikit.provider.ChatUIKitUserProfileProvider
import io.agora.chat.uikit.widget.ChatUIKitImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UIKitManager {

    fun addUIKitSettings(context: Context) {
        addProviders(context)
        setUIKitConfigs(context)
    }

    fun addProviders(context: Context) {
        ChatUIKitClient.setUserProfileProvider(object : ChatUIKitUserProfileProvider {
                override fun getUser(userId: String?): ChatUIKitProfile? {
                    return DemoHelper.getInstance().getDataModel().getAllContacts()[userId]?.toProfile()
                }

                override fun fetchUsers(
                    userIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>
                ) {
                    // fetch users from server and call call onValueSuccess.onSuccess(users) after successfully getting users
                    CoroutineScope(Dispatchers.IO).launch {
                        if (userIds.isEmpty()) {
                            onValueSuccess(mutableListOf())
                            return@launch
                        }
                        val users = ProfileInfoRepository().getUserInfoAttribute(userIds, mutableListOf(ChatUserInfoType.NICKNAME, ChatUserInfoType.AVATAR_URL))
                        val callbackList = users.values.map { it.toProfile() }
                        if (callbackList.isNotEmpty()) {
                            DemoHelper.getInstance().getDataModel().insertUsers(callbackList)
                            DemoHelper.getInstance().getDataModel().updateUsersTimes(callbackList)
//                            ChatUIKitClient.updateUsersInfo(callbackList)
                            callbackList.map {
                                DemoHelper.getInstance().getDataModel().updateUserCache(it.id)
                                CallKitManager.setEaseCallKitUserInfo(it.id)
                            }
                        }
                        onValueSuccess(callbackList)
                    }
                }
            })
            .setGroupProfileProvider(object : ChatUIKitGroupProfileProvider {

                override fun getGroup(id: String?): ChatUIKitGroupProfile? {
                    ChatClient.getInstance().groupManager().getGroup(id)?.let {
                        return ChatUIKitGroupProfile(it.groupId, it.groupName, it.extension)
                    }
                    return null
                }

                override fun fetchGroups(
                    groupIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>
                ) {

                }
            })
            .setSettingsProvider(object : ChatUIKitSettingsProvider {
                override fun isMsgNotifyAllowed(message: ChatMessage?): Boolean {
                    return true
                }

                override fun isMsgSoundAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override fun isMsgVibrateAllowed(message: ChatMessage?): Boolean {
                    return false
                }

                override val isSpeakerOpened: Boolean
                    get() = true

            })
            .setCustomActivityRoute(object : ChatUIKitCustomActivityRoute {
                override fun getActivityRoute(intent: Intent): Intent? {
                    intent.component?.className?.let {
                        when(it) {
                            UIKitChatActivity::class.java.name -> {
                                intent.setClass(context, ChatActivity::class.java)
                            }
                            ChatUIKitGroupDetailActivity::class.java.name -> {
                                intent.setClass(context, ChatGroupDetailActivity::class.java)
                            }
                            ChatUIKitContactDetailsActivity::class.java.name -> {
                                intent.setClass(context, ChatContactDetailActivity::class.java)
                            }
                            ChatUIKitCreateGroupActivity::class.java.name -> {
                                intent.setClass(context, ChatCreateGroupActivity::class.java)
                            }
                            ChatUIKitContactCheckActivity::class.java.name ->{
                                intent.setClass(context, ChatContactCheckActivity::class.java)
                            }
                            ChatUIKitNewRequestsActivity::class.java.name -> {
                                intent.setClass(context, ChatNewRequestsActivity::class.java)
                            }
                            else -> {
                                return intent
                            }
                        }
                    }
                    return intent
                }

            })
    }

    fun setUIKitConfigs(context: Context) {
        ChatUIKitClient.getConfig()?.avatarConfig?.let {
            it.avatarShape = ChatUIKitImageView.ShapeType.ROUND
            it.avatarRadius = context.resources.getDimensionPixelSize(io.agora.chat.uikit.R.dimen.ease_corner_extra_small)
        }
    }
}