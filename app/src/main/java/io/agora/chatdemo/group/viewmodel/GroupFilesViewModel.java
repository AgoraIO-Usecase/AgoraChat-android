package io.agora.chatdemo.group.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


import java.io.File;
import java.util.List;

import io.agora.chat.MucSharedFile;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;
import io.agora.util.PathUtil;

public class GroupFilesViewModel extends AndroidViewModel {
    private Application application;
    private EMGroupManagerRepository repository;
    private SingleSourceLiveData<Resource<List<MucSharedFile>>> filesObservable;
    private SingleSourceLiveData<Resource<File>> showFileObservable;
    private SingleSourceLiveData<Resource<Boolean>> refreshFiles;

    public GroupFilesViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        repository = new EMGroupManagerRepository();
        filesObservable = new SingleSourceLiveData<>();
        showFileObservable = new SingleSourceLiveData<>();
        refreshFiles = new SingleSourceLiveData<>();
    }

    public LiveData<Resource<List<MucSharedFile>>> getFilesObservable() {
        return filesObservable;
    }

    public void getSharedFiles(String groupId, int pageNum, int pageSize) {
        filesObservable.setSource(repository.getSharedFiles(groupId, pageNum, pageSize));
    }

    public LiveData<Resource<File>> getShowFileObservable() {
        return showFileObservable;
    }

    /**
     * show file
     *
     * @param groupId
     * @param file
     */
    public void showFile(String groupId, MucSharedFile file) {
        File localFile = new File(PathUtil.getInstance().getFilePath(), file.getFileName());
        if (localFile.exists()) {
            showFileObservable.postValue(Resource.success(localFile));
            return;
        }
        showFileObservable.setSource(repository.downloadFile(groupId, file.getFileId(), localFile));
    }

    public LiveData<Resource<Boolean>> getDeleteObservable() {
        return refreshFiles;
    }

    /**
     * delete file
     *
     * @param groupId
     * @param file
     */
    public void deleteFile(String groupId, MucSharedFile file) {
        File local = new File(PathUtil.getInstance().getFilePath(), file.getFileName());
        if (local.exists()) {
            try {
                local.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        refreshFiles.setSource(repository.deleteFile(groupId, file.getFileId()));
    }

    /**
     * upload file
     *
     * @param groupId
     * @param uri
     */
    public void uploadFileByUri(String groupId, Uri uri) {
        if (!EaseFileUtils.isFileExistByUri(application, uri)) {
            refreshFiles.postValue(Resource.error(ErrorCode.ERR_FILE_NOT_EXIST, null));
            return;
        }
        refreshFiles.setSource(repository.uploadFile(groupId, uri.toString()));
    }

}
