package io.agora.chatdemo.general.widget;


import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;

public class MembersScrollViewHeadView extends HorizontalScrollView {

    private LinearLayout container;
    private OnMembersChangeListener listener;
    private List<String> groupMembers;

    public MembersScrollViewHeadView(Context context) {
        this(context, null);
    }

    public MembersScrollViewHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MembersScrollViewHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        addView(container);
    }

    // TODO: 2021/11/7 Optimize later
    public void setMembers(List<String> members) {
        if(container != null) {
            container.removeAllViews();
        }
        setChildView(members, true);
        if(groupMembers != null && !groupMembers.isEmpty()) {
            setChildView(groupMembers, false);
        }
    }

    private void setChildView(List<String> data, boolean canDelete) {
        if(container == null || data == null || data.isEmpty()) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            if(TextUtils.equals(data.get(i), DemoHelper.getInstance().getCurrentUser())) {
                continue;
            }
            View view=LayoutInflater.from(getContext()).inflate(R.layout.layout_contact_avatar_name,container,false);
            container.addView(view);
            EaseImageView ivAvatar = view.findViewById(R.id.iv_avatar);
            TextView tvName = view.findViewById(R.id.tv_name);
            ImageView ivDelete = view.findViewById(R.id.iv_delete);
            EaseUserUtils.setUserAvatarStyle(ivAvatar);
            DemoHelper.getInstance().setUserInfo(getContext(), data.get(i), tvName, ivAvatar);
            if(!canDelete) {
                ivDelete.setVisibility(GONE);
            }
            String member = data.get(i);
            ivDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.remove(member);
                    container.removeView(view);
                    listener.onMembersChange(data);
                }
            });
        }
    }

    public void setGroupMembers(List<String> groupMembers) {
        if(container == null || groupMembers == null || groupMembers.isEmpty()) {
            return;
        }
        this.groupMembers = groupMembers;
        int childCount = container.getChildCount();
        if(childCount == 0) {
            setChildView(groupMembers, false);
        }else {
            for(int i = 0; i < childCount; i++) {
                View child = container.getChildAt(i);
                ImageView ivDelete = child.findViewById(R.id.iv_delete);
                TextView tvName = child.findViewById(R.id.tv_name);
                if(groupMembers.contains(tvName.getText().toString().trim())) {
                    ivDelete.setVisibility(GONE);
                }
            }
        }
    }

    public interface OnMembersChangeListener {
        void onMembersChange(List<String> selectedMembers);
    }

    public void setOnMembersChangeListener(OnMembersChangeListener listener) {
        this.listener = listener;
    }
}
