package io.agora.chatdemo.chat.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMChatManagerRepository;
import io.agora.chatdemo.general.repositories.EMChatRoomManagerRepository;

public class ChatViewModel extends AndroidViewModel {
    private EMChatRoomManagerRepository chatRoomManagerRepository;
    private EMChatManagerRepository chatManagerRepository;
    private SingleSourceLiveData<Resource<ChatRoom>> chatRoomObservable;
    private SingleSourceLiveData<Resource<Boolean>> makeConversationReadObservable;
    private SingleSourceLiveData<Resource< List<String>>> getNoPushUsersObservable;
    private SingleSourceLiveData<Resource<Boolean>> setNoPushUsersObservable;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRoomManagerRepository = new EMChatRoomManagerRepository();
        chatManagerRepository = new EMChatManagerRepository();
        chatRoomObservable = new SingleSourceLiveData<>();
        makeConversationReadObservable = new SingleSourceLiveData<>();
        getNoPushUsersObservable = new SingleSourceLiveData<>();
        setNoPushUsersObservable = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<ChatRoom>> getChatRoomObservable() {
        return chatRoomObservable;
    }
    public LiveData<Resource<List<String>>> getNoPushUsersObservable() {
        return getNoPushUsersObservable;
    }
    public LiveData<Resource<Boolean>> setNoPushUsersObservable() {
        return setNoPushUsersObservable;
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

}
