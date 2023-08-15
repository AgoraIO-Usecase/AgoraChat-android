package io.agora.chatdemo.group.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.chat.Group;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;
import io.agora.chatdemo.group.model.MemberAttributeBean;

public class GroupDetailViewModel extends AndroidViewModel {
    private EMGroupManagerRepository repository;
    private SingleSourceLiveData<Resource<Group>> groupObservable;
    private SingleSourceLiveData<Resource<String>> announcementObservable;
    private SingleSourceLiveData<Resource<String>> refreshObservable;
    private SingleSourceLiveData<Resource<Boolean>> leaveGroupObservable;
    private SingleSourceLiveData<Resource<Boolean>> joinObservable;
    private SingleSourceLiveData<Resource<String>> setGroupNameObservable;
    private SingleSourceLiveData<Resource<Map<String,MemberAttributeBean>>> groupMemberAttributeObservable;
    private SingleSourceLiveData<Resource<Map<String,MemberAttributeBean>>> fetchMemberAttributeObservable;
    private SingleSourceLiveData<Resource<Map<String, MemberAttributeBean>>> fetchMemberAttributesObservable;


    public GroupDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new EMGroupManagerRepository();
        groupObservable = new SingleSourceLiveData<>();
        announcementObservable = new SingleSourceLiveData<>();
        refreshObservable = new SingleSourceLiveData<>();
        leaveGroupObservable = new SingleSourceLiveData<>();
        joinObservable = new SingleSourceLiveData<>();
        setGroupNameObservable = new SingleSourceLiveData<>();
        groupMemberAttributeObservable = new SingleSourceLiveData<>();
        fetchMemberAttributeObservable = new SingleSourceLiveData<>();
        fetchMemberAttributesObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<Boolean>> getJoinObservable() {
        return joinObservable;
    }

    public LiveDataBus getMessageChangeObservable() {
        return LiveDataBus.get();
    }

    public LiveData<Resource<Group>> getGroupObservable() {
        return groupObservable;
    }

    public void getGroup(String groupId) {
        new EMPushManagerRepository().getPushConfigsFromServer();
        groupObservable.setSource(repository.getGroupFromServer(groupId));
    }

    public LiveData<Resource<String>> getAnnouncementObservable() {
        return announcementObservable;
    }

    public void getGroupAnnouncement(String groupId) {
        announcementObservable.setSource(repository.getGroupAnnouncement(groupId));
    }

    public LiveData<Resource<String>> getRefreshObservable() {
        return refreshObservable;
    }

    public LiveData<Resource<String>> getSetGroupNameObservable() {
        return setGroupNameObservable;
    }

    public void setGroupName(String groupId, String groupName) {
        setGroupNameObservable.setSource(repository.setGroupName(groupId, groupName));
    }

    public void setGroupAnnouncement(String groupId, String announcement) {
        refreshObservable.setSource(repository.setGroupAnnouncement(groupId, announcement));
    }

    public void setGroupDescription(String groupId, String description) {
        refreshObservable.setSource(repository.setGroupDescription(groupId, description));
    }

    public LiveData<Resource<Boolean>> getLeaveGroupObservable() {
        return leaveGroupObservable;
    }

    public void leaveGroup(String groupId) {
        leaveGroupObservable.setSource(repository.leaveGroup(groupId));
    }

    public void destroyGroup(String groupId) {
        leaveGroupObservable.setSource(repository.destroyGroup(groupId));
    }

    public void joinGroup(Group group, String reason) {
        joinObservable.setSource(repository.joinGroup(group, reason));
    }

    public void setGroupMemberAttributes(String groupId, String userId,String nickName){
        Map<String,String> map = new HashMap<>();
        map.put("nickName",nickName);
        groupMemberAttributeObservable.setSource(repository.setGroupMemberAttributes(groupId,userId,map));
    }

    public void fetchGroupMemberAttribute(String groupId, String userId){
        fetchMemberAttributeObservable.setSource(repository.fetchGroupMemberDetail(groupId,userId));
    }

    public void fetchGroupMemberAttribute(String groupId, List<String> userList){
        fetchMemberAttributesObservable.setSource(repository.fetchGroupMemberDetail(groupId,userList));
    }

    public LiveData<Resource<Map<String,MemberAttributeBean>>> getFetchMemberAttributeObservable() {
        return fetchMemberAttributeObservable;
    }

    public LiveData<Resource<Map<String,MemberAttributeBean>>> getFetchMemberAttributesObservable() {
        return fetchMemberAttributesObservable;
    }

    public LiveData<Resource<Map<String,MemberAttributeBean>>> setMemberAttributeObservable() {
        return groupMemberAttributeObservable;
    }

}
