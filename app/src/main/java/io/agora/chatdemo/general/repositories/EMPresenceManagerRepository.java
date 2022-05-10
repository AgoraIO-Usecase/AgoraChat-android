package io.agora.chatdemo.general.repositories;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Presence;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.Resource;

public class EMPresenceManagerRepository extends BaseEMRepository {

    public LiveData<Resource<List<Presence>>> fetchPresenceStatus(List<String> userIds){
        return new NetworkOnlyResource<List<Presence>>(){
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<Presence>>> callBack) {
                ChatClient.getInstance().presenceManager().fetchPresenceStatus(userIds, new ValueCallBack<List<Presence>>() {
                    @Override
                    public void onSuccess(List<Presence> presences) {
                        for (Presence presence : presences) {
                            DemoHelper.getInstance().getPresences().put(presence.getPublisher(),presence);
                        }
                        callBack.onSuccess(createLiveData(presences));
                    }

                    @Override
                    public void onError(int i, String s) {
                        callBack.onError(i);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<List<Presence>>> subscribePresences(List<String> ids,long expiry) {
        return new NetworkOnlyResource<List<Presence>>(){
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<List<Presence>>> callBack) {
                ChatClient.getInstance().presenceManager().subscribePresences(ids, expiry, new ValueCallBack<List<Presence>>() {
                    @Override
                    public void onSuccess(List<Presence> presences) {
                        for (Presence presence : presences) {
                            DemoHelper.getInstance().getPresences().put(presence.getPublisher(),presence);
                        }
                        callBack.onSuccess(createLiveData(presences));
                    }

                    @Override
                    public void onError(int i, String s) {
                        callBack.onError(i);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> publishPresence(String ext) {
        return new  NetworkOnlyResource<Boolean>(){
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().presenceManager().publishPresence(ext, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int i, String s) {
                        callBack.onError(i);
                    }
                });
            }
        }.asLiveData();
    }
}
