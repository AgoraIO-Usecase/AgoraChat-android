package io.agora.chatdemo.general.repositories;

import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;

/**
 * 处理与chat相关的逻辑
 */
public class EMChatManagerRepository extends BaseEMRepository{

    public LiveData<Resource<Boolean>> deleteConversationById(String conversationId) {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                boolean isDelete = getChatManager().deleteConversation(conversationId, true);
                if(isDelete) {
                    callBack.onSuccess(new MutableLiveData<>(true));
                }else {
                    callBack.onError(ErrorCode.DELETE_CONVERSATION_ERROR);
                }
            }

        }.asLiveData();
    }

    /**
     * 将会话置为已读
     * @param conversationId
     * @return
     */
    public LiveData<Resource<Boolean>> makeConversationRead(String conversationId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                Conversation conversation = getChatManager().getConversation(conversationId);
                if(conversation == null) {
                    callBack.onError(ErrorCode.DELETE_CONVERSATION_ERROR);
                }else {
                    conversation.markAllMessagesAsRead();
                    callBack.onSuccess(createLiveData(true));
                }
            }
        }.asLiveData();
    }

    /**
     * 获取会话列表
     * @return
     */
    public LiveData<Resource<List<EaseConversationInfo>>> fetchConversationsFromServer() {
        return new NetworkOnlyResource<List<EaseConversationInfo>>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<EaseConversationInfo>>> callBack) {
                ChatClient.getInstance().chatManager().asyncFetchConversationsFromServer(new ValueCallBack<Map<String, Conversation>>() {
                    @Override
                    public void onSuccess(Map<String, Conversation> value) {
                        List<Conversation> conversations = new ArrayList<Conversation>(value.values());
                        List<EaseConversationInfo> infoList = new ArrayList<>();
                        if(!conversations.isEmpty()) {
                            EaseConversationInfo info = null;
                            for(Conversation conversation : conversations) {
                                info = new EaseConversationInfo();
                                info.setInfo(conversation);
                                info.setTimestamp(conversation.getLastMessage().getMsgTime());
                                infoList.add(info);
                            }
                        }
                        callBack.onSuccess(createLiveData(infoList));
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
     * 调用api请求将会话置为已读
     * @param conversationId
     * @return
     */
    public LiveData<Resource<Boolean>> makeConversationReadByAck(String conversationId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                runOnIOThread(()-> {
                    try {
                        getChatManager().ackConversationRead(conversationId);
                        callBack.onSuccess(createLiveData(true));
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getDescription());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 设置单聊用户聊天免打扰
     *
     * @param userId 用户名
     * @param noPush 是否免打扰
     */
    public LiveData<Resource<Boolean>> setUserNotDisturb(String userId, boolean noPush) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                runOnIOThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> onPushList = new ArrayList<>();
                        onPushList.add(userId);
                        try {
                            getPushManager().updatePushServiceForUsers(onPushList, noPush);
                            callBack.onSuccess(createLiveData(true));
                        } catch (ChatException e) {
                            e.printStackTrace();
                            callBack.onError(e.getErrorCode(), e.getDescription());
                        }
                    }
                });

            }
        }.asLiveData();
    }

    /**
     * 获取聊天免打扰用户
     */
    public LiveData<Resource<List<String>>> getNoPushUsers() {
        return new NetworkOnlyResource<List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                runOnIOThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> noPushUsers = getPushManager().getNoPushUsers();
                        if (noPushUsers != null && noPushUsers.size() != 0) {
                            callBack.onSuccess(createLiveData(noPushUsers));
                        }
                    }
                });

            }
        }.asLiveData();
    }

}
