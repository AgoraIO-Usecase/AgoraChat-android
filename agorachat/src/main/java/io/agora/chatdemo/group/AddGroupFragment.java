package io.agora.chatdemo.group;

import android.view.View;

import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.base.BottomSheetChildHelper;
import io.agora.chatdemo.contact.SearchFragment;

class AddGroupFragment extends SearchFragment<GroupInfo> implements BottomSheetChildHelper {

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void searchText(String content) {

    }

    @Override
    protected EaseBaseRecyclerViewAdapter<GroupInfo> initAdapter() {
        return null;
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }
}
