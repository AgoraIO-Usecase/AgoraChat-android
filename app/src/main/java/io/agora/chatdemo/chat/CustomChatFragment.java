package io.agora.chatdemo.chat;

import static io.agora.chat.uikit.menu.EaseChatType.SINGLE_CHAT;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.LocationMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chat.uikit.chat.widget.EaseChatMessageListLayout;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chat.uikit.menu.MenuItemBean;
import io.agora.chatdemo.DemoHelper;
import io.agora.chat.uikit.models.EaseReactionEmojiconEntity;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.viewmodel.ChatViewModel;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.RecyclerViewUtils;
import io.agora.chatdemo.group.model.MemberAttributeBean;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;
import io.agora.chat.uikit.chat.interfaces.IChatTopExtendMenu;
import io.agora.chat.uikit.chat.widget.EaseChatMultiSelectView;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.util.EMLog;

public class CustomChatFragment extends EaseChatFragment {
    private boolean isFirstMeasure = true;
    private GroupDetailViewModel groupDetailViewModel;
    private ChatViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {
        super.initData();
        groupDetailViewModel = new ViewModelProvider((AppCompatActivity)mContext).get(GroupDetailViewModel.class);
        groupDetailViewModel.getFetchMemberAttributesObservable().observe(this,response ->{
            if(response == null) {
                return;
            }
            if(response.status == Status.SUCCESS) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getTranslationObservable().observe(this,response ->{
            if(response == null) {
                return;
            }
            if(response.status == Status.SUCCESS) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }else {
                EMLog.e("translationMessage","onError: " + response.errorCode + " - " + response.getMessage());
            }
        });

        LiveDataBus.get().with(DemoConstant.GROUP_MEMBER_ATTRIBUTE_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            chatLayout.getChatMessageListLayout().refreshMessages();
        });
        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isMessageChange()) {
                chatLayout.getChatMessageListLayout().refreshMessages();
            }
        });
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
    public void initListener() {
        super.initListener();
        listenerRecyclerViewItemFinishLayout();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void initView() {
        super.initView();
        MenuItemBean menuItemBean = new MenuItemBean(0, R.id.action_chat_report, 99, getResources().getString(R.string.ease_action_report));
        menuItemBean.setResourceId(R.drawable.chat_item_menu_report);
        MenuItemBean menuTranslationBean = new MenuItemBean(0, R.id.action_chat_translation,88, getResources().getString(R.string.ease_action_translation));
        menuTranslationBean.setResourceId(R.drawable.chat_item_menu_translation);
        MenuItemBean menuReTranslationBean = new MenuItemBean(0, R.id.action_chat_re_translation,111, getResources().getString(R.string.ease_action_re_translation));
        menuReTranslationBean.setResourceId(R.drawable.chat_item_menu_translation);
        chatLayout.getMenuHelper().addItemMenu(menuItemBean);
        chatLayout.getMenuHelper().addItemMenu(menuTranslationBean);
        chatLayout.getMenuHelper().addItemMenu(menuReTranslationBean);
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);

        if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser())
                || message.getBody() instanceof LocationMessageBody
                || message.getBody() instanceof CustomMessageBody
                || message.status() != ChatMessage.Status.SUCCESS) {
            helper.findItemVisible(R.id.action_chat_report, false);
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_re_translation, false);
            if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser()) ||
                    message.getBody() instanceof LocationMessageBody || message.getBody() instanceof CustomMessageBody) {
                chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_report, false);
            } else {
                helper.findItemVisible(R.id.action_chat_report, true);
            }
            boolean isRecallNote = message.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false);
            if (isRecallNote) {
                helper.setAllItemsVisible(false);
                helper.showHeaderView(false);
                helper.findItemVisible(R.id.action_chat_delete, true);
            }
        }

        if (message.getBody() instanceof TextMessageBody) {
            if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser())){
                chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_translation, false);
                chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_re_translation, false);
            }else {
                if (((TextMessageBody) message.getBody()).getTranslations().size() > 0) {
                    chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_translation, false);
                    chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_re_translation, true);
                } else {
                    chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_translation, true);
                    chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_re_translation, false);
                }
            }
        } else {
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_translation, false);
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_re_translation, false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, ChatMessage message) {
        switch (item.getItemId()){
            case R.id.action_chat_report:
                if (message.status() == ChatMessage.Status.SUCCESS)
                    ChatReportActivity.actionStart(getActivity(),message.getMsgId());
                break;
            case R.id.action_chat_select:
                showSelectModelTitle();
                LiveDataBus.get().with(DemoConstant.EVENT_CHAT_MODEL_TO_SELECT).postValue(EaseEvent.create(DemoConstant.EVENT_CHAT_MODEL_TO_SELECT, EaseEvent.TYPE.NOTIFY));
                break;
            case R.id.action_chat_translation:
            case R.id.action_chat_re_translation:
                translationMessage(message);
                break;
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

    private void listenerRecyclerViewItemFinishLayout() {
        if (chatLayout == null || chatType.getChatType() != EaseConstant.CHATTYPE_GROUP) {
            return;
        }
        EaseChatMessageListLayout chatMessageListLayout = chatLayout.getChatMessageListLayout();
        if (chatMessageListLayout == null || chatMessageListLayout.getChildCount() <= 0) {
            return;
        }
        View swipeView = chatMessageListLayout.getChildAt(0);
        if (!(swipeView instanceof SwipeRefreshLayout)) {
            return;
        }
        if (((SwipeRefreshLayout) swipeView).getChildCount() <= 0) {
            return;
        }
        RecyclerView recyclerView = null;
        for (int i = 0; i < ((SwipeRefreshLayout) swipeView).getChildCount(); i++) {
            View child = ((SwipeRefreshLayout) swipeView).getChildAt(i);
            if (child instanceof RecyclerView) {
                recyclerView = (RecyclerView) child;
                break;
            }
        }
        if (recyclerView == null || chatMessageListLayout.getMessageAdapter() == null) {
            return;
        }
        EaseMessageAdapter messageAdapter = chatMessageListLayout.getMessageAdapter();
        RecyclerView finalRecyclerView = recyclerView;
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (isFirstMeasure && finalRecyclerView.getLayoutManager() != null && messageAdapter.getData() != null
                    && ((LinearLayoutManager) finalRecyclerView.getLayoutManager()).findLastVisibleItemPosition() == messageAdapter.getData().size() - 1) {
                isFirstMeasure = false;
                int[] positionArray = RecyclerViewUtils.rangeMeasurement(finalRecyclerView);
                getGroupUserInfo(positionArray[0], positionArray[1]);
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int[] positionArray = RecyclerViewUtils.rangeMeasurement(recyclerView);
                    getGroupUserInfo(positionArray[0], positionArray[1]);
                }
            }
        });
    }

    public void getGroupUserInfo(int start, int end) {
        if (start <= end && end >= 0 && chatType.getChatType() == DemoConstant.CHATTYPE_GROUP) {
            Set<String> nameSet = new HashSet<>();
            for (int i = start; i <= end; i++) {
                ChatMessage message = chatLayout.getChatMessageListLayout().getMessageAdapter().getItem(i);
                if (message != null && !TextUtils.isEmpty(message.getFrom())) {
                    nameSet.add(message.getFrom());
                }
            }
            Iterator<String> iterator = nameSet.iterator();
            while (iterator.hasNext()) {
                String userId = iterator.next();
                MemberAttributeBean bean = DemoHelper.getInstance().getMemberAttribute(conversationId, userId);
                if (bean == null) {
                    //当从本地获取bean对象为空时 默认创建bean对象 并赋值nickName为userId
                    MemberAttributeBean emptyBean = new MemberAttributeBean();
                    emptyBean.setNickName(userId);
                    DemoHelper.getInstance().saveMemberAttribute(conversationId, userId, emptyBean);
                } else {
                    iterator.remove();
                }
            }
            if (nameSet.isEmpty()) {
                return;
            }
            List<String> userIds = new ArrayList<>(nameSet);
            groupDetailViewModel.fetchGroupMemberAttribute(conversationId, userIds);
        }
    }

    @Override
    public void onModifyMessageSuccess(ChatMessage messageModified) {
        super.onModifyMessageSuccess(messageModified);
        //refresh conversation
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
    }


    private void translationMessage(ChatMessage message){
        String targetLanguage = DemoHelper.getInstance().getModel().getTargetLanguage();
        List<String> list = new ArrayList<>();
        list.add(targetLanguage);
        viewModel.translationMessage(message,list);
    }

    @Override
    public void addMsgAttrsBeforeSend(ChatMessage message) {
        super.addMsgAttrsBeforeSend(message);
        String enableAutoTranslation = DemoHelper.getInstance().getModel().getEnableAutoTranslation();
        if (!TextUtils.isEmpty(enableAutoTranslation)){
            try {
                JSONObject jsonObject = new JSONObject(enableAutoTranslation);
                if ((Boolean) jsonObject.get(conversationId)){
                    translationMessage(message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
