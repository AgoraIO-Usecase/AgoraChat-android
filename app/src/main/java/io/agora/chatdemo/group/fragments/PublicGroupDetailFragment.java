package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.Group;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
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
            groupId = arguments.getString("group_id");
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
                    if (true) {
                        showToast(getResources().getString(R.string.group_application_send));
                        back();
                    }

                }
            });
        });
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Group>() {
                @Override
                public void onSuccess(@Nullable Group data) {
                    group = data;
                    setGroupView();
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

    private void setGroupView() {
        if (group == null) {
            return;
        }
        mBinding.layoutUserinfo.tvId.setText(getString(R.string.show_agora_chat_id, groupId));
        mBinding.tvDescription.setText(group.getDescription());
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
                    String reason = getString(R.string.demo_group_listener_onRequestToJoinReceived, DemoHelper.getInstance().getUsersManager().getCurrentUserID(), group.getGroupName());
                    viewModel.joinGroup(group, reason);
                }
                break;
        }
    }
}
