package io.agora.chatdemo.conversation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Presence;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMPresenceManagerRepository;


public class PresenceViewModel extends AndroidViewModel {
    private EMPresenceManagerRepository presenceManagerRepository;
    private SingleSourceLiveData<Resource<Boolean>> publishObservable;
    private SingleSourceLiveData<Resource<List<Presence>>> presencesObservable;

    public PresenceViewModel(@NonNull Application application) {
        super(application);
        presenceManagerRepository = new EMPresenceManagerRepository();
        publishObservable = new SingleSourceLiveData<>();
        presencesObservable = new SingleSourceLiveData<>();

    }

    public LiveData<Resource<Boolean>> getPublishObservable() {
        return publishObservable;
    }

    public void publishPresence(String ext) {
        publishObservable.setSource(presenceManagerRepository.publishPresence(ext));
    }

    public LiveData<Resource<List<Presence>>> presencesObservable() {
        return presencesObservable;
    }

    public void subscribePresences(List<EaseUser> users, long expiry) {
        List<String> ids = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            for (EaseUser user : users) {
                ids.add(user.getUsername());
            }
            presencesObservable.setSource(presenceManagerRepository.subscribePresences(ids, expiry));
        }
    }
    public void subscribePresences(String userName, long expiry) {
        List<String> ids = new ArrayList<>();
        ids.add(userName);
        presencesObservable.setSource(presenceManagerRepository.subscribePresences(ids, expiry));

    }

}
