package io.agora.chatdemo.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.LocationMessageBody;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chat.uikit.chat.widget.EaseChatMessageListLayout;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chat.uikit.menu.MenuItemBean;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.RecyclerViewUtils;
import io.agora.chatdemo.group.model.MemberAttributeBean;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;

public class CustomChatFragment extends EaseChatFragment {
    private boolean isFirstMeasure = true;
    private GroupDetailViewModel groupDetailViewModel;

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
        chatLayout.getMenuHelper().addItemMenu(menuItemBean);
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser()) ||
                message.getBody() instanceof LocationMessageBody || message.getBody() instanceof CustomMessageBody) {
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_report, false);
        } else {
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_report, true);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, ChatMessage message) {
        if (item.getItemId() == R.id.action_chat_report) {
            if (message.status() == ChatMessage.Status.SUCCESS)
                ChatReportActivity.actionStart(getActivity(), message.getMsgId());
        }
        return super.onMenuItemClick(item, message);

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
}
