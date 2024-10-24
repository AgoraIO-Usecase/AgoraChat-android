package io.agora.chatdemo.viewmodel

import androidx.lifecycle.ViewModel
import io.agora.chatdemo.repository.PushRepository
import io.agora.uikit.common.ChatPushRemindType
import io.agora.uikit.common.ChatSilentModeParam
import io.agora.uikit.common.ChatSilentModelType
import kotlinx.coroutines.flow.flow

class PushViewModel: ViewModel() {
    private val mRepository = PushRepository()

    fun setSilentModeForApp() =
            flow {
                emit(mRepository.setSilentModeForApp(ChatSilentModeParam(ChatSilentModelType.REMIND_TYPE).setRemindType(ChatPushRemindType.NONE)))
            }

    fun clearSilentModeForApp() =
            flow {
                emit(mRepository.setSilentModeForApp(ChatSilentModeParam(ChatSilentModelType.REMIND_TYPE).setRemindType(ChatPushRemindType.ALL)))
            }

    fun getSilentModeForApp() =
            flow {
                emit(mRepository.getSilentModeForApp())
            }
}