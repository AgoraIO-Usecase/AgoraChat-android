package io.agora.chatdemo.contact.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;

public class ContactsListViewModel extends AndroidViewModel {
    private EMContactManagerRepository mRepository;

    private SingleSourceLiveData<Resource<List<EaseUser>>> contactObservable;
    private MediatorLiveData<Resource<List<EaseUser>>> blackObservable;
    private SingleSourceLiveData<Resource<Boolean>> blackResultObservable;
    private SingleSourceLiveData<Resource<Boolean>> deleteObservable;
    private SingleSourceLiveData<Resource<List<EaseUser>>> searchObservable;

    public ContactsListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EMContactManagerRepository();
        contactObservable = new SingleSourceLiveData<>();
        blackObservable = new MediatorLiveData<>();
        blackResultObservable = new SingleSourceLiveData<>();
        deleteObservable = new SingleSourceLiveData<>();
        searchObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<List<EaseUser>>> blackObservable() {
        return blackObservable;
    }

    public LiveDataBus messageChangeObservable() {
        return LiveDataBus.get();
    }

    public void getBlackList() {
        blackObservable.addSource(mRepository.getBlackContactList(), result -> blackObservable.postValue(result));
    }

    public void loadContactList(boolean fetchServer) {
        contactObservable.setSource(mRepository.getContactList(fetchServer));
    }

    public void searchContact(String key) {
        searchObservable.setSource(mRepository.getSearchContacts(key));
    }

    public LiveData<Resource<List<EaseUser>>> getContactObservable() {
        return contactObservable;
    }

    public LiveData<Resource<Boolean>> resultObservable() {
        return blackResultObservable;
    }

    public LiveData<Resource<Boolean>> deleteObservable() {
        return deleteObservable;
    }

    public LiveData<Resource<List<EaseUser>>> getSearchObservable() {
        return searchObservable;
    }

    public void deleteContact(String username) {
        deleteObservable.setSource(mRepository.deleteContact(username));
    }

    public void addUserToBlackList(String username, boolean both) {
        blackResultObservable.setSource(mRepository.addUserToBlackList(username, both));
    }


}
