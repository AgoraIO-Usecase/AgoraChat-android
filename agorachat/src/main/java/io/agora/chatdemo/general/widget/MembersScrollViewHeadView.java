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

import com.bumptech.glide.Glide;

import java.util.List;

import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.R;

public class MembersScrollViewHeadView extends HorizontalScrollView {

    private LinearLayout container;
    private EaseUserProfileProvider provider;
    private OnMembersChangeListener listener;

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
        provider = EaseUIKit.getInstance().getUserProvider();
    }

    //后期再优化逻辑，减少不必要的view移除添加
    public void setMembers(List<String> members) {
        if(members==null) {
            return;
        }
        container.removeAllViews();
        for (int i = 0; i < members.size(); i++) {
            View view=LayoutInflater.from(getContext()).inflate(R.layout.layout_contact_avatar_name,container,false);
            container.addView(view);
            EaseImageView ivAvatar = view.findViewById(R.id.iv_avatar);
            TextView tvName = view.findViewById(R.id.tv_name);
            ImageView ivDelete = view.findViewById(R.id.iv_delete);
            if (provider != null) {
                EaseUser user = provider.getUser(members.get(i));
                String avatar = user.getAvatar();
                if(!TextUtils.isEmpty(avatar)) {
                    Glide.with(this).load(avatar).into(ivAvatar);
                }
            }
            tvName.setText(members.get(i));
            String member = members.get(i);
            ivDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    members.remove(member);
                    container.removeView(view);
                    listener.onMembersChange(members);
                }
            });
        }
    }

    public interface OnMembersChangeListener {
        void onMembersChange(List<String> selectedMembers);
    }

    public void setOnMembersChangeListener(OnMembersChangeListener listener) {
        this.listener = listener;
    }
}
