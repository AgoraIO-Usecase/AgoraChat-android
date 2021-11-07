package io.agora.chatdemo.group.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import io.agora.chat.Group;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;

public class GroupMemberAuthorityViewModel extends AndroidViewModel {
    private EMGroupManagerRepository repository;
    private SingleSourceLiveData<Resource<Group>> adminObservable;
    private SingleSourceLiveData<Resource<List<EaseUser>>> membersObservable;
    private SingleSourceLiveData<Resource<List<String>>> allMembersObservable;
    private SingleSourceLiveData<Resource<List<EaseUser>>> groupManagersObservable;
    private SingleSourceLiveData<Resource<Map<String, Long>>> muteMembersObservable;
    private SingleSourceLiveData<Resource<List<String>>> blockMembersObservable;
    private SingleSourceLiveData<Resource<String>> refreshObservable;
    private SingleSourceLiveData<Resource<Boolean>> transferOwnerObservable;
    private LiveDataBus messageChangeLiveData = LiveDataBus.get();

    public GroupMemberAuthorityViewModel(@NonNull Application application) {
        super(application);
        repository = new EMGroupManagerRepository();
        adminObservable = new SingleSourceLiveData<>();
        membersObservable = new SingleSourceLiveData<>();
        muteMembersObservable = new SingleSourceLiveData<>();
        blockMembersObservable = new SingleSourceLiveData<>();
        refreshObservable = new SingleSourceLiveData<>();
        transferOwnerObservable = new SingleSourceLiveData<>();
        groupManagersObservable = new SingleSourceLiveData<>();
        allMembersObservable = new SingleSourceLiveData<>();
    }

    public LiveDataBus getMessageChangeObservable() {
        return messageChangeLiveData;
    }

    public LiveData<Resource<Group>> getGroupObservable() {
        return adminObservable;
    }

    public void getGroup(String groupId) {
        adminObservable.setSource(repository.getGroupFromServer(groupId));
    }

    public LiveData<Resource<List<EaseUser>>> getMemberObservable() {
        return membersObservable;
    }

    public void getMembers(String groupId) {
        membersObservable.setSource(repository.getGroupMembers(groupId));
    }

    public LiveData<Resource<List<String>>> getAllMembersObservable() {
        return allMembersObservable;
    }

    public void getGroupAllMembers(String groupId) {
        allMembersObservable.setSource(repository.getGroupAllStringMembers(groupId));
    }

    public LiveData<Resource<List<EaseUser>>> getGroupManagersObservable() {
        return groupManagersObservable;
    }

    public void getGroupManagers(String groupId) {
        groupManagersObservable.setSource(repository.getGroupManagers(groupId));
    }

    public LiveData<Resource<Map<String, Long>>> getMuteMembersObservable() {
        return muteMembersObservable;
    }

    public void getMuteMembers(String groupId) {
        muteMembersObservable.setSource(repository.getGroupMuteMap(groupId));
    }

    public LiveData<Resource<List<String>>> getBlockObservable() {
        return blockMembersObservable;
    }

    public void getBlockMembers(String groupId) {
        blockMembersObservable.setSource(repository.getGroupBlockList(groupId));
    }

    public LiveData<Resource<String>> getRefreshObservable() {
        return refreshObservable;
    }

    public LiveData<Resource<Boolean>> getTransferOwnerObservable() {
        return transferOwnerObservable;
    }

    public void changeOwner(String groupId, String username) {
        transferOwnerObservable.setSource(repository.changeOwner(groupId, username));
    }

    public void addGroupAdmin(String groupId, String username) {
        refreshObservable.setSource(repository.addGroupAdmin(groupId, username));
    }

    public void removeGroupAdmin(String groupId, String username) {
        refreshObservable.setSource(repository.removeGroupAdmin(groupId, username));
    }

    public void removeUserFromGroup(String groupId, String username) {
        refreshObservable.setSource(repository.removeUserFromGroup(groupId, username));
    }

    public void blockUser(String groupId, String username) {
        refreshObservable.setSource(repository.blockUser(groupId, username));
    }

    public void unblockUser(String groupId, String username) {
        refreshObservable.setSource(repository.unblockUser(groupId, username));
    }

    public void muteGroupMembers(String groupId, List<String> usernames, long duration) {
        refreshObservable.setSource(repository.muteGroupMembers(groupId, usernames, duration));
    }

    public void unMuteGroupMembers(String groupId, List<String> usernames) {
        refreshObservable.setSource(repository.unMuteGroupMembers(groupId, usernames));
    }

}
