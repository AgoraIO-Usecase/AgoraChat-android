package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Group;
import io.agora.chat.GroupManager;
import io.agora.chat.GroupOptions;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chat.uikit.widget.EaseRecyclerView;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.contact.ContactListFragment;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.general.widget.MembersScrollViewHeadView;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.group.viewmodel.NewGroupViewModel;

public class NewGroupSelectContactsFragment extends ContactListFragment implements BottomSheetChildHelper,
        MembersScrollViewHeadView.OnMembersChangeListener, ContactListAdapter.OnSelectListener {

    protected MembersScrollViewHeadView headContainer;
    private String groupName;
    private String groupDesc;
    private String reason;
    private boolean groupPublic;
    private boolean groupAllowInvite;
    protected NewGroupViewModel viewModel;
    private int groupMaxUsers;

    @Override
    protected void initArgument() {
        super.initArgument();

        Bundle bundle=getArguments();
        if(bundle == null) {
            return;
        }
        groupName = bundle.getString(DemoConstant.GROUP_NAME);
        groupDesc = bundle.getString(DemoConstant.GROUP_DESC);
        reason = bundle.getString(DemoConstant.GROUP_REASON);
        groupPublic = bundle.getBoolean(DemoConstant.GROUP_PUBLIC);
        groupAllowInvite = bundle.getBoolean(DemoConstant.GROUP_ALLOW_INVITE);
        groupMaxUsers = bundle.getInt(DemoConstant.GROUP_MAX_USERS);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        addMemebersListHeadView();
        addContactHeadView();

        mRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(this).get(NewGroupViewModel.class);
        viewModel.groupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    showToast(R.string.group_new_success);
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                    // Skip to chat activity
                    ChatActivity.actionStart(mContext, data.getGroupId(), EaseChatType.GROUP_CHAT);
                    hide();
                }

                @Override
                public void onLoading(Group data) {
                    super.onLoading(data);
                    showLoading(getString(R.string.request));
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }
            });
        });
    }

    private void addMemebersListHeadView() {
        AbsListView.LayoutParams headviewParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headContainer = new MembersScrollViewHeadView(mContext);
        headContainer.setPadding(0, UIUtils.dp2px(mContext,10),0,UIUtils.dp2px(mContext,10));
        headContainer.setLayoutParams(headviewParams);
        ((EaseRecyclerView) mRecyclerView).addHeaderView(headContainer);
    }

    private void addContactHeadView() {
        AbsListView.LayoutParams contactsParams = new AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView contacts = new TextView(mContext);
        contacts.setText(getHeadName());
        contacts.setLayoutParams(contactsParams);
        contacts.setGravity(Gravity.LEFT);
        contacts.setTextSize(UIUtils.getSpDimen(mContext, R.dimen.text_size_small));
        contacts.setPadding((int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2));
        contacts.setTextColor(ContextCompat.getColor(mContext, R.color.color_light_gray_999999));
        ((EaseRecyclerView) mRecyclerView).addHeaderView(contacts);
    }

    protected String getHeadName(){
        return getString(R.string.group_contacts);
    }

    @Override
    protected void initData() {
        super.initData();
        ((ContactListAdapter) mListAdapter).setCheckModel(true);
    }

    @Override
    protected void initListener() {
        super.initListener();
        ((ContactListAdapter) mListAdapter).setOnSelectListener(this);
        headContainer.setOnMembersChangeListener(this);
    }

    @Override
    public boolean onTitlebarRightTextViewClick() {
        List<String> checkedList = ((ContactListAdapter) mListAdapter).getCheckedList();
        if(checkedList==null) {
            checkedList=new ArrayList<>();
        }
        GroupOptions option = new GroupOptions();
        option.maxUsers = groupMaxUsers;
        option.inviteNeedConfirm = true;
        if(groupPublic){
            option.style = groupAllowInvite ? GroupManager.GroupStyle.GroupStylePublicJoinNeedApproval : GroupManager.GroupStyle.GroupStylePublicOpenJoin;
        }else{
            option.style = groupAllowInvite ? GroupManager.GroupStyle.GroupStylePrivateMemberCanInvite : GroupManager.GroupStyle.GroupStylePrivateOnlyOwnerInvite;
        }
        viewModel.createGroup(groupName, groupDesc, checkedList.toArray(new String[checkedList.size()]), reason, option);
        return true;
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.group_create_title;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.group_add_members;
    }

    @Override
    public void onSelected(View v, List<String> selectedMembers) {
        headContainer.setMembers(selectedMembers);
    }

    @Override
    public void onMembersChange(List<String> selectedMembers) {
        ((ContactListAdapter) mListAdapter).setSelectedMembers(selectedMembers);
    }
}
