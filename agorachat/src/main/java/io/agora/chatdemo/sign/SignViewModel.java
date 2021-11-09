package io.agora.chatdemo.sign;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMClientRepository;

public class SignViewModel extends AndroidViewModel {
    private EMClientRepository repository;
    private SingleSourceLiveData<Resource<Boolean>> loginObservable;

    public SignViewModel(@NonNull Application application) {
        super(application);
        repository = new EMClientRepository();
        loginObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<Boolean>> getLoginObservable() {
        return loginObservable;
    }
    
    public void login(String username, String nickname) {
        loginObservable.setSource(repository.loginByAppServer(username, nickname));
    }
}
