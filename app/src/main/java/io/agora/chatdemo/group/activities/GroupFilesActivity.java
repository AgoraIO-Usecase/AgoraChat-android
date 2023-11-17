package io.agora.chatdemo.group.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.MucSharedFile;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.manager.EaseActivityProviderHelper;
import io.agora.chat.uikit.utils.EaseCompat;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityGroupFilesBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SelectDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.models.SelectDialogItemBean;
import io.agora.chatdemo.general.permission.PermissionCompat;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.adapter.GroupFilesAdapter;
import io.agora.chatdemo.group.viewmodel.GroupFilesViewModel;
import io.agora.util.EMLog;
import io.agora.util.FileHelper;
import io.agora.util.VersionUtils;

public class GroupFilesActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener, OnRefreshListener, OnRefreshLoadMoreListener {
    private static final int REQUEST_CODE_SELECT_FILE = 1;
    private static final int REQUEST_CODE_SELECT_VIDEO = 2;
    private static final int REQUEST_CODE_SELECT_IMAGE = 3;
    private static final int REQUEST_CODE_STORAGE_PICTURE = 100;
    private static final int REQUEST_CODE_STORAGE_VIDEO = 101;
    private static final int REQUEST_CODE_STORAGE_FILE = 102;
    private static final int MENU_ID_UPLOAD_IMAGE = 1;
    private static final int MENU_ID_UPLOAD_VIDEO = 2;
    private static final int MENU_ID_UPLOAD_FILE = 3;
    private static final int LIMIT = 20;
    private ActivityGroupFilesBinding binding;
    private String groupId;
    private Group group;
    private int pageSize;
    private GroupFilesViewModel viewModel;
    private GroupFilesAdapter adapter;
    private List<SelectDialogItemBean> mUploadFilesSelectDialogItemBeans;

    private String mSearchContent;
    private List<MucSharedFile> mLastData;

    private final ActivityResultLauncher<String[]> requestImagePermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions()
            , result -> onRequestResult(result, REQUEST_CODE_STORAGE_PICTURE));
    private final ActivityResultLauncher<String[]> requestVideoPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions()
            , result -> onRequestResult(result, REQUEST_CODE_STORAGE_VIDEO));
    private final ActivityResultLauncher<String[]> requestFilePermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions()
            , result -> onRequestResult(result, REQUEST_CODE_STORAGE_FILE));

    private final ActivityResultLauncher<Intent> launcherToAlbum = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
            , result -> onActivityResult(result, REQUEST_CODE_SELECT_IMAGE));
    private final ActivityResultLauncher<Intent> launcherToVideo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
            , result -> onActivityResult(result, REQUEST_CODE_SELECT_VIDEO));
    private final ActivityResultLauncher<Intent> launcherToFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
            , result -> onActivityResult(result, REQUEST_CODE_SELECT_FILE));

    public static void actionStart(Context context, String groupId) {
        Intent starter = new Intent(context, GroupFilesActivity.class);
        starter.putExtra("group_id", groupId);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        binding = ActivityGroupFilesBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("group_id");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.titleBar.setTitle(getString(R.string.group_detail_files));
        binding.titleBar.setLeftImageResource(R.drawable.ease_titlebar_back);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);

        binding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new GroupFilesAdapter();
        adapter.hideEmptyView(true);
        adapter.setOnListItemClickListener(new GroupFilesAdapter.OnListItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MucSharedFile file = adapter.getItem(position);
                showFile(file);
            }

            @Override
            public void onDeleteClick(View view, int position) {
                final MucSharedFile file = adapter.getItem(position);
                new SimpleDialog.Builder(mContext)
                        .setContent(String.format(mContext.getResources().getString(R.string.group_file_delete_content), file.getFileName()))
                        .setOnConfirmClickListener(R.string.group_file_delete_confirm_text, new SimpleDialog.OnConfirmClickListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                deleteFile(file);
                            }
                        })
                        .setConfirmColor(R.color.contact_color_block)
                        .showCancelButton(true)
                        .show();
            }
        });
        binding.rvList.setCanSlide(isAllowEdit());
        binding.rvList.setAdapter(adapter);
        binding.rvList.addItemDecoration(new FileSpacesItemDecoration((int) EaseUtils.dip2px(mContext, 10)));

        binding.searchFile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString().trim());
            }
        });
    }


    @Override
    protected void initListener() {
        super.initListener();
        mLastData = new ArrayList<>();

        binding.titleBar.setOnRightClickListener(this);
        binding.titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        binding.srlRefresh.setOnRefreshLoadMoreListener(this);
    }


    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupFilesViewModel.class);
        viewModel.getFilesObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<MucSharedFile>>() {
                @Override
                public void onSuccess(List<MucSharedFile> data) {
                    if (null != data && 0 != data.size()) {
                        binding.titleBar.setTitle(getString(R.string.group_detail_files) + "(" + data.size() + ")");
                        binding.rvList.setVisibility(View.VISIBLE);
                        binding.rvList.closeMenu();
                        binding.noneFileTipView.setVisibility(View.GONE);

                        mLastData.clear();
                        mLastData.addAll(data);
                        search(mSearchContent);
                    } else {
                        binding.titleBar.setTitle(getString(R.string.group_detail_files));
                        binding.rvList.setVisibility(View.GONE);
                        binding.noneFileTipView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    finishRefresh();
                    finishLoadMore();
                }
            });
        });
        viewModel.getShowFileObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<File>() {
                @Override
                public void onSuccess(File data) {
                    //openFile(data);
                    shareFile(data);
                }
            });
        });
        viewModel.getDeleteObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    refresh();
                }

                @Override
                public void onLoading(Boolean data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }
            });
        });


        LiveDataBus.get().with(DemoConstant.GROUP_SHARE_FILE_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (TextUtils.equals(event.event, DemoConstant.GROUP_SHARE_FILE_CHANGE)) {
                refresh();
            }
        });
        refresh();
        initSelectDialogData();
    }

    private void initSelectDialogData() {
        SelectDialogItemBean bean;
        mUploadFilesSelectDialogItemBeans = new ArrayList<>(3);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.group_file_upload_image));
        bean.setAlert(false);
        bean.setIcon(R.drawable.upload_image);
        bean.setId(MENU_ID_UPLOAD_IMAGE);
        mUploadFilesSelectDialogItemBeans.add(bean);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.group_file_upload_video));
        bean.setIcon(R.drawable.upload_video);
        bean.setId(MENU_ID_UPLOAD_VIDEO);
        mUploadFilesSelectDialogItemBeans.add(bean);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.group_file_upload_file));
        bean.setIcon(R.drawable.upload_file);
        bean.setId(MENU_ID_UPLOAD_FILE);
        mUploadFilesSelectDialogItemBeans.add(bean);

    }

    @Override
    public void onRightClick(View view) {
        SelectDialog dialog = new SelectDialog(mContext);
        dialog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SelectDialogItemBean bean = mUploadFilesSelectDialogItemBeans.get(position);
                executeUploadFileAction(bean);
            }


        });
        dialog.setData(mUploadFilesSelectDialogItemBeans);
        dialog.init();
        new EaseAlertDialog.Builder<SelectDialog>(mContext)
                .setCustomDialog(dialog)
                .setFullWidth()
                .setGravity(Gravity.BOTTOM)
                .setFromBottomAnimation()
                .show();

    }

    private void executeUploadFileAction(SelectDialogItemBean item) {
        switch (item.getId()) {
            case MENU_ID_UPLOAD_IMAGE:
                if(!PermissionCompat.checkMediaPermission(mContext, requestImagePermission, Manifest.permission.READ_MEDIA_IMAGES)) {
                    return;
                }
                selectPicFromLocal();
                break;
            case MENU_ID_UPLOAD_VIDEO:
                if(!PermissionCompat.checkMediaPermission(mContext, requestVideoPermission, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.CAMERA)) {
                    return;
                }
                selectVideoFromLocal();
                break;
            case MENU_ID_UPLOAD_FILE:
                if(!PermissionCompat.checkMediaPermission(mContext, requestFilePermission, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)) {
                    return;
                }
                selectFileFromLocal();
                break;
        }

    }

    private void selectPicFromLocal() {
        EaseCompat.openImage(launcherToAlbum, mContext);
    }

    private void selectVideoFromLocal() {
        EaseActivityProviderHelper.startToImageGridActivity(launcherToVideo, mContext);
    }

    private void selectFileFromLocal() {
        Intent intent = new Intent();
        if (VersionUtils.isTargetQ(mContext)) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            } else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        launcherToFile.launch(intent);
    }

    private boolean isAllowEdit() {
        // Group owner or admin
        if (GroupHelper.isOwner(group) || GroupHelper.isAdmin(group)) {
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        refresh();
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        loadMore();
    }

    private void refresh() {
        pageSize = LIMIT;
        viewModel.getSharedFiles(groupId, 0, pageSize);
    }

    private void loadMore() {
        pageSize += LIMIT;
        viewModel.getSharedFiles(groupId, 0, pageSize);
    }

    private void finishRefresh() {
        runOnUiThread(() -> binding.srlRefresh.finishRefresh());
    }

    private void finishLoadMore() {
        runOnUiThread(() -> binding.srlRefresh.finishLoadMore());
    }

    private void deleteFile(MucSharedFile file) {
        viewModel.deleteFile(groupId, file);
    }

    private void showFile(MucSharedFile item) {
        viewModel.showFile(groupId, item);
    }

    private void openFile(File file) {
        if (file != null && file.exists()) {
            EaseCompat.openFile(file, mContext);
        }
    }

    private void shareFile(File file) {
        if (file != null && file.exists()) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setType(EaseCompat.getMimeType(this.getApplicationContext(), file));
                intent.putExtra(Intent.EXTRA_STREAM, EaseCompat.getUriForFile(this.getApplicationContext(), file));
                Intent chooser = Intent.createChooser(intent, file.getName());
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void onRequestResult(Map<String, Boolean> result, int requestCode) {
        if(result != null && result.size() > 0) {
            for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                EMLog.e("chat", "onRequestResult: " + entry.getKey() + "  " + entry.getValue());
            }
            if(PermissionCompat.getMediaAccess(mContext) != PermissionCompat.StorageAccess.Denied) {
                if(requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                    selectPicFromLocal();
                }else if(requestCode == REQUEST_CODE_STORAGE_VIDEO) {
                    selectVideoFromLocal();
                }else if(requestCode == REQUEST_CODE_STORAGE_FILE) {
                    selectFileFromLocal();
                }
            }
        }
    }

    private void onActivityResult(ActivityResult result, int requestCode) {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (requestCode == REQUEST_CODE_SELECT_IMAGE || requestCode == REQUEST_CODE_SELECT_FILE) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        String filePath = EaseFileUtils.getFilePath(mContext, uri);
                        if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                            sendByPath(Uri.parse(filePath));
                        } else {
                            EaseFileUtils.saveUriPermission(mContext, uri, data);
                            viewModel.uploadFileByUri(groupId, uri);
                        }
                    }
                }
            } else if (requestCode == REQUEST_CODE_SELECT_VIDEO) {
                if (data != null) {
                    String videoPath = data.getStringExtra("path");
                    String uriString = data.getStringExtra("uri");
                    if (!TextUtils.isEmpty(videoPath)) {
                        sendByPath(Uri.parse(videoPath));
                    } else {
                        Uri videoUri = FileHelper.getInstance().formatInUri(uriString);
                        EaseFileUtils.saveUriPermission(mContext, videoUri, data);
                        viewModel.uploadFileByUri(groupId, videoUri);
                    }
                }
            }
        }
    }

    private void sendByPath(Uri uri) {
        String path = EaseCompat.getPath(this, uri);
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, R.string.file_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.uploadFileByUri(groupId, Uri.parse(path));
    }

    private void search(String content) {
        mSearchContent = content;
        if (TextUtils.isEmpty(content)) {
            adapter.setData(mLastData);
        } else {
            List<MucSharedFile> fileList = new ArrayList<>(mLastData);
            Iterator<MucSharedFile> iterator = fileList.iterator();
            MucSharedFile file;
            while (iterator.hasNext()) {
                file = iterator.next();
                if (!file.getFileName().contains(content)) {
                    iterator.remove();
                }
            }

            adapter.setData(fileList);
        }
    }

    private static class FileSpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public FileSpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.top = space;
        }
    }
}
