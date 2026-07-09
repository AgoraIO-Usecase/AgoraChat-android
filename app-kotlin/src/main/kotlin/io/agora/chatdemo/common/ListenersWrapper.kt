package io.agora.chatdemo.common

import android.content.Intent
import android.util.Log
import io.agora.chatdemo.DemoApplication
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.common.helper.LocalNotifyHelper
import io.agora.chatdemo.page.login.LoginActivity
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatPresence
import io.agora.chat.uikit.common.ChatPresenceListener
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.ioScope
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.impl.ValueCallbackImpl
import io.agora.chat.uikit.interfaces.ChatUIKitConnectionListener
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.ChatUIKitMessageListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ListenersWrapper {
    private var isLoadGroupList = false

    private val connectListener by lazy {
        object : ChatUIKitConnectionListener() {
            override fun onConnected() {
                // do something
                CoroutineScope(Dispatchers.IO).launch {
                    val groups = ChatClient.getInstance().groupManager().allGroups
                    if (isLoadGroupList.not() && groups.isEmpty()) {
                        ChatClient.getInstance().groupManager().asyncGetJoinedGroupsFromServer(
                            ValueCallbackImpl<List<ChatGroup>>(onSuccess = {
                            isLoadGroupList = true
                            if (it.isEmpty().not()) {
                                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
                                    .post(
                                        DemoHelper.getInstance().context.ioScope(),
                                        ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.GROUP)
                                    )
                            }
                        }, onError = {_,_ ->

                        })
                        )
                    }
                }

            }

            override fun onTokenExpired() {
                super.onTokenExpired()
                logout(false)
            }


            override fun onLogout(errorCode: Int, info: String?) {
                super.onLogout(errorCode, info)
                ChatLog.e("app","onLogout: $errorCode $info")
                logout()
            }
        }
    }

    private fun logout(unbindPushToken:Boolean = true){
        ChatUIKitClient.logout(unbindPushToken,
            onSuccess = {
                ChatLog.e("ListenersWrapper","logout success")
                DemoApplication.getInstance().getLifecycleCallbacks().activityList.forEach {
                    it.finish()
                }
                DemoApplication.getInstance().apply {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            },
            onError = {code, error ->
                ChatLog.e("ListenersWrapper","logout error $code $error")
            }
        )
    }

    private val messageListener by lazy { object : ChatUIKitMessageListener(){
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            super.onMessageReceived(messages)
            if (DemoHelper.getInstance().getDataModel().isAppPushSilent()) {
                return
            }
            // do something
            messages?.forEach { message ->

                if (ChatUIKitClient.checkMutedConversationList(message.conversationId())) {
                    return@forEach
                }
                if (DemoApplication.getInstance().getLifecycleCallbacks().isFront.not()) {
                    DemoHelper.getInstance().getNotifier()?.notify(message)
                }
            }
        }
    } }

    private val presenceListener by lazy{
        ChatPresenceListener {
            defaultPresencesEvent(it)
        }
    }

    private fun defaultPresencesEvent(presences: MutableList<ChatPresence>?){
        presences?.forEach { presence->
            PresenceCache.insertPresences(presence.publisher,presence)
            ChatUIKitClient.getContext()?.let {
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
                    .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.PRESENCE,presence.publisher))
            }
        }
    }

    private val contactListener by lazy { object : ChatUIKitContactListener(){

        override fun onFriendRequestAccepted(username: String?) {
            val notifyMsg = LocalNotifyHelper.createContactNotifyMessage(username)
            ChatClient.getInstance().chatManager().saveMessage(notifyMsg)
            DemoHelper.getInstance().context.let {
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                    .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.CONTACT))
            }
        }

        override fun onContactDeleted(username: String?) {
            LocalNotifyHelper.removeContactNotifyMessage(username)
        }
    } }

    fun registerListeners() {
        // register connection listener
        ChatUIKitClient.addConnectionListener(connectListener)
        ChatUIKitClient.addChatMessageListener(messageListener)
        ChatUIKitClient.addPresenceListener(presenceListener)
        ChatUIKitClient.addContactListener(contactListener)
    }
}