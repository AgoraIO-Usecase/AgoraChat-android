package io.agora.chatdemo.common

import android.content.Intent
import android.util.Log
import io.agora.chatdemo.DemoApplication
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.common.helper.LocalNotifyHelper
import io.agora.chatdemo.page.login.LoginActivity
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatGroup
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatPresence
import io.agora.uikit.common.ChatPresenceListener
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.extensions.ioScope
import io.agora.uikit.common.extensions.mainScope
import io.agora.uikit.common.impl.ValueCallbackImpl
import io.agora.uikit.interfaces.EaseConnectionListener
import io.agora.uikit.interfaces.EaseContactListener
import io.agora.uikit.interfaces.EaseMessageListener
import io.agora.uikit.model.EaseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ListenersWrapper {
    private var isLoadGroupList = false

    private val connectListener by lazy {
        object : EaseConnectionListener() {
            override fun onConnected() {
                // do something
                CoroutineScope(Dispatchers.IO).launch {
                    val groups = ChatClient.getInstance().groupManager().allGroups
                    if (isLoadGroupList.not() && groups.isEmpty()) {
                        ChatClient.getInstance().groupManager().asyncGetJoinedGroupsFromServer(
                            ValueCallbackImpl<List<ChatGroup>>(onSuccess = {
                            isLoadGroupList = true
                            if (it.isEmpty().not()) {
                                EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name)
                                    .post(
                                        DemoHelper.getInstance().context.ioScope(),
                                        EaseEvent(EaseEvent.EVENT.UPDATE.name, EaseEvent.TYPE.GROUP)
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
        EaseIM.logout(unbindPushToken,
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

    private val messageListener by lazy { object : EaseMessageListener(){
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            super.onMessageReceived(messages)
            if (DemoHelper.getInstance().getDataModel().isAppPushSilent()) {
                return
            }
            // do something
            messages?.forEach { message ->

                if (EaseIM.checkMutedConversationList(message.conversationId())) {
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
            EaseIM.getContext()?.let {
                EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE.name)
                    .post(it.mainScope(), EaseEvent(EaseEvent.EVENT.UPDATE.name, EaseEvent.TYPE.PRESENCE,presence.publisher))
            }
        }
    }

    private val contactListener by lazy { object : EaseContactListener(){

        override fun onFriendRequestAccepted(username: String?) {
            val notifyMsg = LocalNotifyHelper.createContactNotifyMessage(username)
            ChatClient.getInstance().chatManager().saveMessage(notifyMsg)
            DemoHelper.getInstance().context.let {
                EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.ADD.name)
                    .post(it.mainScope(), EaseEvent(EaseEvent.EVENT.ADD.name, EaseEvent.TYPE.CONTACT))
            }
        }

        override fun onContactDeleted(username: String?) {
            LocalNotifyHelper.removeContactNotifyMessage(username)
        }
    } }

    fun registerListeners() {
        // register connection listener
        EaseIM.addConnectionListener(connectListener)
        EaseIM.addChatMessageListener(messageListener)
        EaseIM.addPresenceListener(presenceListener)
        EaseIM.addContactListener(contactListener)
    }
}