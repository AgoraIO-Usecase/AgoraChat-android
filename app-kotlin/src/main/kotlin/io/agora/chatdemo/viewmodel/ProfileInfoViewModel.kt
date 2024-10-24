package io.agora.chatdemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.repository.ProfileInfoRepository
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatUserInfoType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

class ProfileInfoViewModel(application: Application) : AndroidViewModel(application)  {
    private val mRepository: ProfileInfoRepository = ProfileInfoRepository()
    @OptIn(ExperimentalCoroutinesApi::class)
    fun uploadAvatar(filePath: String?) =
        flow {
            emit(mRepository.uploadAvatar(filePath))
        }.flatMapConcat { result ->
            EaseIM.getCurrentUser()?.let {
                it.avatar = result
                DemoHelper.getInstance().getDataModel().insertUser(it)
                EaseIM.updateCurrentUser(it)
            }
            flow {
                emit(mRepository.uploadAvatarToChatServer(result))
            }
        }

    /**
     * Update user nickname.
     */
    fun updateUserNickName(nickName:String) =
        flow {
            emit(mRepository.updateNickname(nickName))
        }

    fun setUserRemark(username:String,remark:String) = flow {
        emit(mRepository.setUserRemark(username,remark))
    }

    fun fetchLocalUserRemark(userId:String):String?{
        val contact = ChatClient.getInstance().contactManager().fetchContactFromLocal(userId)
        return contact?.remark
    }

    fun synchronizeProfile(isSyncFromServer:Boolean = false) =
        flow {
            emit(mRepository.synchronizeProfile(isSyncFromServer))
        }

    fun getGroupAvatar(groupId:String?) = flow {
        emit(mRepository.getGroupAvatar(groupId))
    }

    /**
     * Fetch the user info attribute.
     */
    fun fetchUserInfoAttribute(userIds: List<String>, attributes: List<ChatUserInfoType>) =
        flow {
            emit(mRepository.getUserInfoAttribute(userIds, attributes))
        }

}