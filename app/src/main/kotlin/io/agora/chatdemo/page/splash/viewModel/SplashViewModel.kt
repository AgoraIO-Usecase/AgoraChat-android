package io.agora.chatdemo.page.splash.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.agora.chatdemo.page.splash.repository.ChatClientRepository
import kotlinx.coroutines.flow.flow

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: ChatClientRepository = ChatClientRepository()

    fun loginData() = flow {
        emit(mRepository.loadAllInfoFromAgora())
    }

}