package io.agora.chatdemo.general.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;

/**
 * Handle the logic related to chat
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
     * Make conversation as read
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
     * Get conversations from server
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
     * Call the api request to set the session as read
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
     * Set Do Not Disturb for Single Chat User Chat
     *
     * @param userId
     * @param noPush
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
     * Get chat do not disturb users
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


    public LiveData<Resource<Boolean>> reportMessage(String reportMsgId,String reportType,String reportReason){
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().chatManager().asyncReportMessage(reportMsgId, reportType, reportReason, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Remove message of conversation from server.
     * @param conversationId
     * @param type
     * @param msgIdList
     * @return
     */
    public LiveData<Resource<Boolean>> removeMessagesFromServer(String conversationId, Conversation.ConversationType type, List<String> msgIdList) {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId, type);
                if(conversation == null) {
                    callBack.onError(Error.GENERAL_ERROR, "Not found conversation: "+conversationId);
                    return;
                }
                conversation.removeMessagesFromServer(msgIdList, new CallBack() {
                    @Override
                    public void onSuccess() {
                        for (String msgId : msgIdList) {
                            conversation.removeMessage(msgId);
                        }
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * translation message
     * @param message
     * @param targetLanguage
     * @return
     */
    public LiveData<Resource<ChatMessage>> translationMessage(ChatMessage message,List<String> targetLanguage) {
        return new NetworkOnlyResource<ChatMessage>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatMessage>> callBack) {
                getChatManager().translateMessage(message, targetLanguage, new ValueCallBack<ChatMessage>() {
                    @Override
                    public void onSuccess(ChatMessage value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error,errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

}
