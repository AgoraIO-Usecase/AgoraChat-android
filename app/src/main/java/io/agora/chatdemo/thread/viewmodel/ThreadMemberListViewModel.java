package io.agora.chatdemo.thread.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMThreadManagerRepository;

public class ThreadMemberListViewModel extends AndroidViewModel {
    private EMThreadManagerRepository threadRepository;
    private SingleSourceLiveData<Resource<List<EaseUser>>> resultObservable;
    private SingleSourceLiveData<List<EaseUser>> searchResultObservable;
    private SingleSourceLiveData<Resource<Boolean>> removeResultObservable;

    public ThreadMemberListViewModel(@NonNull Application application) {
        super(application);
        threadRepository = new EMThreadManagerRepository();
        resultObservable = new SingleSourceLiveData<>();
        searchResultObservable = new SingleSourceLiveData<>();
        removeResultObservable = new SingleSourceLiveData<>();
    }

    /**
     * Get result observable
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

    /**
     * Get search result observable
     */
    public LiveData<List<EaseUser>> getSearchResultObservable() {
        return searchResultObservable;
    }

    /**
     * Search content from local data
     * @param mData
     * @param content
     */
    public void searchContact(List<EaseUser> mData, String content) {
        List<EaseUser> searchResult = new ArrayList<>();
        if(mData != null && mData.size() > 0) {
            for (EaseUser user : mData) {
                if(user.getUsername().contains(content) || user.getNickname().contains(content)) {
                    searchResult.add(user);
                }
            }
        }
        searchResultObservable.postValue(searchResult);
    }

    /**
     * Get remove thread member observable
     * @return
     */
    public LiveData<Resource<Boolean>> getRemoveResultObservable() {
        return removeResultObservable;
    }

    /**
     * Remove thread member
     * @param threadId
     * @param username
     */
    public void removeThreadMember(String threadId, String username) {
        removeResultObservable.setSource(threadRepository.removeThreadMember(threadId, username));
    }
}
