package io.agora.chatdemo.group;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

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
    }

    @Override
    protected void initData() {
        super.initData();
        datas.add(new Pair(R.drawable.add, R.string.group_new_group));
        datas.add(new Pair(R.drawable.add, R.string.group_join_a_group));
        datas.add(new Pair(R.drawable.add, R.string.group_public_group_list));
        datas.add(new Pair(R.drawable.add, R.string.group_add_contacts));

        LinearLayout headView = new LinearLayout(mContext);
        headView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) CommonUtils.getDimen(mContext,R.dimen.common_arrow_item_view_height));
        for (Pair<Integer, Integer> data : datas) {
            ArrowItemView itemView = new ArrowItemView(mContext);
            itemView.setAvatar(data.first);
            itemView.setAvatarHeight(UIUtils.dp2px(mContext, (int) CommonUtils.getDimen(mContext, R.dimen.common_arrow_item_avatar_height)));
            itemView.setAvatarWidth(UIUtils.dp2px(mContext, (int) CommonUtils.getDimen(mContext, R.dimen.common_arrow_item_avatar_height)));
            itemView.setTitle(CommonUtils.getString(mContext, data.second));
            itemView.setTitleSize(UIUtils.px2dp(mContext, (int) CommonUtils.getDimen(mContext, R.dimen.text_size_big)));
            itemView.setArrow(R.drawable.arrow_right);
            itemView.setLayoutParams(params);
            headView.addView(itemView);
        }

        ((EaseRecyclerView)mRecyclerView).addHeaderView(headView);

        mRecyclerView.setNestedScrollingEnabled(false);
    }
}
