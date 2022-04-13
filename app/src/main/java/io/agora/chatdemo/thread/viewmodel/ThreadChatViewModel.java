package io.agora.chatdemo.thread.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMThreadManagerRepository;

public class ThreadChatViewModel extends AndroidViewModel {
    private EMThreadManagerRepository threadRepository;
    private SingleSourceLiveData<Resource<Boolean>> resultObservable;
    private SingleSourceLiveData<Resource<Boolean>> disbandObservable;

    public ThreadChatViewModel(@NonNull Application application) {
        super(application);
        threadRepository = new EMThreadManagerRepository();
        resultObservable = new SingleSourceLiveData<>();
        disbandObservable = new SingleSourceLiveData<>();
    }

    /**
     * Get result observable
     */
    public LiveData<Resource<Boolean>> getResultObservable() {
        return resultObservable;
    }

    /**
     * Leave thread
     * @param threadId
     */
    public void leaveThread(String threadId) {
        resultObservable.setSource(threadRepository.leaveThread(threadId));
    }

    /**
     * Get disband observable
     * @return
     */
    public LiveData<Resource<Boolean>> getDisbandObservable() {
        return disbandObservable;
    }

    /**
     * Disband thread
     * @param threadId
     * @param parentMsgId
     */
    public void disbandThread(String threadId, String parentMsgId) {
        resultObservable.setSource(threadRepository.destroyThread(threadId, parentMsgId));
    }

}
