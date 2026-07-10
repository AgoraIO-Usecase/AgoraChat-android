package io.agora.chatdemo.page.login.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.agora.chatdemo.page.splash.repository.ChatClientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: ChatClientRepository = ChatClientRepository()

    /**
     * Login to Chat Server.
     * @param userName
     * @param pwd
     * @param isTokenFlag
     */
    fun login(userName: String, pwd: String,agoraUid:Int = 0,isTokenFlag: Boolean = false) =
        flow {
            emit(mRepository.loginToServer(userName, pwd, agoraUid, isTokenFlag))
        }

    /**
     * Login from app server.
     * @param userName
     * @param userPassword
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun loginFromAppServer(userName: String, userPassword: String) =
        flow {
            emit(mRepository.loginFromServer(userName, userPassword))
        } .flatMapConcat { result ->
            flow { emit(mRepository.loginToServer(result.chatUserName!!, result.token!!,result.agoraUid, true)) }
        }

    /**
     * Logout from Chat server.
     */
    fun logout() =
        flow {
            emit(mRepository.logout(true))
        }


}
