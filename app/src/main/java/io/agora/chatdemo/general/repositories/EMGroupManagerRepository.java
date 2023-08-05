package io.agora.chatdemo.general.repositories;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chat.GroupInfo;
import io.agora.chat.GroupOptions;
import io.agora.chat.MucSharedFile;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;

public class EMGroupManagerRepository extends BaseEMRepository{

    /**
     * Get all groups
     * @return
     */
    public LiveData<Resource<List<Group>>> getAllGroups() {
        return new NetworkBoundResource<List<Group>, List<Group>>() {
            @Override
            protected boolean shouldFetch(List<Group> data) {
                return true;
            }

            @Override
            protected LiveData<List<Group>> loadFromDb() {
                List<Group> allGroups = getGroupManager().getAllGroups();
                return new MutableLiveData<>(allGroups);
            }

            @Override
            protected void createCall(ResultCallBack<LiveData<List<Group>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                getGroupManager().asyncGetJoinedGroupsFromServer(new ValueCallBack<List<Group>>() {
                    @Override
                    public void onSuccess(List<Group> value) {
                        callBack.onSuccess(new MutableLiveData<>(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

            @Override
            protected void saveCallResult(List<Group> item) {

            }

        }.asLiveData();
    }

    /**
     * Get all groups
     * @param callBack
     */
    public void getAllGroups(ResultCallBack<List<Group>> callBack) {
        if(!isLoggedIn()) {
            callBack.onError(ErrorCode.NOT_LOGIN);
            return;
        }
        getGroupManager().asyncGetJoinedGroupsFromServer(new ValueCallBack<List<Group>>() {
            @Override
            public void onSuccess(List<Group> value) {
                if(callBack != null) {
                    callBack.onSuccess(value);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                if(callBack != null) {
                    callBack.onError(error, errorMsg);
                }
            }
        });
    }

    /**
     * Get joined groups from the server page
     * @param pageIndex
     * @param pageSize
     * @return
     */
    public LiveData<Resource<List<Group>>> getGroupListFromServer(int pageIndex, int pageSize) {
        return new NetworkOnlyResource<List<Group>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<Group>>> callBack) {
                getGroupManager().asyncGetJoinedGroupsFromServer(pageIndex, pageSize, false, false, new ValueCallBack<List<Group>>() {
                    @Override
                    public void onSuccess(List<Group> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get public groups from server by page
     * @param pageSize
     * @param cursor
     * @return
     */
    public LiveData<Resource<CursorResult<GroupInfo>>> getPublicGroupFromServer(int pageSize, String cursor) {
        return new NetworkOnlyResource<CursorResult<GroupInfo>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<CursorResult<GroupInfo>>> callBack) {
                DemoHelper.getInstance().getGroupManager().asyncGetPublicGroupsFromServer(pageSize, cursor, new ValueCallBack<CursorResult<GroupInfo>>() {
                    @Override
                    public void onSuccess(CursorResult<GroupInfo> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get group info from server
     * @param groupId
     * @return
     */
    public LiveData<Resource<Group>> getGroupFromServer(String groupId) {
        return new NetworkOnlyResource<Group>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Group>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                DemoHelper.getInstance().getGroupManager().asyncGetGroupFromServer(groupId, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Join group
     * @param group
     * @param reason
     * @return
     */
    public LiveData<Resource<Boolean>> joinGroup(Group group, String reason) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                if(group.isMemberOnly()) {
                    getGroupManager().asyncApplyJoinToGroup(group.getGroupId(), reason, new CallBack() {
                        @Override
                        public void onSuccess() {
                            callBack.onSuccess(createLiveData(true));
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code,error);
                        }

                        @Override
                        public void onProgress(int progress, String status) {

                        }
                    });
                }else {
                    getGroupManager().asyncJoinGroup(group.getGroupId(), new CallBack() {
                        @Override
                        public void onSuccess() {
                            callBack.onSuccess(createLiveData(true));
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code,error);
                        }

                        @Override
                        public void onProgress(int progress, String status) {

                        }
                    });
                }

            }
        }.asLiveData();
    }

    public LiveData<Resource<List<String>>> getGroupMembersByName(String groupId) {
        return new NetworkOnlyResource<List<String>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                DemoHelper.getInstance().getGroupManager().asyncGetGroupFromServer(groupId, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        List<String> members = value.getMembers();
                        if(members.size() < (value.getMemberCount() - value.getAdminList().size() - 1)) {
                            members = getAllGroupMemberByServer(groupId);
                        }
                        members.addAll(value.getAdminList());
                        members.add(value.getOwner());
                        if(!members.isEmpty()) {
                            callBack.onSuccess(createLiveData(members));
                        }else {
                            callBack.onError(ErrorCode.ERR_GROUP_NO_MEMBERS);
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Get a list of group members (including administrators and owners)
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<String>>> getGroupAllStringMembers(String groupId) {
        return new NetworkOnlyResource<List<String>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                DemoHelper.getInstance().getGroupManager().asyncGetGroupFromServer(groupId, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        List<String> members = value.getMembers();
                        if(members.size() < (value.getMemberCount() - value.getAdminList().size() - 1)) {
                            members = getAllGroupMemberByServer(groupId);
                        }
                        members.addAll(value.getAdminList());
                        members.add(value.getOwner());
                        if(!members.isEmpty()) {
                            Collections.sort(members);
                            callBack.onSuccess(createLiveData(members));
                        }else {
                            callBack.onError(ErrorCode.ERR_GROUP_NO_MEMBERS);
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Get a list of group members (including administrators and owners)
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<EaseUser>>> getGroupAllMembers(String groupId) {
        return new NetworkOnlyResource<List<EaseUser>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<EaseUser>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                DemoHelper.getInstance().getGroupManager().asyncGetGroupFromServer(groupId, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        List<String> members = value.getMembers();
                        if(members.size() < (value.getMemberCount() - value.getAdminList().size() - 1)) {
                            members = getAllGroupMemberByServer(groupId);
                        }
                        members.addAll(value.getAdminList());
                        members.add(value.getOwner());
                        if(!members.isEmpty()) {
                            List<EaseUser> users = EmUserEntity.parse(members);
                            sortUserData(users);
                            callBack.onSuccess(createLiveData(users));
                        }else {
                            callBack.onError(ErrorCode.ERR_GROUP_NO_MEMBERS);
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Get a list of group members (excluding administrators and owners)
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<EaseUser>>> getGroupMembers(String groupId) {
        return new NetworkOnlyResource<List<EaseUser>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<EaseUser>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                runOnIOThread(()-> {
                    List<String> members = getAllGroupMemberByServer(groupId);
                    List<EaseUser> users = new ArrayList<>();
                    if(members != null && !members.isEmpty()){
                        for(int i = 0; i < members.size(); i++){
                            EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(members.get(i));
                            if(user != null){
                                users.add(user);
                            }
                        }
                    }
                    sortUserData(users);
                    callBack.onSuccess(createLiveData(users));
                });
            }

        }.asLiveData();
    }

    /**
     * Get group managers, include group owner and group admins
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<EaseUser>>> getGroupManagers(String groupId) {
        return new NetworkOnlyResource<List<EaseUser>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<EaseUser>>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                getGroupManager().asyncGetGroupFromServer(groupId, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        List<EaseUser> groupManagers = new ArrayList<>();
                        List<String> adminList = value.getAdminList();
                        if(adminList != null && !adminList.isEmpty()) {
                            for (String username : adminList) {
                                EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(username);
                                if(user != null){
                                    groupManagers.add(user);
                                }
                            }
                        }
                        sortUserData(groupManagers);
                        EaseUser owner = DemoHelper.getInstance().getUsersManager().getUserInfo(value.getOwner());
                        groupManagers.add(0, owner);
                        if(!groupManagers.isEmpty()) {
                            callBack.onSuccess(createLiveData(groupManagers));
                        }else {
                            callBack.onError(ErrorCode.ERR_GROUP_NO_MEMBERS);
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get group muted map
     * @param groupId
     * @return
     */
    public LiveData<Resource<Map<String, Long>>> getGroupMuteMap(String groupId) {
        return new NetworkOnlyResource<Map<String, Long>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Map<String, Long>>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(() -> {
                    Map<String, Long> map = null;
                    Map<String, Long> result = new HashMap<>();
                    int pageSize = 200;
                    do{
                        try {
                            map = getGroupManager().fetchGroupMuteList(groupId, 0, pageSize);
                        } catch (ChatException e) {
                            e.printStackTrace();
                            callBack.onError(e.getErrorCode(), e.getMessage());
                            break;
                        }
                        if(map != null) {
                            result.putAll(map);
                        }
                    }while (map != null && map.size() >= 200);
                    callBack.onSuccess(createLiveData(result));
                });

            }

        }.asLiveData();
    }

    /**
     * Get group blacklist
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<String>>> getGroupBlockList(String groupId) {
        return new NetworkOnlyResource<List<String>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(() -> {
                    List<String> list = null;
                    try {
                        list = fetchGroupBlacklistFromServer(groupId);
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getMessage());
                        return;
                    }
                    if(list == null) {
                        list = new ArrayList<>();
                    }
                    callBack.onSuccess(createLiveData(list));
                });

            }

        }.asLiveData();
    }

    private List<String> fetchGroupBlacklistFromServer(String groupId) throws ChatException {
        int pageSize = 200;
        List<String> list = null;
        List<String> result = new ArrayList<>();
        do{
            list = getGroupManager().fetchGroupBlackList(groupId, 0, pageSize);
            if(list != null) {
                result.addAll(list);
            }
        }while (list != null && list.size() >= pageSize);
        return result;
    }

    /**
     * Get group announcement
     * @param groupId
     * @return
     */
    public LiveData<Resource<String>> getGroupAnnouncement(String groupId) {
        return new NetworkBoundResource<String, String>() {

            @Override
            protected boolean shouldFetch(String data) {
                return true;
            }

            @Override
            protected LiveData<String> loadFromDb() {
                String announcement = DemoHelper.getInstance().getGroupManager().getGroup(groupId).getAnnouncement();
                return createLiveData(announcement);
            }

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncFetchGroupAnnouncement(groupId, new ValueCallBack<String>() {
                    @Override
                    public void onSuccess(String value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

            @Override
            protected void saveCallResult(String item) {

            }

        }.asLiveData();
    }

    /**
     * Get group all members from server
     * @param groupId
     * @return
     */
    public List<String> getAllGroupMemberByServer(String groupId) {
        // 根据groupId获取群组中所有成员
        List<String> contactList = new ArrayList<>();
        CursorResult<String> result = null;
        do {
            try {
                result = getGroupManager().fetchGroupMembers(groupId, result != null ? result.getCursor() : "", 20);
            } catch (ChatException e) {
                e.printStackTrace();
            }
            if(result != null) {
                contactList.addAll(result.getData());
            }
        } while (result != null && !TextUtils.isEmpty(result.getCursor()));
        return contactList;
    }

    private void sortUserData(List<EaseUser> users) {
        Collections.sort(users, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                    return lhs.getNickname().compareTo(rhs.getNickname());
                }else{
                    if("#".equals(lhs.getInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getInitialLetter())){
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });
    }

    public List<Group> getAllManageGroups(List<Group> allGroups) {
        if(allGroups != null && allGroups.size() > 0) {
            List<Group> manageGroups = new ArrayList<>();
            for (Group group : allGroups) {
                if(TextUtils.equals(group.getOwner(), getCurrentUser()) || group.getAdminList().contains(getCurrentUser())) {
                    manageGroups.add(group);
                }
            }
            sortData(manageGroups);
            return manageGroups;
        }
        return new ArrayList<>();
    }

    /**
     * get all join groups, not contain manage groups
     * @return
     */
    public List<Group> getAllJoinGroups(List<Group> allGroups) {
        if(allGroups != null && allGroups.size() > 0) {
            List<Group> joinGroups = new ArrayList<>();
            for (Group group : allGroups) {
                if(!TextUtils.equals(group.getOwner(), getCurrentUser()) && !group.getAdminList().contains(getCurrentUser())) {
                    joinGroups.add(group);
                }
            }
            sortData(joinGroups);
            return joinGroups;
        }
        return new ArrayList<>();
    }

    /**
     * Sort by group name
     * @param groups
     */
    private void sortData(List<Group> groups) {
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                String name1 = EaseUtils.getLetter(o1.getGroupName());
                String name2 = EaseUtils.getLetter(o2.getGroupName());
                if(name1.equals(name2)){
                    return o1.getGroupId().compareTo(o2.getGroupId());
                }else{
                    if("#".equals(name1)){
                        return 1;
                    }else if("#".equals(name2)){
                        return -1;
                    }
                    return name1.compareTo(name2);
                }
            }
        });
    }

    /**
     * Set group name
     * @param groupId
     * @param groupName
     * @return
     */
    public LiveData<Resource<String>> setGroupName(String groupId, String groupName) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncChangeGroupName(groupId, groupName, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(groupName));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code,  error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Set group announcement
     * @param groupId
     * @param announcement
     * @return
     */
    public LiveData<Resource<String>> setGroupAnnouncement(String groupId, String announcement) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncUpdateGroupAnnouncement(groupId, announcement, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(announcement));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Set group description
     * @param groupId
     * @param description
     * @return
     */
    public LiveData<Resource<String>> setGroupDescription(String groupId, String description) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncChangeGroupDescription(groupId, description, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(description));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get group shared files
     * @param groupId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public LiveData<Resource<List<MucSharedFile>>> getSharedFiles(String groupId, int pageNum, int pageSize) {
        return new NetworkOnlyResource<List<MucSharedFile>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<MucSharedFile>>> callBack) {
                getGroupManager().asyncFetchGroupSharedFileList(groupId, pageNum, pageSize, new ValueCallBack<List<MucSharedFile>>() {
                    @Override
                    public void onSuccess(List<MucSharedFile> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Download shared files
     * @param groupId
     * @param fileId
     * @param localFile
     * @return
     */
    public LiveData<Resource<File>> downloadFile(String groupId, String fileId, File localFile) {
        return new NetworkOnlyResource<File>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<File>> callBack) {
                getGroupManager().asyncDownloadGroupSharedFile(groupId, fileId, localFile.getAbsolutePath(), new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(localFile));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Delete the file on the server side
     * @param groupId
     * @param fileId
     * @return
     */
    public LiveData<Resource<Boolean>> deleteFile(String groupId, String fileId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncDeleteGroupSharedFile(groupId, fileId, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Upload file to server
     * @param groupId
     * @param filePath
     * @return
     */
    public LiveData<Resource<Boolean>> uploadFile(String groupId, String filePath) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncUploadGroupSharedFile(groupId, filePath, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Invite group members
     * @param isOwner
     * @param groupId
     * @param members
     * @return
     */
    public LiveData<Resource<Boolean>> addMembers(boolean isOwner, String groupId, String[] members) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                if(isOwner) {
                    getGroupManager().asyncAddUsersToGroup(groupId, members, new CallBack() {
                        @Override
                        public void onSuccess() {
                            callBack.onSuccess(createLiveData(true));
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code, error);
                        }

                        @Override
                        public void onProgress(int progress, String status) {

                        }
                    });
                }else {
                    getGroupManager().asyncInviteUser(groupId, members, null, new CallBack() {
                        @Override
                        public void onSuccess() {
                            callBack.onSuccess(createLiveData(true));
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code, error);
                        }

                        @Override
                        public void onProgress(int progress, String status) {

                        }
                    });
                }
            }
        }.asLiveData();
    }

    /**
     * Transfer group own permissions
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<Boolean>> changeOwner(String groupId, String username) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncChangeOwner(groupId, username, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Set as group manager
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<String>> addGroupAdmin(String groupId, String username) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncAddGroupAdmin(groupId, username, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_add_admin, username)));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Remove group manager
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<String>> removeGroupAdmin(String groupId, String username) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncRemoveGroupAdmin(groupId, username, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_remove_admin, username)));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Remove from group
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<String>> removeUserFromGroup(String groupId, String username) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncRemoveUserFromGroup(groupId, username, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_remove, username)));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Add to group blacklist
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<String>> blockUser(String groupId, String username) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncBlockUser(groupId, username, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_add_black, username)));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Remove from group blacklist
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<String>> unblockUser(String groupId, String username) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncUnblockUser(groupId, username, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_remove_black, username)));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Mute group members
     * @param groupId
     * @param usernames
     * @return
     */
    public LiveData<Resource<String>> muteGroupMembers(String groupId, List<String> usernames, long duration) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncMuteGroupMembers(groupId, usernames, duration, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_mute, usernames.get(0))));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * UnMute group members
     * @param groupId
     * @param usernames
     * @return
     */
    public LiveData<Resource<String>> unMuteGroupMembers(String groupId, List<String> usernames) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getGroupManager().asyncUnMuteGroupMembers(groupId, usernames, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(getContext().getString(R.string.group_member_remove_mute, usernames.get(0))));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Leave from group
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> leaveGroup(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncLeaveGroup(groupId, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Destroy group
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> destroyGroup(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncDestroyGroup(groupId, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * create a new group
     * @param groupName
     * @param desc
     * @param allMembers
     * @param reason
     * @param option
     * @return
     */
    public LiveData<Resource<Group>> createGroup(String groupName, String desc, String[] allMembers, String reason, GroupOptions option) {
        return new NetworkOnlyResource<Group>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Group>> callBack) {
                getGroupManager().asyncCreateGroup(groupName, desc, allMembers, reason, option, new ValueCallBack<Group>() {
                    @Override
                    public void onSuccess(Group value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Block group messages
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> blockGroupMessage(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncBlockGroupMessage(groupId, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Unblock group messages
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> unblockGroupMessage(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getGroupManager().asyncUnblockGroupMessage(groupId, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }
}
