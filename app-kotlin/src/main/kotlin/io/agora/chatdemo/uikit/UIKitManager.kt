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
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatUserInfoType
import io.agora.uikit.common.extensions.toProfile
import io.agora.uikit.common.impl.OnValueSuccess
import io.agora.uikit.feature.chat.activities.EaseChatActivity
import io.agora.uikit.feature.contact.EaseContactCheckActivity
import io.agora.uikit.feature.contact.EaseContactDetailsActivity
import io.agora.uikit.feature.group.EaseCreateGroupActivity
import io.agora.uikit.feature.group.EaseGroupDetailActivity
import io.agora.uikit.feature.invitation.EaseNewRequestsActivity
import io.agora.uikit.model.EaseGroupProfile
import io.agora.uikit.model.EaseProfile
import io.agora.uikit.provider.EaseCustomActivityRoute
import io.agora.uikit.provider.EaseGroupProfileProvider
import io.agora.uikit.provider.EaseSettingsProvider
import io.agora.uikit.provider.EaseUserProfileProvider
import io.agora.uikit.widget.EaseImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object UIKitManager {

    fun addUIKitSettings(context: Context) {
        addProviders(context)
        setUIKitConfigs(context)
    }

    fun addProviders(context: Context) {
        EaseIM.setUserProfileProvider(object : EaseUserProfileProvider {
                override fun getUser(userId: String?): EaseProfile? {
                    return DemoHelper.getInstance().getDataModel().getAllContacts()[userId]?.toProfile()
                }

                override fun fetchUsers(
                    userIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<EaseProfile>>
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
//                            EaseIM.updateUsersInfo(callbackList)
                            callbackList.map {
                                DemoHelper.getInstance().getDataModel().updateUserCache(it.id)
                                CallKitManager.setEaseCallKitUserInfo(it.id)
                            }
                        }
                        onValueSuccess(callbackList)
                    }
                }
            })
            .setGroupProfileProvider(object : EaseGroupProfileProvider {

                override fun getGroup(id: String?): EaseGroupProfile? {
                    ChatClient.getInstance().groupManager().getGroup(id)?.let {
                        return EaseGroupProfile(it.groupId, it.groupName, it.extension)
                    }
                    return null
                }

                override fun fetchGroups(
                    groupIds: List<String>,
                    onValueSuccess: OnValueSuccess<List<EaseGroupProfile>>
                ) {

                }
            })
            .setSettingsProvider(object : EaseSettingsProvider {
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
            .setCustomActivityRoute(object : EaseCustomActivityRoute {
                override fun getActivityRoute(intent: Intent): Intent? {
                    intent.component?.className?.let {
                        when(it) {
                            EaseChatActivity::class.java.name -> {
                                intent.setClass(context, ChatActivity::class.java)
                            }
                            EaseGroupDetailActivity::class.java.name -> {
                                intent.setClass(context, ChatGroupDetailActivity::class.java)
                            }
                            EaseContactDetailsActivity::class.java.name -> {
                                intent.setClass(context, ChatContactDetailActivity::class.java)
                            }
                            EaseCreateGroupActivity::class.java.name -> {
                                intent.setClass(context, ChatCreateGroupActivity::class.java)
                            }
                            EaseContactCheckActivity::class.java.name ->{
                                intent.setClass(context, ChatContactCheckActivity::class.java)
                            }
                            EaseNewRequestsActivity::class.java.name -> {
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
        EaseIM.getConfig()?.avatarConfig?.let {
            it.avatarShape = EaseImageView.ShapeType.ROUND
            it.avatarRadius = context.resources.getDimensionPixelSize(io.agora.uikit.R.dimen.ease_corner_extra_small)
        }
    }
}