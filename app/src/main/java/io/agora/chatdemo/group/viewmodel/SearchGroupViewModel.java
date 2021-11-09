package io.agora.chatdemo.group.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chat.Group;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;


public class SearchGroupViewModel extends AndroidViewModel {
    private EMGroupManagerRepository repository;
    private SingleSourceLiveData<Resource<Group>> groupObservable;
    private SingleSourceLiveData<Resource<Boolean>> joinObservable;

    public SearchGroupViewModel(@NonNull Application application) {
        super(application);
        repository = new EMGroupManagerRepository();
        groupObservable = new SingleSourceLiveData<>();
        joinObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<Group>> getGroupObservable() {
        return groupObservable;
    }

    public void getGroup(String groupId) {
        groupObservable.setSource(repository.getGroupFromServer(groupId));
    }

    public LiveData<Resource<Boolean>> getJoinObservable() {
        return joinObservable;
    }

    public void joinGroup(Group group, String reason) {
        joinObservable.setSource(repository.joinGroup(group, reason));
    }

}
