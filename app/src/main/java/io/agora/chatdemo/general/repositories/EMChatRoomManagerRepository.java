package io.agora.chatdemo.general.repositories;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.CursorResult;
import io.agora.chat.PageResult;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

public class EMChatRoomManagerRepository extends BaseEMRepository{

    public LiveData<Resource<List<ChatRoom>>> loadChatRoomsFromServer(int pageNum, int pageSize) {
        return new NetworkOnlyResource<List<ChatRoom>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<ChatRoom>>> callBack) {
                getChatRoomManager().asyncFetchPublicChatRoomsFromServer(pageNum, pageSize, new ValueCallBack<PageResult<ChatRoom>>() {
                    @Override
                    public void onSuccess(PageResult<ChatRoom> value) {
                        if(value != null && value.getData() != null) {
                            EMLog.i("TAG", "chatRooms = "+value.getData().toString());
                            callBack.onSuccess(createLiveData(value.getData()));
                        }else {
                            callBack.onError(ErrorCode.ERR_UNKNOWN);
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
     * get chat room from server
     * @param roomId
     * @return
     */
    public LiveData<Resource<ChatRoom>> getChatRoomById(String roomId) {
        return new NetworkBoundResource<ChatRoom, ChatRoom>() {
            @Override
            protected boolean shouldFetch(ChatRoom data) {
                return true;
            }

            @Override
            protected LiveData<ChatRoom> loadFromDb() {
                return createLiveData(getChatRoomManager().getChatRoom(roomId));
            }

            @Override
            protected void createCall(ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncFetchChatRoomFromServer(roomId, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

            @Override
            protected void saveCallResult(ChatRoom item) {

            }
        }.asLiveData();
    }

    public LiveData<Resource<List<String>>> loadMembers(String roomId) {
        return new NetworkOnlyResource<List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    List<String> memberList = new ArrayList<>();
                    try {
                        ChatRoom chatRoom = getChatRoomManager().fetchChatRoomFromServer(roomId);
                        // page size set to 20 is convenient for testing, should be applied to big value
                        CursorResult<String> result = new CursorResult<String>();
                        memberList.clear();
                        do {
                            result = ChatClient.getInstance().chatroomManager().fetchChatRoomMembers(roomId, result.getCursor(), 20);
                            memberList.addAll(result.getData());
                        } while (result.getCursor() != null && !result.getCursor().isEmpty());

                        memberList.remove(chatRoom.getOwner());
                        memberList.removeAll(chatRoom.getAdminList());
                        
                        if(isAdmin(chatRoom)) {
                            //Set<String> muteList = getChatRoomManager().fetchChatRoomMuteList(roomId, 0, 500).keySet();
                            List<String> blacks = getChatRoomManager().fetchChatRoomBlackList(roomId, 0, 500);
                            //memberList.removeAll(muteList);
                            memberList.removeAll(blacks);
                        }

                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                    callBack.onSuccess(createLiveData(memberList));
                });
            }
        }.asLiveData();
    }

    /**
     * Get chat room announcement content
     * @param roomId
     * @return
     */
    public LiveData<Resource<String>> fetchChatRoomAnnouncement(String roomId) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getChatRoomManager().asyncFetchChatRoomAnnouncement(roomId, new ValueCallBack<String>() {
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
        }.asLiveData();
    }

    /**
     * update chat room announcement
     * @param roomId
     * @param announcement
     * @return
     */
    public LiveData<Resource<String>> updateAnnouncement(String roomId, String announcement) {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                getChatRoomManager().asyncUpdateChatRoomAnnouncement(roomId, announcement, new CallBack() {
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
     * change chat room subject
     * @param roomId
     * @param newSubject
     * @return
     */
    public LiveData<Resource<ChatRoom>> changeChatRoomSubject(String roomId, String newSubject) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncChangeChatRoomSubject(roomId, newSubject, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * change chat room description
     * @param roomId
     * @param newDescription
     * @return
     */
    public LiveData<Resource<ChatRoom>> changeChatroomDescription(String roomId, String newDescription) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncChangeChatroomDescription(roomId, newDescription, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Determine whether it is an administrator or group owner
     * @param room
     * @return
     */
    private boolean isAdmin(ChatRoom room) {
        return TextUtils.equals(room.getOwner(), getCurrentUser()) || room.getAdminList().contains(getCurrentUser());
    }

    /**
     * Transfer chat room owner permissions
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<ChatRoom>> changeOwner(String groupId, String username) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                try {
                    getChatRoomManager().asyncChangeOwner(groupId, username, new ValueCallBack<ChatRoom>() {
                        @Override
                        public void onSuccess(ChatRoom value) {
                            callBack.onSuccess(createLiveData(value));
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            callBack.onError(error, errorMsg);
                        }
                    });
                } catch (ChatException e) {
                    e.printStackTrace();
                }
            }
        }.asLiveData();
    }

    /**
     * Get the list of banned chat rooms
     * @param groupId
     * @return
     */
    public LiveData<Resource<Map<String, Long>>> getChatRoomMuteMap(String groupId) {
        return new NetworkOnlyResource<Map<String, Long>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Map<String, Long>>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(() -> {
                    Map<String, Long> map = null;
                    Map<String, Long> result = new HashMap<>();
                    int pageSize = 200;
                    do{
                        try {
                            map = getChatRoomManager().fetchChatRoomMuteList(groupId, 0, pageSize);
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
     * Get the blacklist of chat rooms
     * @param groupId
     * @return
     */
    public LiveData<Resource<List<String>>> getChatRoomBlackList(String groupId) {
        return new NetworkOnlyResource<List<String>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(() -> {
                    List<String> list = null;
                    List<String> result = new ArrayList<>();
                    int pageSize = 200;
                    do{
                        try {
                            list = getChatRoomManager().fetchChatRoomBlackList(groupId, 0, pageSize);
                        } catch (ChatException e) {
                            e.printStackTrace();
                            callBack.onError(e.getErrorCode(), e.getMessage());
                            break;
                        }
                        if(list != null) {
                            result.addAll(list);
                        }
                    }while (list != null && list.size() >= 200);
                    callBack.onSuccess(createLiveData(result));
                });

            }

        }.asLiveData();
    }

    /**
     * Set as chat room manager
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<ChatRoom>> addChatRoomAdmin(String groupId, String username) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncAddChatRoomAdmin(groupId, username, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Remove chat room manager
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<ChatRoom>> removeChatRoomAdmin(String groupId, String username) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncRemoveChatRoomAdmin(groupId, username, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Move out of chat room
     * @param groupId
     * @param usernames
     * @return
     */
    public LiveData<Resource<ChatRoom>> removeUserFromChatRoom(String groupId, List<String> usernames) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncRemoveChatRoomMembers(groupId, usernames, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Add to chat room blacklist
     * Requires owner or administrator rights
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<ChatRoom>> blockUser(String groupId, List<String> username) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncBlockChatroomMembers(groupId, username, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Remove from the chat room blacklist
     * @param groupId
     * @param username
     * @return
     */
    public LiveData<Resource<ChatRoom>> unblockUser(String groupId, List<String> username) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncUnBlockChatRoomMembers(groupId, username, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Mute
     * Requires chat room owner or administrator permissions
     * @param groupId
     * @param usernames
     * @return
     */
    public LiveData<Resource<ChatRoom>> muteChatRoomMembers(String groupId, List<String> usernames, long duration) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncMuteChatRoomMembers(groupId, usernames, duration, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * UnMute
     * @param groupId
     * @param usernames
     * @return
     */
    public LiveData<Resource<ChatRoom>> unMuteChatRoomMembers(String groupId, List<String> usernames) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncUnMuteChatRoomMembers(groupId, usernames, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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
     * Leave chat room
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> leaveChatRoom(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getChatRoomManager().leaveChatRoom(groupId);
                callBack.onSuccess(createLiveData(true));
            }
        }.asLiveData();
    }

    /**
     * Destroy chat room
     * @param groupId
     * @return
     */
    public LiveData<Resource<Boolean>> destroyChatRoom(String groupId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getChatRoomManager().asyncDestroyChatRoom(groupId, new CallBack() {
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
     * create new chat room
     * @param subject
     * @param description
     * @param welcomeMessage
     * @param maxUserCount
     * @param members
     * @return
     */
    public LiveData<Resource<ChatRoom>> createChatRoom(String subject, String description, String welcomeMessage,
                                                         int maxUserCount, List<String> members) {
        return new NetworkOnlyResource<ChatRoom>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatRoom>> callBack) {
                getChatRoomManager().asyncCreateChatRoom(subject, description, welcomeMessage, maxUserCount, members, new ValueCallBack<ChatRoom>() {
                    @Override
                    public void onSuccess(ChatRoom value) {
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

}
