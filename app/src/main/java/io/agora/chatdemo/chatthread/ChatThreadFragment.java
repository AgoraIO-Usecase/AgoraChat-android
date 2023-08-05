package io.agora.chatdemo.chatthread;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;


import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.interfaces.IChatTopExtendMenu;
import io.agora.chat.uikit.chat.widget.EaseChatMultiSelectView;
import io.agora.chat.uikit.chatthread.EaseChatThreadFragment;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chat.uikit.menu.MenuItemBean;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.ToastUtils;

public class ChatThreadFragment extends EaseChatThreadFragment {

    @Override
    public void initData() {
        super.initData();
        LiveDataBus.get().with(DemoConstant.EVENT_CHAT_MODEL_TO_NORMAL, EaseEvent.class).observe(this, event-> {
            if(event == null) {
                return;
            }
            if(event.type == EaseEvent.TYPE.NOTIFY && TextUtils.equals(event.message, "chatThread")) {
                showNormalModelTitle();
            }
        });
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        boolean isRecall = message.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false);
        if(isRecall) {
            helper.showHeaderView(false);
            helper.findItemVisible(R.id.action_chat_delete, true);
            helper.findItemVisible(R.id.action_chat_unsent, false);
            helper.findItemVisible(R.id.action_chat_recall, false);
            helper.findItemVisible(R.id.action_chat_copy, false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, ChatMessage message) {
        if(item.getItemId() == R.id.action_chat_select) {
            showSelectModelTitle();
        }
        return super.onMenuItemClick(item, message);
    }

    @Override
    public void recallSuccess(ChatMessage originalMessage, ChatMessage notification) {
        super.recallSuccess(originalMessage, notification);
        ToastUtils.showToast(R.string.thread_unsent_message_success);
    }

    @Override
    public void recallFail(int code, String errorMsg) {
        super.recallFail(code, errorMsg);
        ToastUtils.showToast(errorMsg);
    }

    private void showSelectModelTitle() {
        titleBar.setDisplayHomeAsUpEnabled(false);
        titleBar.setRightTitle(getString(R.string.ease_cancel));
        titleBar.getRightText().setTextColor(ContextCompat.getColor(mContext, R.color.color_action_text));
        titleBar.getRightText().setVisibility(View.VISIBLE);
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                showNormalModelTitle();
                IChatTopExtendMenu chatTopExtendMenu = chatLayout.getChatInputMenu().getChatTopExtendMenu();
                if(chatTopExtendMenu instanceof EaseChatMultiSelectView) {
                    ((EaseChatMultiSelectView) chatTopExtendMenu).dismiss();
                }
            }
        });
        ViewParent parent = titleBar.getTitle().getParent();
        if(parent instanceof ViewGroup) {
            ViewGroup.LayoutParams params = ((ViewGroup) parent).getLayoutParams();
            if(params instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) params).leftMargin = (int) EaseUtils.dip2px(mContext, 12);
            }
        }
    }

    private void showNormalModelTitle() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            boolean canBack = bundle.getBoolean("key_enable_back", false);
            titleBar.setDisplayHomeAsUpEnabled(canBack);
        }
        titleBar.getRightText().setVisibility(View.GONE);
        ViewParent parent = titleBar.getTitle().getParent();
        if(parent instanceof ViewGroup) {
            ViewGroup.LayoutParams params = ((ViewGroup) parent).getLayoutParams();
            if(params instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) params).leftMargin = (int) EaseUtils.dip2px(mContext, 60);
            }
        }
    }
}
