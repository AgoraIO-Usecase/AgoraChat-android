package io.agora.chatdemo.group.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;


public class GroupContactViewModel extends AndroidViewModel {
    private String currentUser;
    private EMGroupManagerRepository mRepository;
    private SingleSourceLiveData<Resource<List<Group>>> allGroupObservable;
    private SingleSourceLiveData<Resource<List<EaseUser>>> groupMemberObservable;
    private SingleSourceLiveData<Resource<CursorResult<GroupInfo>>> publicGroupObservable;
    private SingleSourceLiveData<Resource<CursorResult<GroupInfo>>> morePublicGroupObservable;
    private SingleSourceLiveData<Resource<List<Group>>> groupObservable;
    private SingleSourceLiveData<Resource<List<Group>>> moreGroupObservable;

    public GroupContactViewModel(@NonNull Application application) {
        super(application);
        currentUser = DemoHelper.getInstance().getCurrentUser();
        mRepository = new EMGroupManagerRepository();
        allGroupObservable = new SingleSourceLiveData<>();
        groupMemberObservable = new SingleSourceLiveData<>();
        publicGroupObservable = new SingleSourceLiveData<>();
        morePublicGroupObservable = new SingleSourceLiveData<>();
        groupObservable = new SingleSourceLiveData<>();
        moreGroupObservable = new SingleSourceLiveData<>();
    }

    public LiveDataBus getMessageObservable() {
        return LiveDataBus.get();
    }

    public LiveData<Resource<List<Group>>> getAllGroupsObservable() {
        return allGroupObservable;
    }

    public void loadAllGroups() {
        allGroupObservable.setSource(mRepository.getAllGroups());
    }
    
    public List<Group> getManageGroups(List<Group> allGroups) {
        return mRepository.getAllManageGroups(allGroups);
    }

    public List<Group> getJoinGroups(List<Group> allGroups) {
        return mRepository.getAllJoinGroups(allGroups);
    }

    public void getGroupMembers(String groupId) {
        groupMemberObservable.setSource(mRepository.getGroupAllMembers(groupId));
    }

    public LiveData<Resource<List<EaseUser>>> getGroupMember() {
        return groupMemberObservable;
    }

    public LiveData<Resource<CursorResult<GroupInfo>>> getPublicGroupObservable() {
        return publicGroupObservable;
    }

    public void getPublicGroups(int pageSize) {
        publicGroupObservable.setSource(mRepository.getPublicGroupFromServer(pageSize, null));
    }

    public LiveData<Resource<CursorResult<GroupInfo>>> getMorePublicGroupObservable() {
        return morePublicGroupObservable;
    }

    public void getMorePublicGroups(int pageSize, String cursor) {
        morePublicGroupObservable.setSource(mRepository.getPublicGroupFromServer(pageSize, cursor));
    }

    public LiveData<Resource<List<Group>>> getGroupObservable() {
        return groupObservable;
    }

    public void loadGroupListFromServer(int pageIndex, int pageSize) {
        groupObservable.setSource(mRepository.getGroupListFromServer(pageIndex, pageSize));
    }

    public LiveData<Resource<List<Group>>> getMoreGroupObservable() {
        return moreGroupObservable;
    }

    public void loadMoreGroupListFromServer(int pageIndex, int pageSize) {
        moreGroupObservable.setSource(mRepository.getGroupListFromServer(pageIndex, pageSize));
    }
}
