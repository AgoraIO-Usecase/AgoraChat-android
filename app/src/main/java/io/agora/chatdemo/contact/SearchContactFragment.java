package io.agora.chatdemo.contact;

import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.contact.viewmodels.AddContactViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.global.AddType;

public class SearchContactFragment extends SearchFragment<String> implements AddAdapter.OnItemSubViewClickListener, BottomSheetChildHelper {
    private AddContactViewModel mViewModel;

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(mContext).get(AddContactViewModel.class);
        mViewModel.getAddContact().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    showToast(getResources().getString(R.string.add_contact_send_successful));
                }
            });
        });
    }

    @Override
    protected void initData() {
        super.initData();

        List<String> localUsers = null;
        if (DemoDbHelper.getInstance(mContext).getUserDao() != null) {
            localUsers = DemoDbHelper.getInstance(mContext).getUserDao().loadContactUsers();
        }
        ((AddAdapter) mListAdapter).setAddedDatas(localUsers);

    }

    @Override
    protected void initListener() {
        super.initListener();
        mListAdapter.setOnItemSubViewClickListener(this);
    }

    @Override
    protected EaseBaseRecyclerViewAdapter<String> initAdapter() {
        return new AddAdapter(AddType.CONTACT);
    }

    @Override
    public void searchText(String query) {
        // you can search the user from your app server here.
        if (mListAdapter.getData() != null && !mListAdapter.getData().isEmpty()) {
            mListAdapter.clearData();
        }
        if (!TextUtils.isEmpty(query)) {
            mListAdapter.addData(query);
        }
    }


    @Override
    public void onItemSubViewClick(View view, int position) {
        mViewModel.addContact((String) mListAdapter.getItem(position), getResources().getString(R.string.add_contact_add_a_friend));
    }

    @Override
    public void onItemClick(View view, int position) {
        String username = (String) mListAdapter.getItem(position);
//        ContactDetailActivity.actionStart(mContext, username);
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.ease_cancel;
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
