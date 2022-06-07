package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;

import java.util.ArrayList;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.widget.EaseRecyclerView;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListFragment;
import io.agora.chatdemo.contact.SearchContactFragment;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.general.widget.ArrowItemView;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.group.adapter.HomeHeaderMenuAdapter;

public class BottomSheetMenuFragment extends ContactListFragment implements BottomSheetChildHelper {
    private ArrayList<Pair<Integer, Integer>> datas = new ArrayList();
    private HomeHeaderMenuAdapter headerMenuAdapter;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        sideBarContact.setVisibility(View.GONE);
    }

    @Override
    public void addHeader(ConcatAdapter adapter) {
        super.addHeader(adapter);
        initHeaderData();
        headerMenuAdapter = new HomeHeaderMenuAdapter();
        adapter.addAdapter(0, headerMenuAdapter);
        headerMenuAdapter.setData(datas);
        mRecyclerView.setNestedScrollingEnabled(false);

        LinearLayout.LayoutParams contactsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView contacts = new TextView(mContext);
        contacts.setText(R.string.group_contacts);
        contacts.setLayoutParams(contactsParams);
        contacts.setGravity(Gravity.LEFT);
        contacts.setTextSize(UIUtils.getSpDimen(mContext, R.dimen.text_size_small));
        contacts.setPadding((int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2));
        contacts.setTextColor(ContextCompat.getColor(mContext, R.color.color_light_gray_999999));
    }

    @Override
    protected void initListener() {
        super.initListener();
        headerMenuAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position) {
                    case 0 :
                        startFragment(new NewGroupSettingFragment(), null);
                        break;
                    case 1 :
                        startFragment(new SearchGroupFragment(),null);
                        break;
                    case 2 :
                        startFragment(new PublicGroupFragment(),null);
                        break;
                    case 3 :
                        startFragment(new SearchContactFragment(),null);
                        break;
                }
            }
        });
    }

    private void initHeaderData() {
        datas.clear();
        datas.add(new Pair(R.drawable.new_group, R.string.group_new_group));
        datas.add(new Pair(R.drawable.join_a_group, R.string.group_join_a_group));
        datas.add(new Pair(R.drawable.public_group, R.string.group_public_group_list));
        datas.add(new Pair(R.drawable.group_add_contacts, R.string.group_add_contacts));
    }

    protected void checkView(String content) {
        super.checkView(content);
        sideBarContact.setVisibility(View.GONE);
        if(TextUtils.isEmpty(content)) {
            initHeaderData();
            headerMenuAdapter.setData(datas);
            mRecyclerView.setNestedScrollingEnabled(false);
        }else{
            datas.clear();
            headerMenuAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.group_create_title;
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.ease_cancel;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.group_blue_154dfe;
    }
}
