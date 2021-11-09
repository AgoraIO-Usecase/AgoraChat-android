package io.agora.chatdemo.conversation;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import io.agora.chat.Conversation;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.global.BottomSheetContainerFragment;

public class ConversationListFragment extends EaseConversationListFragment {

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar.setRightImageResource(R.drawable.main_add_top);
        titleBar.setRightLayoutVisibility(View.VISIBLE);
        // Set toolbar's icon
        EaseImageView icon = titleBar.getIcon();
        titleBar.setIcon(R.drawable.chat_toolbar_icon);
        icon.setShapeType(0);
        ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
        layoutParams.height = (int) EaseUtils.dip2px(mContext, 20);
        layoutParams.width = (int) EaseUtils.dip2px(mContext, 65);
    }

    @Override
    public void initListener() {
        super.initListener();
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                new BottomSheetContainerFragment().show(getChildFragmentManager(),"ContainerFragment");
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        EaseConversationInfo item = conversationListLayout.getListAdapter().getItem(position);
        if(item.getInfo() instanceof Conversation) {
            ChatActivity.actionStart(mContext, ((Conversation) item.getInfo()).conversationId(), EaseUtils.getChatType((Conversation) item.getInfo()));
        }

    }
}
