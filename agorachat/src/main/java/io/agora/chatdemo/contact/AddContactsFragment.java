package io.agora.chatdemo.contact;

import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BottomSheetChildHelper;
import io.agora.chatdemo.contact.viewmodels.AddContactViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.db.DemoDbHelper;

public class AddContactsFragment extends SearchFragment<String> implements AddContactAdapter.OnItemSubViewClickListener, BottomSheetChildHelper {
    private AddContactViewModel mViewModel;
    @Override
    protected void initData() {
        super.initData();
        mViewModel = new ViewModelProvider(mContext).get(AddContactViewModel.class);
        mViewModel.getAddContact().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    showToast(getResources().getString(R.string.em_add_contact_send_successful));
                }
            });

        });
        //获取本地的好友列表
        List<String> localUsers = null;
        if(DemoDbHelper.getInstance(mContext).getUserDao() != null) {
            localUsers = DemoDbHelper.getInstance(mContext).getUserDao().loadContactUsers();
        }
        ((AddContactAdapter)mListAdapter).addLocalContacts(localUsers);

        mListAdapter.setOnItemSubViewClickListener(this);
    }

    @Override
    protected EaseBaseRecyclerViewAdapter<String> initAdapter() {
        return new AddContactAdapter();
    }

    @Override
    public void searchText(String query) {
        // you can search the user from your app server here.
        if(!TextUtils.isEmpty(query)) {
            if (mListAdapter.getData() != null && !mListAdapter.getData().isEmpty()) {
                mListAdapter.clearData();
            }
            mListAdapter.addData(query);
        }
    }


    @Override
    public void onItemSubViewClick(View view, int position) {
        // 添加好友
        mViewModel.addContact((String) mListAdapter.getItem(position), getResources().getString(R.string.em_add_contact_add_a_friend));
    }

    @Override
    public void onItemClick(View view, int position) {
        // 跳转到好友页面
        String username = (String) mListAdapter.getItem(position);
        ContactDetailActivity.actionStart(mContext, username);
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.cancel;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.group_blue_154dfe;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.contact_add_contacts;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }
}
