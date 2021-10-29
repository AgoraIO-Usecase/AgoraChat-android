package io.agora.chatdemo.notification;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.notification.viewmodels.NotifiationMsgsViewModel;

/**
 * Created by 许成谱 on 2021/10/27 0027 16:04.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class NotificationMsgFragment extends BaseContactListFragment<ChatMessage> {
    private NotifiationMsgsViewModel mMsgsViewModel;
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
        mMsgsViewModel= new ViewModelProvider(this).get(NotifiationMsgsViewModel.class);
        mMsgsViewModel.getChatMessageObservable().observe(this,datas -> {
            srlContactRefresh.setRefreshing(false);
            mData = datas;
            mListAdapter.setData(datas);
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mMsgsViewModel.getChatMessages();
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mMsgsViewModel.getChatMessages();
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
        return super.initAdapter();
    }
}
