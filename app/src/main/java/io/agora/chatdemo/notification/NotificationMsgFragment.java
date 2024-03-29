package io.agora.chatdemo.notification;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.util.Collections;
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
import io.agora.chatdemo.notification.viewmodels.NotificationMsgsViewModel;

public class NotificationMsgFragment extends BaseContactListFragment<ChatMessage> implements EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener {
    private NotificationMsgsViewModel mMsgsViewModel;
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
        mMsgsViewModel = new ViewModelProvider(this).get(NotificationMsgsViewModel.class);
        mNewFriendViewModel=new ViewModelProvider(this).get(NewFriendsViewModel.class);
        mMsgsViewModel.getChatMessageObservable().observe(this, datas -> {
            srlContactRefresh.setRefreshing(false);
            Collections.reverse(datas);
            mData = datas;
            mListAdapter.setData(datas);
        });

        mMsgsViewModel.getSearchResultObservable().observe(this, response -> {
            mListAdapter.setData(response);
        });

        mNewFriendViewModel.agreeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String message) {
                }
                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                }
            });
        });
        mNewFriendViewModel.refuseObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String message) {
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
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
        mMsgsViewModel.getAllMessages();
        if(this.isVisible()) {
            mNewFriendViewModel.makeAllMsgRead();
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
            srlContactRefresh.setEnabled(true);
        }else {
            mMsgsViewModel.searchMsgs(content);
            srlContactRefresh.setEnabled(false);
        }
    }
    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    protected EaseBaseRecyclerViewAdapter<ChatMessage> initAdapter() {
        return new NotificationMsgAdapter();
    }

    @Override
    public void onItemSubViewClick(View view, int position) {
        switch (view.getId()) {
            case R.id.btn_accept :
                mNewFriendViewModel.agreeInvite(mData.get(position));
                break;
            case  R.id.iv_msg_delete:
                mNewFriendViewModel.refuseInvite(mData.get(position));
                break;
        }
    }
}
