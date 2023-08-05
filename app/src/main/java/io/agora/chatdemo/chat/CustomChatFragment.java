package io.agora.chatdemo.chat;

import static io.agora.chat.uikit.menu.EaseChatType.SINGLE_CHAT;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.LocationMessageBody;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.interfaces.IChatTopExtendMenu;
import io.agora.chat.uikit.chat.widget.EaseChatMultiSelectView;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chat.uikit.menu.MenuItemBean;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;

public class CustomChatFragment extends EaseChatFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void initView() {
        super.initView();
        MenuItemBean menuItemBean = new MenuItemBean(0, R.id.action_chat_report,99, getResources().getString(R.string.ease_action_report));
        menuItemBean.setResourceId(R.drawable.chat_item_menu_report);
        chatLayout.getMenuHelper().addItemMenu(menuItemBean);
    }

    @Override
    public void initData() {
        super.initData();
        LiveDataBus.get().with(DemoConstant.EVENT_CHAT_MODEL_TO_NORMAL, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.type == EaseEvent.TYPE.NOTIFY && TextUtils.isEmpty(event.message)) {
                IChatTopExtendMenu chatTopExtendMenu = chatLayout.getChatInputMenu().getChatTopExtendMenu();
                if(chatTopExtendMenu instanceof EaseChatMultiSelectView) {
                    ((EaseChatMultiSelectView) chatTopExtendMenu).dismissSelectView(null);
                }
                titleBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser())
                || message.getBody() instanceof LocationMessageBody
                || message.getBody() instanceof CustomMessageBody
                || message.status() != ChatMessage.Status.SUCCESS){
            helper.findItemVisible(R.id.action_chat_report,false);
        }else {
            helper.findItemVisible(R.id.action_chat_report,true);
        }
        boolean isThreadNotify = message.getBooleanAttribute(EaseConstant.EASE_THREAD_NOTIFICATION_TYPE, false);
        if(isThreadNotify) {
            helper.findItemVisible(R.id.action_chat_report,false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, ChatMessage message) {
        if(item.getItemId() == R.id.action_chat_report){
            if (message.status() == ChatMessage.Status.SUCCESS)
                ChatReportActivity.actionStart(getActivity(),message.getMsgId());
        }else if(item.getItemId() == R.id.action_chat_select) {
            showSelectModelTitle();
            LiveDataBus.get().with(DemoConstant.EVENT_CHAT_MODEL_TO_SELECT).postValue(EaseEvent.create(DemoConstant.EVENT_CHAT_MODEL_TO_SELECT, EaseEvent.TYPE.NOTIFY));
        }
        return super.onMenuItemClick(item, message);
    }

    private void showSelectModelTitle() {
        titleBar.setVisibility(View.VISIBLE);
        titleBar.setDisplayHomeAsUpEnabled(false);
        titleBar.setTitlePosition(EaseTitleBar.TitlePosition.Left);
        titleBar.setRightTitle(getString(R.string.ease_cancel));
        titleBar.getRightText().setTextColor(ContextCompat.getColor(mContext, R.color.color_action_text));
        titleBar.getIcon().setVisibility(View.VISIBLE);
        titleBar.getLeftLayout().setVisibility(View.GONE);
        ViewParent parent = titleBar.getTitle().getParent();
        if(parent instanceof ViewGroup) {
            ViewGroup.LayoutParams params = ((ViewGroup) parent).getLayoutParams();
            if(params instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) params).leftMargin = (int) EaseUtils.dip2px(mContext, 12);
            }
        }
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                LiveDataBus.get().with(DemoConstant.EVENT_CHAT_MODEL_TO_NORMAL).postValue(EaseEvent.create(DemoConstant.EVENT_CHAT_MODEL_TO_NORMAL, EaseEvent.TYPE.NOTIFY));
            }
        });
        if(chatType != SINGLE_CHAT) {
            boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, conversationId, titleBar.getTitle(), titleBar.getIcon());
            if(!hasProvided) {
                setGroupInfo();
            }
        } else {
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, conversationId, titleBar.getTitle(), titleBar.getIcon());
            titleBar.getTitle().setVisibility(View.INVISIBLE);
            titleBar.getSubTitle().setVisibility(View.INVISIBLE);
        }
    }

    private void setGroupInfo() {
        String title = "";
        if(chatType == EaseChatType.GROUP_CHAT) {
            title = GroupHelper.getGroupName(conversationId);
            titleBar.getIcon().setImageResource(R.drawable.icon);
        }else if(chatType == EaseChatType.CHATROOM) {
            titleBar.getIcon().setImageResource(R.drawable.icon);
            ChatRoom room = ChatClient.getInstance().chatroomManager().getChatRoom(conversationId);
            if(room == null) {
                return;
            }
            title =  TextUtils.isEmpty(room.getName()) ? conversationId : room.getName();
        }
        titleBar.getTitle().setText(title);
    }
}
