package io.agora.chatdemo.conversation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.chat.ChatRoom;
import io.agora.chat.Conversation;
import io.agora.chat.Group;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.adapter.EaseConversationListAdapter;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chat.uikit.interfaces.OnEaseChatConnectionListener;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.widget.EaseSearchEditText;
import io.agora.chatdemo.global.BottomSheetContainerFragment;

public class ConversationListFragment extends EaseConversationListFragment {

    private TextView mNetworkDisconnectedTip;
    private List<EaseConversationInfo> mLastData;
    private EaseConversationListAdapter mAdapter;

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

        llRoot.setFocusableInTouchMode(true);
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_conversation_list_add, null);
        llRoot.addView(view, 1);
        EaseSearchEditText mEtSearch = view.findViewById(R.id.et_search);
        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString().trim());
            }
        });

        mNetworkDisconnectedTip = findViewById(R.id.network_disconnected_tip);
        mAdapter = conversationListLayout.getListAdapter();

    }


    private void search(final String content) {
        if (null == mLastData) {
            return;
        }
        if (TextUtils.isEmpty(content)) {
            mAdapter.setData(mLastData);
        } else {
            List<EaseConversationInfo> newData = new ArrayList<>(mLastData);
            Iterator<EaseConversationInfo> iterator = newData.iterator();
            Object conversationInfo;
            Conversation conversationItem;
            while (iterator.hasNext()) {
                conversationInfo = iterator.next().getInfo();
                if (conversationInfo instanceof Conversation) {
                    conversationItem = (Conversation) conversationInfo;
                    String username = conversationItem.conversationId();
                    if (conversationItem.getType() == Conversation.ConversationType.GroupChat) {
                        Group group = DemoHelper.getInstance().getGroupManager().getGroup(username);
                        if (group != null) {
                            if (!group.getGroupName().contains(content)) {
                                iterator.remove();
                            }
                        } else {
                            if (!username.contains(content)) {
                                iterator.remove();
                            }
                        }
                    } else if (conversationItem.getType() == Conversation.ConversationType.ChatRoom) {
                        ChatRoom chatRoom = DemoHelper.getInstance().getChatroomManager().getChatRoom(username);
                        if (chatRoom != null) {
                            if (!chatRoom.getName().contains(content)) {
                                iterator.remove();
                            }
                        } else {
                            if (!username.contains(content)) {
                                iterator.remove();
                            }
                        }
                    } else {
                        if (!username.contains(content)) {
                            iterator.remove();
                        }
                    }
                }
            }
            mAdapter.setData(newData);
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                new BottomSheetContainerFragment().show(getChildFragmentManager(), "ContainerFragment");
            }
        });
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                refreshList();
            }
        });

        EaseUIKit.getInstance().setOnEaseChatConnectionListener(new OnEaseChatConnectionListener() {
            @Override
            public void onConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mNetworkDisconnectedTip) {
                            mNetworkDisconnectedTip.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onDisconnect(int error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (null != mNetworkDisconnectedTip) {
                            mNetworkDisconnectedTip.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

            @Override
            public void onAccountLogout(int error) {

            }

            @Override
            public void onTokenExpired() {

            }

            @Override
            public void onTokenWillExpire() {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        EaseConversationInfo item = conversationListLayout.getListAdapter().getItem(position);
        if (item.getInfo() instanceof Conversation) {
            ChatActivity.actionStart(mContext, ((Conversation) item.getInfo()).conversationId(), EaseUtils.getChatType((Conversation) item.getInfo()));
        }

    }

    @Override
    public void loadDataFinish(List<EaseConversationInfo> data) {
        super.loadDataFinish(data);
        mLastData = new ArrayList<>(data);
    }
}
