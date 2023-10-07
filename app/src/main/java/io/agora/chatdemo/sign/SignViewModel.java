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
    private SingleSourceLiveData<Resource<Boolean>> registerObservable;
    private SingleSourceLiveData<Resource<Boolean>> checkLoginObservable;

    public SignViewModel(@NonNull Application application) {
        super(application);
        repository = new EMClientRepository();
        loginObservable = new SingleSourceLiveData<>();
        registerObservable = new SingleSourceLiveData<>();
        checkLoginObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<Boolean>> getLoginObservable() {
        return loginObservable;
    }

    public LiveData<Resource<Boolean>> getRegisterObservable() {
        return registerObservable;
    }

    public LiveData<Resource<Boolean>> getCheckLoginObservable() {
        return checkLoginObservable;
    }
    
    public void login(String username, String password) {
        loginObservable.setSource(repository.loginByAppServer(username, password));
    }

    public void register(String username, String pwd){
        registerObservable.setSource(repository.registerByAppServer(username, pwd));
    }

    public void checkLogin() {
        checkLoginObservable.setSource(repository.checkAndSignOut());
    }
}
