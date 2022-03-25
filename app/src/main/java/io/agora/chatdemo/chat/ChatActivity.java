package io.agora.chatdemo.chat;

import static io.agora.chatdemo.general.constant.DemoConstant.GROUP_MEMBER_USER;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.EaseChatLayout;
import io.agora.chat.uikit.chat.interfaces.OnChatExtendMenuItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnChatInputChangeListener;
import io.agora.chat.uikit.chat.interfaces.OnMessageItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnChatRecordTouchListener;
import io.agora.chat.uikit.chat.interfaces.OnMessageSendCallBack;
import io.agora.chat.uikit.chat.interfaces.OnPeerTypingListener;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.chat.viewmodel.ChatViewModel;
import io.agora.chatdemo.contact.ContactDetailActivity;
import io.agora.chatdemo.contact.GroupMemberDetailBottomSheetFragment;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.permission.PermissionsManager;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.activities.GroupDetailActivity;
import io.agora.util.EMLog;

public class ChatActivity extends BaseInitActivity {
    private String conversationId;
    private int chatType;
    private EaseTitleBar titleBar;
    private ChatViewModel viewModel;

    public static void actionStart(Context context, String conversationId, int chatType) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID);
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        titleBar.setRightImageResource(R.drawable.chat_settings_more);
        titleBar.getIcon().setVisibility(View.GONE);
        titleBar.setTitlePosition(EaseTitleBar.TitlePosition.Left);
        initChatFragment();
    }

    private void initChatFragment() {
        EaseChatFragment fragment = new EaseChatFragment.Builder(conversationId, chatType)
                .useHeader(false)
                .setEmptyLayout(R.layout.ease_layout_no_data_show_nothing)
                .setOnChatExtendMenuItemClickListener(new OnChatExtendMenuItemClickListener() {
                    @Override
                    public boolean onChatExtendMenuItemClick(View view, int itemId) {
                        EMLog.e("TAG", "onChatExtendMenuItemClick");
                        if(itemId == R.id.extend_item_take_picture) {
                            // check if has permissions
                            if(!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.CAMERA)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.CAMERA}, null);
                                return true;
                            }
                            if(!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, null);
                                return true;
                            }
                            return false;
                        }else if(itemId == R.id.extend_item_picture || itemId == R.id.extend_item_file || itemId == R.id.extend_item_video) {
                            if(!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, null);
                                return true;
                            }
                            return false;
                        }
                        return false;
                    }
                })
                .setOnChatInputChangeListener(new OnChatInputChangeListener() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        EMLog.e("TAG", "onTextChanged: s: "+s.toString());
                    }
                })
                .setOnChatRecordTouchListener(new OnChatRecordTouchListener() {
                    @Override
                    public boolean onRecordTouch(View v, MotionEvent event) {
                        // Check if has record audio permission
                        if(!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.RECORD_AUDIO)) {
                            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                    , new String[]{Manifest.permission.RECORD_AUDIO}, null);
                            return true;
                        }
                        return false;
                    }
                })
                .setOnMessageItemClickListener(new OnMessageItemClickListener() {
                    @Override
                    public boolean onBubbleClick(ChatMessage message) {
                        return false;
                    }

                    @Override
                    public boolean onBubbleLongClick(View v, ChatMessage message) {
                        return false;
                    }

                    @Override
                    public void onUserAvatarClick(String username) {
                        if(!TextUtils.equals(username, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
                            EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(username);
                            if(user == null){
                                user = new EaseUser(username);
                            }
                            boolean isFriend =  DemoHelper.getInstance().getModel().isContact(username);
                            if(isFriend){
                                user.setContact(0);
                            }else{
                                user.setContact(3);
                            }
                            GroupMemberDetailBottomSheetFragment fragment = new GroupMemberDetailBottomSheetFragment();
                            Bundle bundle =new Bundle();
                            bundle.putSerializable(GROUP_MEMBER_USER,user);
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(),"ContainerFragment");
                        }
                    }

                    @Override
                    public void onUserAvatarLongClick(String username) {

                    }
                })
                .setOnMessageSendCallBack(new OnMessageSendCallBack() {

                    @Override
                    public void onSuccess(ChatMessage message) {
                        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE,EaseEvent.TYPE.MESSAGE));
                    }

                    @Override
                    public void onError(int code, String errorMsg) {
                        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE,EaseEvent.TYPE.MESSAGE));
                        if(code== Error.MESSAGE_EXTERNAL_LOGIC_BLOCKED) {
                            errorMsg=getString(R.string.error_message_external_logic_blocked);
                        }
                        showToast(getString(R.string.chat_msg_error_toast, code, errorMsg));
                    }
                })
                .turnOnTypingMonitor(DemoHelper.getInstance().getModel().isShowMsgTyping())
                .setOnPeerTypingListener(new OnPeerTypingListener() {
                    @Override
                    public void onPeerTyping(String action) {
                        if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_BEGIN)) {
                            titleBar.setTitle(getString(R.string.alert_during_typing));
                        } else if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_END)) {
                            setDefaultTitle();
                        }
                    }
                })
                .hideSenderAvatar(true)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment, fragment, "chat").commit();
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                // Skip to chat settings fragment
                Bundle bundle = new Bundle();
                bundle.putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
                bundle.putInt(EaseConstant.EXTRA_CHAT_TYPE, chatType);
                BottomSheetDialogFragment fragment = new ChatSettingsFragment();
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "chat_settings");
            }
        });
        titleBar.setOnIconClickListener(new EaseTitleBar.OnIconClickListener() {
            @Override
            public void onIconClick(View view) {
                if(chatType == DemoConstant.CHATTYPE_SINGLE) {
                    ContactDetailActivity.actionStart(mContext, conversationId, true);
                }else if(chatType == DemoConstant.CHATTYPE_GROUP){
                    GroupDetailActivity.actionStart(mContext, conversationId, true);
                }
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getChatRoomObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<ChatRoom>() {
                @Override
                public void onSuccess(@Nullable ChatRoom data) {
                    setDefaultTitle();
                }
            });
        });
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isGroupLeave() && TextUtils.equals(conversationId, event.message)) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isChatRoomLeave() && TextUtils.equals(conversationId,  event.message)) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isMessageChange()) {
                showSnackBar(event.event);
            }
        });
        LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
            if(conversation == null) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
            if(conversation == null) {
                finish();
            }
        });
        checkUnreadCount();
        setDefaultTitle();
    }

    /**
     * If conversation's unread count is not 0, then should notify to refresh
     */
    private void checkUnreadCount() {
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
        if(conversation != null && conversation.getUnreadMsgCount() > 0) {
            LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
        }
    }

    private void showSnackBar(String event) {
        Snackbar.make(titleBar, event, Snackbar.LENGTH_SHORT).show();
    }

    private void setDefaultTitle() {
        if(chatType != DemoConstant.CHATTYPE_SINGLE) {
            boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, conversationId, titleBar.getTitle(), titleBar.getIcon());
            if(!hasProvided) {
                setGroupInfo();
            }
        }else {
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, conversationId, titleBar.getTitle(), titleBar.getIcon());
        }
    }

    private void setGroupInfo() {
        String title = "";
        if(chatType == DemoConstant.CHATTYPE_GROUP) {
            title = GroupHelper.getGroupName(conversationId);
            titleBar.setIcon(R.drawable.icon);
        }else if(chatType == DemoConstant.CHATTYPE_CHATROOM) {
            ChatRoom room = ChatClient.getInstance().chatroomManager().getChatRoom(conversationId);
            if(room == null) {
                viewModel.getChatRoom(conversationId);
                return;
            }
            title =  TextUtils.isEmpty(room.getName()) ? conversationId : room.getName();
            titleBar.setIcon(R.drawable.icon);
        }
        titleBar.setTitle(title);
    }
}
