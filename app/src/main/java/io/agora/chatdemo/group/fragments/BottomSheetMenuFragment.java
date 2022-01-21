package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import io.agora.chat.uikit.widget.EaseRecyclerView;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListFragment;
import io.agora.chatdemo.contact.SearchContactFragment;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.general.widget.ArrowItemView;
import io.agora.chatdemo.global.BottomSheetChildHelper;

public class BottomSheetMenuFragment extends ContactListFragment implements View.OnClickListener, BottomSheetChildHelper {
    private ArrayList<Pair<Integer, Integer>> datas = new ArrayList();
    private LinearLayout headView;

    @Override
    protected void initArgument() {
        super.initArgument();
        datas.clear();
        datas.add(new Pair(R.drawable.new_group, R.string.group_new_group));
        datas.add(new Pair(R.drawable.join_a_group, R.string.group_join_a_group));
        datas.add(new Pair(R.drawable.public_group, R.string.group_public_group_list));
        datas.add(new Pair(R.drawable.group_add_contacts, R.string.group_add_contacts));

        AbsListView.LayoutParams headviewParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headView = new LinearLayout(mContext);
        headView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams itemViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) UIUtils.getAbsDimen(mContext, R.dimen.common_arrow_item_view_height));
        for (Pair<Integer, Integer> data : datas) {
            ArrowItemView itemView = new ArrowItemView(mContext);
            itemView.setAvatar(data.first);
            itemView.setAvatarVisiablity(View.VISIBLE);
            itemView.setAvatarMargin(UIUtils.dp2px(mContext, 0), UIUtils.dp2px(mContext, 7), UIUtils.dp2px(mContext, 6), UIUtils.dp2px(mContext, 7));
            itemView.setAvatarHeight(UIUtils.dp2px(mContext, 40));
            itemView.setAvatarWidth(UIUtils.dp2px(mContext, 40));
            itemView.setTitle(UIUtils.getString(mContext, data.second));
            itemView.setTitleSize(UIUtils.px2dp(mContext, (int) UIUtils.getAbsDimen(mContext, R.dimen.text_size_big)));
            itemView.setArrow(R.drawable.arrow_right);
            itemView.setLayoutParams(itemViewParams);
            itemView.setTag(data.first);
            itemView.setOnClickListener(this);
            headView.addView(itemView);
        }
        headView.setLayoutParams(headviewParams);

        LinearLayout.LayoutParams contactsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView contacts = new TextView(mContext);
        contacts.setText(R.string.group_contacts);
        contacts.setLayoutParams(contactsParams);
        contacts.setGravity(Gravity.LEFT);
        contacts.setTextSize(UIUtils.getSpDimen(mContext, R.dimen.text_size_small));
        contacts.setPadding((int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_15), (int) UIUtils.getAbsDimen(mContext, R.dimen.margin_2));
        contacts.setTextColor(ContextCompat.getColor(mContext, R.color.color_light_gray_999999));
        headView.addView(contacts);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        sideBarContact.setVisibility(View.GONE);
    }

    protected void checkSearchContent(String content) {
        super.checkSearchContent(content);
        sideBarContact.setVisibility(View.GONE);
        if(TextUtils.isEmpty(content)) {
            if(headView.getParent()==null) {
                ((EaseRecyclerView) mRecyclerView).addHeaderView(headView);
                mRecyclerView.setNestedScrollingEnabled(false);
            }
        }else{
            ((EaseRecyclerView) mRecyclerView).removeHeaderViews();
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ArrowItemView) {
            int tag = (int) v.getTag();
            switch (tag) {
                case R.drawable.new_group:
                    startFragment(new NewGroupSettingFragment(), null);
                    break;
                case R.drawable.join_a_group:
                    startFragment(new SearchGroupFragment(),null);
                    break;
                case R.drawable.public_group:
                    startFragment(new PublicGroupFragment(),null);
                    break;
                case R.drawable.group_add_contacts:
                    startFragment(new SearchContactFragment(),null);
                    break;
            }
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
