package io.agora.chatdemo.general.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.PushConfigs;
import io.agora.chat.PushManager;
import io.agora.chat.SilentModeResult;
import io.agora.chat.SilentModeParam;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;


public class EMPushManagerRepository extends BaseEMRepository {

    /**
     * Get push configuration
     * @return
     */
    public LiveData<Resource<PushConfigs>> getPushConfigsFromServer() {
        return new NetworkBoundResource<PushConfigs, PushConfigs>() {
            @Override
            protected boolean shouldFetch(PushConfigs data) {
                return true;
            }

            @Override
            protected LiveData<PushConfigs> loadFromDb() {
                return createLiveData(getPushManager().getPushConfigs());
            }

            @Override
            protected void createCall(ResultCallBack<LiveData<PushConfigs>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    PushConfigs configs = null;
                    try {
                        configs = getPushManager().getPushConfigsFromServer();
                        callBack.onSuccess(createLiveData(configs));
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }

            @Override
            protected void saveCallResult(PushConfigs item) {

            }
        }.asLiveData();
    }

    /**
     * Get push configuration
     * @return
     */
    public PushConfigs fetchPushConfigsFromServer() {
        try {
            return getPushManager().getPushConfigsFromServer();
        } catch (ChatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set Do Not Disturb Time Period
     * If end is less than start, then end is the hour of the next day
     * @param start
     * @param end
     * @return
     */
    public LiveData<Resource<Boolean>> disableOfflinePush(int start, int end) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    try {
                        ChatClient.getInstance().pushManager().disableOfflinePush(start, end);
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
     * Allow offline push
     * @return
     */
    public LiveData<Resource<Boolean>> enableOfflinePush() {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    try {
                        ChatClient.getInstance().pushManager().enableOfflinePush();
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
     * Update push nickname
     * @param nickname
     * @return
     */
    public LiveData<Resource<Boolean>> updatePushNickname(String nickname) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getPushManager().asyncUpdatePushNickname(nickname, new CallBack() {
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
     * Set push message style
     * @param style
     * @return
     */
    public LiveData<Resource<Boolean>> updatePushStyle(PushManager.DisplayStyle style) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getPushManager().asyncUpdatePushDisplayStyle(style, new CallBack() {
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
                            callBack.onSuccess(createLiveData(noPush));
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
                        try {
                            getPushManager().getPushConfigsFromServer();
                        } catch (ChatException e) {
                            e.printStackTrace();
                            callBack.onError(e.getErrorCode(), e.getDescription());
                            return;
                        }
                        List<String> noPushUsers = getPushManager().getNoPushUsers();
                        if (noPushUsers != null && noPushUsers.size() != 0) {
                            callBack.onSuccess(createLiveData(noPushUsers));
                        }
                    }
                });

            }
        }.asLiveData();
    }

    /**
     * Sets whether the specified group accepts offline message push
     *
     * @param groupID
     * @param noPush
     */
    public LiveData<Resource<Boolean>> setGroupNotDisturb(String groupID, boolean noPush) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                runOnIOThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> onPushList = new ArrayList<>();
                        onPushList.add(groupID);
                        try {
                            getPushManager().updatePushServiceForGroup(onPushList, noPush);
                            callBack.onSuccess(createLiveData(noPush));
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
     * Get the group list that disabled offline push
     * Note: If you want to get the latest configs, you should call {@link PushManager#getPushConfigsFromServer()} first
     */
    public LiveData<Resource<List<String>>> getNoPushGroups() {
        return new NetworkOnlyResource<List<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<String>>> callBack) {
                runOnIOThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            getPushManager().getPushConfigsFromServer();
                        } catch (ChatException e) {
                            e.printStackTrace();
                            callBack.onError(e.getErrorCode(), e.getDescription());
                            return;
                        }
                        List<String> noPushGroups = getPushManager().getNoPushGroups();
                        if (noPushGroups != null && noPushGroups.size() != 0) {
                            callBack.onSuccess(createLiveData(noPushGroups));
                        }
                    }
                });

            }
        }.asLiveData();
    }

    /**
     * Example Set the DND Settings for the current login user
     * @param param
     */
    public LiveData<Resource<SilentModeResult>> setSilentModeForAll(SilentModeParam param) {
        return new NetworkOnlyResource<SilentModeResult>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<SilentModeResult>> callBack) {
                ChatClient.getInstance().pushManager().setSilentModeForAll(param, new ValueCallBack<SilentModeResult>() {
                    @Override
                    public void onSuccess(SilentModeResult value) {
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
     * Set the DND of the conversation
     * @param conversationId
     * @param type
     * @param param
     */
    public LiveData<Resource<SilentModeResult>> setSilentModeForConversation(String conversationId, Conversation.ConversationType type, SilentModeParam param) {
        return new NetworkOnlyResource<SilentModeResult>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<SilentModeResult>> callBack) {
                ChatClient.getInstance().pushManager().setSilentModeForConversation(conversationId, type, param, new ValueCallBack<SilentModeResult>() {
                    @Override
                    public void onSuccess(SilentModeResult value) {
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
     * Gets the DND Settings of the current login user
     */
    public LiveData<Resource<SilentModeResult>> getSilentModeForAll() {
        return new NetworkOnlyResource<SilentModeResult>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<SilentModeResult>> callBack) {
                ChatClient.getInstance().pushManager().getSilentModeForAll(new ValueCallBack<SilentModeResult>() {
                    @Override
                    public void onSuccess(SilentModeResult value) {
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
     * Gets the DND setting of the conversation
     * @param conversationId
     * @param type
     */
    public LiveData<Resource<SilentModeResult>> getSilentModeForConversation(String conversationId, Conversation.ConversationType type) {
        return new NetworkOnlyResource<SilentModeResult>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<SilentModeResult>> callBack) {
                ChatClient.getInstance().pushManager().getSilentModeForConversation(conversationId, type, new ValueCallBack<SilentModeResult>() {
                    @Override
                    public void onSuccess(SilentModeResult value) {
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
     * Clear the setting of offline push notification type for the conversation
     * @param conversationId
     * @param type
     */
    public LiveData<Resource<Boolean>> clearRemindTypeForConversation(String conversationId, Conversation.ConversationType type) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().pushManager().clearRemindTypeForConversation(conversationId, type, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Obtain the DND Settings of specified sessions in batches
     * @param conversations
     */
    public LiveData<Resource<Map<String, SilentModeResult>>> getSilentModeForConversations(List<Conversation> conversations) {
        return new NetworkOnlyResource<Map<String, SilentModeResult>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Map<String, SilentModeResult>>> callBack) {
                ChatClient.getInstance().pushManager().getSilentModeForConversations(conversations, new ValueCallBack<Map<String, SilentModeResult>>() {
                    @Override
                    public void onSuccess(Map<String, SilentModeResult> value) {
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
     * Set user push translation language
     * @param languageCode
     */
    public LiveData<Resource<Boolean>> setPushPerformLanguage(String languageCode) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().pushManager().setPreferredNotificationLanguage(languageCode, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onProgress(int progress, String status) {

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
     * Get the push translation language set by the user
     */
    public LiveData<Resource<String>> getPushPerformLanguage() {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                ChatClient.getInstance().pushManager().getPreferredNotificationLanguage(new ValueCallBack<String>() {
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
}
