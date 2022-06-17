package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.Error;
import io.agora.chat.Group;
import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.databinding.FragmentPublicGroupDetailBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;


public class PublicGroupDetailFragment extends BaseInitFragment implements BottomSheetChildHelper, View.OnClickListener {

    private FragmentPublicGroupDetailBinding mBinding;
    private TextView rightTitle;
    private String groupId;
    private GroupDetailViewModel viewModel;
    private Group group;
    private GroupInfo groupInfo;

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentPublicGroupDetailBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle arguments = getArguments();
        if(arguments!=null) {
            groupInfo = (GroupInfo) arguments.getSerializable("group");
            groupId = groupInfo.getGroupId();
        }
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getJoinObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    showToast(getResources().getString(R.string.group_application_send));
                    EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(event);
                    back();
                }

                @Override
                public void onLoading(@Nullable Boolean data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    if (code == Error.GROUP_ALREADY_JOINED)
                    ChatActivity.actionStart(mContext, groupId, EaseChatType.GROUP_CHAT);
                }
            });
        });
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Group>() {
                @Override
                public void onSuccess(@Nullable Group data) {
                    group = data;
                    if(group != null) {
                        setGroupView(group.getGroupId(), group.getGroupName(), group.getDescription());
                    }
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    viewModel.getGroup(groupId);
                }
            });
        });
        viewModel.getLeaveGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    back();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_LEAVE, EaseEvent.TYPE.GROUP, groupId));
                }
            });
        });
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                back();
                return;
            }
            if (event.isGroupChange()) {
                viewModel.getGroup(groupId);
            }
        });
    }

    private void setGroupView(String groupId, String groupName, String groupDes) {
        if(TextUtils.isEmpty(groupId)) {
            return;
        }
        mBinding.layoutUserinfo.tvId.setText(getString(R.string.show_agora_group_id, groupId));
        boolean hasSet = DemoHelper.getInstance().setGroupInfo(mContext, groupId, mBinding.layoutUserinfo.tvNickname, mBinding.layoutUserinfo.ivUserAvatar);
        if(!hasSet) {
            mBinding.layoutUserinfo.tvNickname.setText(groupName);
        }
        if(!TextUtils.isEmpty(groupDes)) {
            mBinding.tvDescription.setVisibility(View.VISIBLE);
            mBinding.tvDescription.setText(groupDes);
        }else {
            mBinding.tvDescription.setVisibility(View.GONE);
        }
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rightTitle = mBinding.itemJoin.getRightTitle();
        rightTitle.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.itemJoin.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        rightTitle.setText(R.string.group_join);
        viewModel.getGroup(groupId);
        setGroupView(groupId, groupInfo.getGroupName(), "");
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.group_public_group;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_join:
                if (group != null) {
                    String reason = getString(R.string.group_listener_onRequestToJoinReceived, DemoHelper.getInstance().getUsersManager().getCurrentUserID(), group.getGroupName());
                    viewModel.joinGroup(group, reason);
                }
                break;
        }
    }
}
