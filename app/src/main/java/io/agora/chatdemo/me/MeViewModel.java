package io.agora.chatdemo.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chat.UserInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;

public class MeViewModel extends AndroidViewModel {
    private EMPushManagerRepository repository;
    private EMContactManagerRepository contactManagerRepository;
    private SingleSourceLiveData<Resource<EaseUser>> updateNicknameObservable;
    public MeViewModel(@NonNull  Application application) {
        super(application);
        repository = new EMPushManagerRepository();
        contactManagerRepository= new EMContactManagerRepository();
        updateNicknameObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<EaseUser>> getUpdateNicknameObservable() {
        return updateNicknameObservable;
    }
    public void updateNickname(String nickname) {
        repository.updatePushNickname(nickname);
        updateNicknameObservable.setSource(contactManagerRepository.updateCurrentUserInfo(UserInfo.UserInfoType.NICKNAME, nickname));
    }
}
