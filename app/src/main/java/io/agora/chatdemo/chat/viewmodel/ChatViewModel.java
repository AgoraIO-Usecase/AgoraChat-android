package io.agora.chatdemo.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.Conversation;
import io.agora.chat.Presence;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMChatManagerRepository;
import io.agora.chatdemo.general.repositories.EMChatRoomManagerRepository;
import io.agora.chatdemo.general.repositories.EMPresenceManagerRepository;

public class ChatViewModel extends AndroidViewModel {
    private EMChatRoomManagerRepository chatRoomManagerRepository;
    private EMChatManagerRepository chatManagerRepository;
    private EMPresenceManagerRepository presenceManagerRepository;
    private SingleSourceLiveData<Resource<List<Presence>>> presenceObservable;
    private SingleSourceLiveData<Resource<ChatRoom>> chatRoomObservable;
    private SingleSourceLiveData<Resource<Boolean>> makeConversationReadObservable;
    private SingleSourceLiveData<Resource< List<String>>> getNoPushUsersObservable;
    private SingleSourceLiveData<Resource<Boolean>> setNoPushUsersObservable;
    private SingleSourceLiveData<Resource<Boolean>> chatManagerObservable;
    private SingleSourceLiveData<Resource<Boolean>> removeMessagesObservable;
    private SingleSourceLiveData<Resource<ChatMessage>> translationMessagesObservable;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRoomManagerRepository = new EMChatRoomManagerRepository();
        chatManagerRepository = new EMChatManagerRepository();
        presenceManagerRepository=new EMPresenceManagerRepository();
        chatRoomObservable = new SingleSourceLiveData<>();
        makeConversationReadObservable = new SingleSourceLiveData<>();
        getNoPushUsersObservable = new SingleSourceLiveData<>();
        setNoPushUsersObservable = new SingleSourceLiveData<>();
        presenceObservable = new SingleSourceLiveData<>();
        chatManagerObservable = new SingleSourceLiveData<>();
        removeMessagesObservable = new SingleSourceLiveData<>();
        translationMessagesObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<List<Presence>>> getPresenceObservable(){
        return presenceObservable;
    }
    public void fetchPresenceStatus(List<String> userIds){
        presenceObservable.setSource(presenceManagerRepository.fetchPresenceStatus(userIds));
    }
    public LiveData<Resource<ChatRoom>> getChatRoomObservable() {
        return chatRoomObservable;
    }
    public LiveData<Resource<Boolean>> getChatManagerObservable(){
        return chatManagerObservable;
    }
    public LiveData<Resource<List<String>>> getNoPushUsersObservable() {
        return getNoPushUsersObservable;
    }
    public LiveData<Resource<Boolean>> setNoPushUsersObservable() {
        return setNoPushUsersObservable;
    }

    public LiveData<Resource<ChatMessage>> getTranslationObservable(){
        return translationMessagesObservable;
    }

    public void getChatRoom(String roomId) {
        ChatRoom room = ChatClient.getInstance().chatroomManager().getChatRoom(roomId);
        if (room != null) {
            chatRoomObservable.setSource(new MutableLiveData<>(Resource.success(room)));
        } else {
            chatRoomObservable.setSource(chatRoomManagerRepository.getChatRoomById(roomId));
        }
    }

    public void makeConversationReadByAck(String conversationId) {
        makeConversationReadObservable.setSource(chatManagerRepository.makeConversationReadByAck(conversationId));
    }

    /**
     * Set single chat to be not disturb
     *
     * @param userId 
     * @param noPush 
     */
    public void setUserNotDisturb(String userId, boolean noPush) {
        setNoPushUsersObservable.setSource(chatManagerRepository.setUserNotDisturb(userId,noPush));
    }
    /**
     * Get no push user list
     */
    public void getNoPushUsers() {
        getNoPushUsersObservable.setSource(chatManagerRepository.getNoPushUsers());
    }

    public LiveData<Resource<Boolean>> getMakeConversationReadObservable() {
        return makeConversationReadObservable;
    }

    public void reportMessage(String reportMsgId, String reportType, String reportReason ){
        chatManagerObservable.setSource(chatManagerRepository.reportMessage(reportMsgId,reportType,reportReason));
    }

    public LiveData<Resource<Boolean>> getRemoveMessagesObservable() {
        return removeMessagesObservable;
    }

    /**
     * Remove messages from server
     * @param conversationId
     * @param type
     * @param msgIdList
     */
    public void removeMessagesFromServer(String conversationId, Conversation.ConversationType type, List<String> msgIdList) {
        removeMessagesObservable.setSource(chatManagerRepository.removeMessagesFromServer(conversationId, type, msgIdList));
    }

    /**
     * Translation message
     * @param message
     * @param targetLanguage
     */
    public void translationMessage(ChatMessage message,List<String> targetLanguage){
        translationMessagesObservable.setSource(chatManagerRepository.translationMessage(message,targetLanguage));
    }
}
