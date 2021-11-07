package io.agora.chatdemo.group.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.chat.Group;
import io.agora.chat.GroupOptions;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;


public class NewGroupViewModel extends AndroidViewModel {
    private EMGroupManagerRepository repository;
    private SingleSourceLiveData<Resource<Group>> groupObservable;
    private SingleSourceLiveData<Resource<Boolean>> addMemberObservable;

    public NewGroupViewModel(@NonNull Application application) {
        super(application);
        repository = new EMGroupManagerRepository();
        groupObservable = new SingleSourceLiveData<>();
        addMemberObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<Group>> groupObservable() {
        return groupObservable;
    }

    public void createGroup(String groupName, String desc, String[] allMembers, String reason, GroupOptions option) {
        groupObservable.setSource(repository.createGroup(groupName, desc, allMembers, reason, option));
    }

    public LiveData<Resource<Boolean>> addMemberObservable() {
        return addMemberObservable;
    }

    public void addGroupMembers(boolean isOwner, String groupId, @NonNull List<String> members) {
        String[] memberData = members.toArray(new String[members.size()]);
        addMemberObservable.setSource(repository.addMembers(isOwner, groupId, memberData));
    }
}
