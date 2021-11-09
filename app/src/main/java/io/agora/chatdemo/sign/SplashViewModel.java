package io.agora.chatdemo.sign;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMClientRepository;

public class SplashViewModel extends AndroidViewModel {
    private EMClientRepository mRepository;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        mRepository = new EMClientRepository();
    }

    public LiveData<Resource<Boolean>> getLoginData() {
        return mRepository.loadAllInfoFromHX();
    }
}
