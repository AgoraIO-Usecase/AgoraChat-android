package io.agora.chatdemo.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMChatManagerRepository;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;

public class ChatSettingsViewModel extends AndroidViewModel {
    private EMPushManagerRepository repository;
    private EMChatManagerRepository chatManagerRepository;
    private SingleSourceLiveData<Resource<List<String>>> getNoPushUsersObservable;
    private SingleSourceLiveData<Resource<Boolean>> setNoPushUsersObservable;
    private SingleSourceLiveData<Resource<List<String>>> getNoPushGroupsObservable;
    private SingleSourceLiveData<Resource<Boolean>> setNoPushGroupsObservable;
    private SingleSourceLiveData<Resource<Boolean>> clearHistoryObservable;

    public ChatSettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new EMPushManagerRepository();
        chatManagerRepository = new EMChatManagerRepository();
        getNoPushGroupsObservable = new SingleSourceLiveData<>();
        setNoPushGroupsObservable = new SingleSourceLiveData<>();
        getNoPushUsersObservable = new SingleSourceLiveData<>();
        setNoPushUsersObservable = new SingleSourceLiveData<>();
        clearHistoryObservable = new SingleSourceLiveData<>();
    }

    /**
     * Set single chat to be not disturb
     *
     * @param userId 
     * @param noPush 
     */
    public void setUserNotDisturb(String userId, boolean noPush) {
        setNoPushUsersObservable.setSource(repository.setUserNotDisturb(userId,noPush));
    }

    public LiveData<Resource<Boolean>> getSetNoPushUsersObservable() {
        return setNoPushUsersObservable;
    }

    /**
     * Get no push user list
     */
    public void getNoPushUsers() {
        getNoPushUsersObservable.setSource(repository.getNoPushUsers());
    }

    public LiveData<Resource<List<String>>> getNoPushUsersObservable() {
        return getNoPushUsersObservable;
    }

    /**
     * Set group chat to be not disturb
     *
     * @param GroupID
     * @param noPush
     */
    public void setGroupNotDisturb(String GroupID, boolean noPush) {
        setNoPushGroupsObservable.setSource(repository.setGroupNotDisturb(GroupID, noPush));
    }

    public LiveData<Resource<Boolean>> getSetNoPushGroupsObservable() {
        return setNoPushGroupsObservable;
    }

    /**
     * Get no push group list
     */
    public void getNoPushGroups() {
        getNoPushGroupsObservable.setSource(repository.getNoPushGroups());
    }

    public LiveData<Resource<List<String>>> getNoPushGroupsObservable() {
        return getNoPushGroupsObservable;
    }

    public LiveData<Resource<Boolean>> getClearHistoryObservable() {
        return clearHistoryObservable;
    }

    public void clearHistory(String conversationId) {
        clearHistoryObservable.setSource(chatManagerRepository.deleteConversationById(conversationId));
    }

}
