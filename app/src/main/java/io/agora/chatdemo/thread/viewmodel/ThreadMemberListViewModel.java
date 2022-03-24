package io.agora.chatdemo.thread.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMThreadManagerRepository;

public class ThreadMemberListViewModel extends AndroidViewModel {
    private EMThreadManagerRepository threadRepository;
    private SingleSourceLiveData<Resource<List<EaseUser>>> resultObservable;

    public ThreadMemberListViewModel(@NonNull Application application) {
        super(application);
        threadRepository = new EMThreadManagerRepository();
        resultObservable = new SingleSourceLiveData<>();
    }

    /**
     * Get no push user list
     */
    public LiveData<Resource<List<EaseUser>>> getResultObservable() {
        return resultObservable;
    }

    /**
     * Get thread members
     * @param threadId
     */
    public void getThreadMembers(String threadId) {
        resultObservable.setSource(threadRepository.getThreadMembers(threadId));
    }

}
