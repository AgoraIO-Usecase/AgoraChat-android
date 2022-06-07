package io.agora.chatdemo.chatthread.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMThreadManagerRepository;

public class ChatThreadEditViewModel extends AndroidViewModel {
    private EMThreadManagerRepository threadRepository;
    private SingleSourceLiveData<Resource<Boolean>> resultObservable;

    public ChatThreadEditViewModel(@NonNull Application application) {
        super(application);
        threadRepository = new EMThreadManagerRepository();
        resultObservable = new SingleSourceLiveData<>();
    }

    /**
     * Get result observable
     */
    public LiveData<Resource<Boolean>> getResultObservable() {
        return resultObservable;
    }

    /**
     * Change thread name
     * @param threadId
     */
    public void changeThreadName(String threadId, String threadName) {
        resultObservable.setSource(threadRepository.changeThreadName(threadId, threadName));
    }

}
