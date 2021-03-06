package io.agora.chatdemo.contact.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;

public class ContactDetailViewModel extends AndroidViewModel {
    private EMContactManagerRepository repository;
    private SingleSourceLiveData<Resource<Boolean>> deleteObservable;
    private SingleSourceLiveData<Resource<Boolean>> blackObservable;
    private SingleSourceLiveData<Resource<EaseUser>> userInfoObservable;
    private SingleSourceLiveData<Resource<Boolean>> addContactObservable;

    public ContactDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new EMContactManagerRepository();
        deleteObservable = new SingleSourceLiveData<>();
        blackObservable = new SingleSourceLiveData<>();
        userInfoObservable = new SingleSourceLiveData<>();
        addContactObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<Boolean>> getAddContact() {
        return addContactObservable;
    }

    public LiveData<Resource<Boolean>> deleteObservable() {
        return deleteObservable;
    }

    public LiveData<Resource<Boolean>> blackObservable() {
        return blackObservable;
    }

    public LiveData<Resource<EaseUser>> userInfoObservable() {
        return userInfoObservable;
    }

    public void addContact(String username, String reason) {
        addContactObservable.setSource(repository.addContact(username, reason));
    }
    public void deleteContact(String username) {
        deleteObservable.setSource(repository.deleteContact(username));
    }

    public void addUserToBlackList(String username, boolean both) {
        blackObservable.setSource(repository.addUserToBlackList(username, both));
    }

    public void getUserInfoById(String username, boolean mIsFriend) {
        userInfoObservable.setSource(repository.getUserInfoById(username,mIsFriend));
    }

}
