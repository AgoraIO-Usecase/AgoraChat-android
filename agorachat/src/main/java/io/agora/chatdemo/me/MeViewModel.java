package io.agora.chatdemo.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;

public class MeViewModel extends AndroidViewModel {
    private EMPushManagerRepository repository;
    private SingleSourceLiveData<Resource<Boolean>> updatePushNicknameObservable;
    public MeViewModel(@NonNull  Application application) {
        super(application);
        repository = new EMPushManagerRepository();
        updatePushNicknameObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<Boolean>> getUpdatePushNicknameObservable() {
        return updatePushNicknameObservable;
    }
    public void updatePushNickname(String nickname) {
        updatePushNicknameObservable.setSource(repository.updatePushNickname(nickname));
    }
}
