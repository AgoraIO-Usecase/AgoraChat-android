package io.agora.chatdemo.group;

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
import io.agora.chatdemo.general.utils.CommonUtils;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.general.widget.ArrowItemView;

/**
 * Created by 许成谱 on 2021/10/26 0026 12:10.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class GroupCreateFragment extends ContactListFragment {
    private ArrayList<Pair<Integer, Integer>> datas = new ArrayList();

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        sideBarContact.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        super.initData();
        datas.add(new Pair(R.drawable.new_group, R.string.group_add_contacts));
        datas.add(new Pair(R.drawable.join_a_group, R.string.group_join_a_group));
        datas.add(new Pair(R.drawable.public_group, R.string.group_public_group_list));
        datas.add(new Pair(R.drawable.group_add_contacts, R.string.group_add_contacts));

        AbsListView.LayoutParams headviewParams=new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout headView = new LinearLayout(mContext);
        headView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams itemViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) CommonUtils.getAbsDimen(mContext,R.dimen.common_arrow_item_view_height));
        for (Pair<Integer, Integer> data : datas) {
            ArrowItemView itemView = new ArrowItemView(mContext);
            itemView.setAvatar(data.first);
            itemView.setAvatarVisiablity(View.VISIBLE);
            itemView.setAvatarMargin(UIUtils.dp2px(mContext,0),UIUtils.dp2px(mContext,7),UIUtils.dp2px(mContext,6),UIUtils.dp2px(mContext,7));
            itemView.setAvatarHeight(UIUtils.dp2px(mContext,40));
            itemView.setAvatarWidth(UIUtils.dp2px(mContext, 40));
            itemView.setTitle(CommonUtils.getString(mContext, data.second));
            itemView.setTitleSize(UIUtils.px2dp(mContext, (int) CommonUtils.getAbsDimen(mContext, R.dimen.text_size_big)));
            itemView.setArrow(R.drawable.arrow_right);
            itemView.setLayoutParams(itemViewParams);
            headView.addView(itemView);
        }
        headView.setLayoutParams(headviewParams);

        LinearLayout.LayoutParams contactsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView contacts=new TextView(mContext);
        contacts.setText(R.string.group_contacts);
        contacts.setLayoutParams(contactsParams);
        contacts.setGravity(Gravity.LEFT);
        contacts.setTextSize(CommonUtils.getSpDimen(mContext,R.dimen.text_size_small));
        contacts.setPadding((int)CommonUtils.getAbsDimen(mContext,R.dimen.margin_15),(int)CommonUtils.getAbsDimen(mContext,R.dimen.margin_2),(int)CommonUtils.getAbsDimen(mContext,R.dimen.margin_15),(int)CommonUtils.getAbsDimen(mContext,R.dimen.margin_2));
        contacts.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_999999));
        headView.addView(contacts);
        ((EaseRecyclerView)mRecyclerView).addHeaderView(headView);

        mRecyclerView.setNestedScrollingEnabled(false);
    }
    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            srlContactRefresh.setEnabled(true);
        }else {
            srlContactRefresh.setEnabled(false);
        }
    }
}
