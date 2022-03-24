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

    public ThreadChatViewModel(@NonNull Application application) {
        super(application);
        threadRepository = new EMThreadManagerRepository();
        resultObservable = new SingleSourceLiveData<>();
    }

    /**
     * Get no push user list
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
     * Disband thread
     * @param threadId
     */
    public void disbandThread(String threadId) {
        resultObservable.setSource(threadRepository.destroyThread(threadId));
    }

}
