package io.agora.chatdemo.feature.presence.viewmodel

import androidx.lifecycle.viewModelScope
import io.agora.chatdemo.feature.presence.interfaces.IPresenceRequest
import io.agora.chatdemo.feature.presence.interfaces.IPresenceResultView
import io.agora.chatdemo.feature.presence.repository.ChatPresenceRepository
import io.agora.uikit.common.extensions.catchChatException
import io.agora.uikit.viewmodel.EaseBaseViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class PresenceViewModel: EaseBaseViewModel<IPresenceResultView>(), IPresenceRequest {

    private val presenceRepository by lazy { ChatPresenceRepository() }
    private val expiryTime = (7 * 24 * 60 * 60).toLong()

    override fun publishPresence(ext: String?) {
        viewModelScope.launch {
            flow {
                ext?.let {
                    emit(presenceRepository.publishPresence(it))
                }
            }
                .catchChatException { e ->
                    view?.onPublishPresenceFail(e.errorCode, e.description)
                }
                .collect {
                    view?.onPublishPresenceSuccess()
                }
        }
    }

    override fun fetchPresenceStatus(userIds: MutableList<String>?) {
        viewModelScope.launch {
            flow {
                userIds?.let {
                    emit(presenceRepository.fetchPresenceStatus(it))
                }
            }
                .catchChatException { e ->
                    view?.fetchPresenceStatusFail(e.errorCode, e.description)
                }
                .collect {
                    view?.fetchPresenceStatusSuccess(it)
                }
        }
    }

    override fun subscribePresences(userIds: MutableList<String>?, expiry: Long?) {
        viewModelScope.launch {
            flow {
                userIds?.let {
                    emit(presenceRepository.subscribePresences(it,expiry?:expiryTime))
                }
            }
                .catchChatException { e ->
                    view?.subscribePresenceFail(e.errorCode, e.description)
                }
                .collect {
                    view?.subscribePresenceSuccess(it)
                }
        }
    }

    override fun unsubscribePresences(userIds: MutableList<String>?) {
        viewModelScope.launch {
            flow {
                userIds?.let {
                    emit(presenceRepository.unSubscribePresences(it))
                }
            }
                .catchChatException { e ->
                    view?.unSubscribePresenceFail(e.errorCode, e.description)
                }
                .collect {
                    view?.unSubscribePresenceSuccess()
                }
        }
    }

    override fun fetchChatPresence(userIds: MutableList<String>) {
        viewModelScope.launch {
            flow {
                emit(presenceRepository.fetchPresenceStatus(userIds))
            }
                .catchChatException { e->
                    view?.fetchChatPresenceFail(e.errorCode, e.description)
                }
                .collect {
                    view?.fetchChatPresenceSuccess(it)
                }
        }
    }


}