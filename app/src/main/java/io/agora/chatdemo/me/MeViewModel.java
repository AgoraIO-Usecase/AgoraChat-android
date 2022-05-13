package io.agora.chatdemo.me;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.Map;

import io.agora.chat.Conversation;
import io.agora.chat.PushManager;
import io.agora.chat.SilentModeResult;
import io.agora.chat.SilentModeParam;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;

public class MeViewModel extends AndroidViewModel {
    private EMPushManagerRepository repository;
    private EMContactManagerRepository contactManagerRepository;
    private SingleSourceLiveData<Resource<EaseUser>> updateNicknameObservable;
    private SingleSourceLiveData<Resource<Boolean>> updatePushStyleObservable;
    private SingleSourceLiveData<Resource<SilentModeResult>> updateAllSilentModeObservable;
    private SingleSourceLiveData<Resource<SilentModeResult>> fetchAllSilentModeObservable;
    private SingleSourceLiveData<Resource<SilentModeResult>> updateConversationSilentModeObservable;
    private SingleSourceLiveData<Resource<SilentModeResult>> fetchConversationSilentModeObservable;
    private SingleSourceLiveData<Resource<Boolean>> clearConversationRemindTypeObservable;
    private SingleSourceLiveData<Resource<Map<String, SilentModeResult>>> fetchConversationsSilentModeObservable;
    private SingleSourceLiveData<Resource<Boolean>> updatePushTranslationLanguageObservable;
    private SingleSourceLiveData<Resource<String>> fetchPushTranslationLanguageObservable;
    public MeViewModel(@NonNull  Application application) {
        super(application);
        repository = new EMPushManagerRepository();
        contactManagerRepository= new EMContactManagerRepository();
        updateNicknameObservable = new SingleSourceLiveData<>();
        updatePushStyleObservable = new SingleSourceLiveData<>();
        updateAllSilentModeObservable = new SingleSourceLiveData<>();
        fetchAllSilentModeObservable = new SingleSourceLiveData<>();
        updateConversationSilentModeObservable = new SingleSourceLiveData<>();
        fetchConversationSilentModeObservable = new SingleSourceLiveData<>();
        clearConversationRemindTypeObservable = new SingleSourceLiveData<>();
        fetchConversationsSilentModeObservable = new SingleSourceLiveData<>();
        updatePushTranslationLanguageObservable = new SingleSourceLiveData<>();
        fetchPushTranslationLanguageObservable = new SingleSourceLiveData<>();
    }
    public LiveData<Resource<EaseUser>> getUpdateNicknameObservable() {
        return updateNicknameObservable;
    }
    public void updateNickname(String nickname) {
        repository.updatePushNickname(nickname);
        updateNicknameObservable.setSource(contactManagerRepository.updateCurrentUserInfo(UserInfo.UserInfoType.NICKNAME, nickname));
    }

    public LiveData<Resource<Boolean>> getUpdatePushStyleObservable() {
        return updatePushStyleObservable;
    }
    public LiveData<Resource<SilentModeResult>> getUpdateAllSilentModeObservable() {
        return updateAllSilentModeObservable;
    }
    public LiveData<Resource<SilentModeResult>> getFetchAllSilentModeObservable() {
        return fetchAllSilentModeObservable;
    }
    public LiveData<Resource<SilentModeResult>> getUpdateConversationSilentModeObservable() {
        return updateConversationSilentModeObservable;
    }
    public LiveData<Resource<SilentModeResult>> getFetchConversationSilentModeObservable() {
        return fetchConversationSilentModeObservable;
    }

    public LiveData<Resource<Boolean>> getClearConversationRemindTypeObservable(){
        return clearConversationRemindTypeObservable;
    }
    public LiveData<Resource<Map<String, SilentModeResult>>> getFetchConversationsSilentModeObservable() {
        return fetchConversationsSilentModeObservable;
    }
    public LiveData<Resource<Boolean>> getUpdatePushTranslationLanguageObservable() {
        return updatePushTranslationLanguageObservable;
    }
    public LiveData<Resource<String>> getFetchPushTranslationLanguageObservable() {
        return fetchPushTranslationLanguageObservable;
    }


    public void updatePushStyle(PushManager.DisplayStyle style) {
        updatePushStyleObservable.setSource(repository.updatePushStyle(style));
    }
    public void updateSilentModeForAll(SilentModeParam param) {
        updateAllSilentModeObservable.setSource(repository.setSilentModeForAll(param));
    }
    public void fetchSilentModeForAll() {
        fetchAllSilentModeObservable.setSource(repository.getSilentModeForAll());
    }
    public void updateSilentModeForConversation(String conversationId, Conversation.ConversationType type, SilentModeParam param) {
        updateConversationSilentModeObservable.setSource(repository.setSilentModeForConversation(conversationId, type, param));
    }
    public void fetchSilentModeForConversation(String conversationId, Conversation.ConversationType type) {
        fetchConversationSilentModeObservable.setSource(repository.getSilentModeForConversation(conversationId, type));
    }

    public void clearRemindTypeForConversation(String conversationId, Conversation.ConversationType type){
        clearConversationRemindTypeObservable.setSource(repository.clearRemindTypeForConversation(conversationId, type));
    }

    public void fetchSilentModeForConversations(List<Conversation> conversations) {
        fetchConversationsSilentModeObservable.setSource(repository.getSilentModeForConversations(conversations));
    }
    public void updatePushPerformLanguage(String languageCode) {
        updatePushTranslationLanguageObservable.setSource(repository.setPushPerformLanguage(languageCode));
    }
    public void fetchPushPerformLanguage() {
        fetchPushTranslationLanguageObservable.setSource(repository.getPushPerformLanguage());
    }
}
