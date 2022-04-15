package io.agora.chatdemo.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;

public class ContactBlackListModel extends AndroidViewModel {
    private EMContactManagerRepository repository;
    private SingleSourceLiveData<Resource<List<EaseUser>>> blackObservable;
    private SingleSourceLiveData<Resource<Boolean>> resultObservable;

    public ContactBlackListModel(@NonNull Application application) {
        super(application);
        repository = new EMContactManagerRepository();
        blackObservable = new SingleSourceLiveData<>();
        resultObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<List<EaseUser>>> blackObservable() {
        return blackObservable;
    }

    public void getBlackList() {
        blackObservable.setSource(repository.getBlackContactList());
    }

    public LiveData<Resource<Boolean>> resultObservable() {
        return resultObservable;
    }

    public void removeUserFromBlackList(String username) {
        resultObservable.setSource(repository.removeUserFromBlackList(username));
    }

}
