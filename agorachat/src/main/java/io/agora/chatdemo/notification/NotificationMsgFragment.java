package io.agora.chatdemo.notification;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.notification.viewmodels.NewFriendsViewModel;
import io.agora.chatdemo.notification.viewmodels.NotifiationMsgsViewModel;

/**
 * Created by 许成谱 on 2021/10/27 0027 16:04.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class NotificationMsgFragment extends BaseContactListFragment<ChatMessage> implements EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener {
    private NotifiationMsgsViewModel mMsgsViewModel;
    private NewFriendsViewModel mNewFriendViewModel;
    private List<ChatMessage> mData;
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        sideBarContact.setVisibility(View.GONE);
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mMsgsViewModel = new ViewModelProvider(this).get(NotifiationMsgsViewModel.class);
        mNewFriendViewModel=new ViewModelProvider(this).get(NewFriendsViewModel.class);
        mMsgsViewModel.getChatMessageObservable().observe(this, datas -> {
            srlContactRefresh.setRefreshing(false);
            mData = datas;
            mListAdapter.setData(datas);
        });

        mNewFriendViewModel.agreeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    mMsgsViewModel.getAllMessages();
                    EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
                    LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE).postValue(event);
                }
            });
        });
        mNewFriendViewModel.refuseObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    mMsgsViewModel.getAllMessages();
                    EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
                    LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE).postValue(event);
                }
            });
        });
        LiveDataBus messageChange = mMsgsViewModel.getMessageChange();
        messageChange.with(DemoConstant.NOTIFY_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadList);
        messageChange.with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadList);
        messageChange.with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadList);
        messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadList);
    }

    private void loadList(EaseEvent change) {
        if(change == null) {
            return;
        }
        if(change.isMessageChange() || change.isNotifyChange()
                || change.isGroupLeave() || change.isChatRoomLeave()
                || change.isContactChange()
                || change.type == EaseEvent.TYPE.CHAT_ROOM || change.isGroupChange()) {
            mMsgsViewModel.getAllMessages();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mNewFriendViewModel.makeAllMsgRead();
    }

    @Override
    protected void initData() {
        super.initData();
        mMsgsViewModel.getAllMessages();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mListAdapter.setOnItemSubViewClickListener(this);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mMsgsViewModel.getAllMessages();
    }

    @Override
    protected void searchText(String content) {
        checkSearchContent(content);
    }
    private void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            mListAdapter.setData(mData);
            sideBarContact.setVisibility(View.VISIBLE);
            srlContactRefresh.setEnabled(true);
        }else {
            mMsgsViewModel.searchMsgs(content);
            sideBarContact.setVisibility(View.GONE);
            srlContactRefresh.setEnabled(false);
        }
    }
    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    protected EaseBaseRecyclerViewAdapter<ChatMessage> initAdapter() {
        return new NotaficationMsgAdapter();
    }

    @Override
    public void onItemSubViewClick(View view, int position) {
        switch (view.getId()) {
            case R.id.btn_accept :
                mNewFriendViewModel.agreeInvite(mData.get(position));
                break;
            case  R.id.iv_msg_delete:
                mNewFriendViewModel.refuseInvite(mData.get(position));
                mNewFriendViewModel.deleteMsg(mData.get(position));
                break;
        }
    }
}
