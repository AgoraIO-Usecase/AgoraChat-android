package io.agora.chatdemo.chat;

import static io.agora.chat.callkit.general.EaseCallType.CONFERENCE_VIDEO_CALL;
import static io.agora.chat.callkit.general.EaseCallType.CONFERENCE_VOICE_CALL;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VOICE_CALL;
import static io.agora.chat.uikit.menu.EaseChatType.SINGLE_CHAT;
import static io.agora.chatdemo.general.constant.DemoConstant.GROUP_MEMBER_USER;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatRoom;
import io.agora.chat.Conversation;
import io.agora.chat.Presence;
import io.agora.chat.callkit.EaseCallKit;
import io.agora.chat.uikit.activities.EaseChatThreadListActivity;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.EaseChatLayout;
import io.agora.chat.uikit.chat.interfaces.OnChatExtendMenuItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnChatInputChangeListener;
import io.agora.chat.uikit.chat.interfaces.OnChatLayoutFinishInflateListener;
import io.agora.chat.uikit.chat.interfaces.OnChatRecordTouchListener;
import io.agora.chat.uikit.chat.interfaces.OnMessageItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnMessageSendCallBack;
import io.agora.chat.uikit.chat.interfaces.OnPeerTypingListener;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.StatusBarCompat;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.av.CallSingleBaseActivity;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.chat.adapter.CustomMessageAdapter;
import io.agora.chatdemo.chat.viewmodel.ChatViewModel;
import io.agora.chatdemo.chatthread.ChatThreadActivity;
import io.agora.chatdemo.contact.ContactDetailActivity;
import io.agora.chatdemo.contact.GroupMemberDetailBottomSheetFragment;
import io.agora.chatdemo.databinding.ActivityChatBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.permission.PermissionsManager;
import io.agora.chatdemo.general.widget.EasePresenceView;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.activities.GroupDetailActivity;
import io.agora.chatdemo.group.fragments.MultiplyVideoSelectMemberContainerFragment;
import io.agora.util.EMLog;

public class ChatActivity extends BaseInitActivity implements EasePresenceView.OnPresenceClickListener, View.OnClickListener{

    private String conversationId;
    private EaseChatType chatType;
    private ChatViewModel viewModel;
    private AlertDialog callSelectedDialog;
    private ActivityChatBinding binding;
    private EaseChatLayout mChatLayout;

    public static void actionStart(Context context, String conversationId, EaseChatType chatType) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType.getChatType());
        context.startActivity(intent);
    }

    @Override
    protected View getContentView() {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID);
        chatType = EaseChatType.from(intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, SINGLE_CHAT.getChatType()));
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        if (chatType == SINGLE_CHAT) {
            binding.presenceView.setVisibility(View.VISIBLE);
            binding.presenceView.setNameTextViewVisibility(View.VISIBLE);
            binding.presenceView.setPresenceTextViewArrowVisible(false);
            binding.presenceView.setPresenceTextViewColor(ContextCompat.getColor(this,R.color.color_light_gray_999999));
        } else {
            binding.presenceView.setVisibility(View.GONE);
        }

        binding.rightImage.setImageResource(R.drawable.chat_settings_more);
        binding.toolbar.setNavigationIcon(R.drawable.ease_titlebar_back);
        if(mContext.getSupportActionBar() == null) {
            binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        if(chatType == EaseChatType.GROUP_CHAT) {
            binding.llTitleRight.setVisibility(View.VISIBLE);
        }else {
            binding.llTitleRight.setVisibility(View.INVISIBLE);
        }
        initChatFragment();
    }

    private void initChatFragment() {
        EaseChatFragment fragment = new EaseChatFragment.Builder(conversationId, chatType)
                .useHeader(false)
                .setCustomAdapter(new CustomMessageAdapter())
                .setEmptyLayout(R.layout.ease_layout_no_data_show_nothing)
                .setOnChatExtendMenuItemClickListener(new OnChatExtendMenuItemClickListener() {
                    @Override
                    public boolean onChatExtendMenuItemClick(View view, int itemId) {
                        EMLog.e("TAG", "onChatExtendMenuItemClick");
                        if (itemId == R.id.extend_item_take_picture) {
                            // check if has permissions
                            if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.CAMERA)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.CAMERA}, null);
                                return true;
                            }
                            if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, null);
                                return true;
                            }
                            return false;
                        } else if (itemId == R.id.extend_item_picture || itemId == R.id.extend_item_file) {
                            if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, null);
                                return true;
                            }
                            return false;
                        } else if (itemId == R.id.extend_item_video) {
                            if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.CAMERA)) {
                                PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                                        , new String[]{Manifest.permission.CAMERA}, null);
                                return true;
                            }
                            if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                        EMLog.e("TAG", "onTextChanged: s: " + s.toString());
                    }
                })
                .setOnChatRecordTouchListener(new OnChatRecordTouchListener() {
                    @Override
                    public boolean onRecordTouch(View v, MotionEvent event) {
                        // Check if has record audio permission
                        if (!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.RECORD_AUDIO)) {
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
                        if (!TextUtils.equals(username, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
                            EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(username);
                            if (user == null) {
                                user = new EaseUser(username);
                            }
                            boolean isFriend = DemoHelper.getInstance().getModel().isContact(username);
                            if (isFriend) {
                                user.setContact(0);
                            } else {
                                user.setContact(3);
                            }
                            GroupMemberDetailBottomSheetFragment fragment = new GroupMemberDetailBottomSheetFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(GROUP_MEMBER_USER, user);
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), "ContainerFragment");
                        }
                    }

                    @Override
                    public void onUserAvatarLongClick(String username) {

                    }

                    @Override
                    public boolean onThreadClick(String messageId, String threadId) {
                        ChatThreadActivity.actionStart(mContext, threadId, messageId);
                        return true;
                    }
                })
                .setOnMessageSendCallBack(new OnMessageSendCallBack() {

                    @Override
                    public void onSuccess(ChatMessage message) {
                        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
                    }

                    @Override
                    public void onError(int code, String errorMsg) {
                        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
                        if (code == Error.MESSAGE_EXTERNAL_LOGIC_BLOCKED) {
                            errorMsg = getString(R.string.error_message_external_logic_blocked);
                        }
                        showToast(getString(R.string.chat_msg_error_toast, code, errorMsg));
                    }
                })
                .turnOnTypingMonitor(DemoHelper.getInstance().getModel().isShowMsgTyping())
                .setOnPeerTypingListener(new OnPeerTypingListener() {
                    @Override
                    public void onPeerTyping(String action) {
                        if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_BEGIN)) {
                            binding.subTitle.setText(getString(R.string.alert_during_typing));
                            binding.subTitle.setVisibility(View.VISIBLE);
                        } else if (TextUtils.equals(action, EaseChatLayout.ACTION_TYPING_END)) {
                            setDefaultTitle();
                        }
                    }
                })
                .setOnChatLayoutFinishInflateListener(new OnChatLayoutFinishInflateListener() {
                    @Override
                    public void onChatListFinishInflate(EaseChatLayout chatLayout) {
                        mChatLayout = chatLayout;
                    }
                })
                .hideSenderAvatar(true)
                .sendMessageByOriginalImage(true)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment, fragment, "chat").commit();
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.presenceView.setOnPresenceClickListener(this);
        binding.rightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Skip to chat settings fragment

                Bundle bundle = new Bundle();
                bundle.putString(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
                bundle.putInt(EaseConstant.EXTRA_CHAT_TYPE, chatType.getChatType());
                BottomSheetDialogFragment fragment = new ChatSettingsFragment();
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(), "chat_settings");
            }
        });
        binding.ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatType == SINGLE_CHAT) {
                    ContactDetailActivity.actionStart(mContext, conversationId, true);
                } else if (chatType == EaseChatType.GROUP_CHAT) {
                    GroupDetailActivity.actionStart(mContext, conversationId, true);
                }
            }
        });
        binding.ivThreadMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatType == EaseChatType.GROUP_CHAT) {
                    EaseChatThreadListActivity.actionStart(mContext, conversationId);
                }
            }
        });
        binding.ivCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCallSelectedDialog();
            }
        });
    }

    private void showCallSelectedDialog() {
        if (callSelectedDialog == null) {
            callSelectedDialog = new AlertDialog.Builder(mContext)
                    .setContentView(R.layout.dialog_call_selected)
                    .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(true)
                    .setOnClickListener(R.id.btn_cancel, this)
                    .setOnClickListener(R.id.ll_audio_call, this)
                    .setOnClickListener(R.id.ll_video_call, this)
                    .show();
        } else {
            callSelectedDialog.show();
        }
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getChatRoomObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<ChatRoom>() {
                @Override
                public void onSuccess(@Nullable ChatRoom data) {
                    setDefaultTitle();
                }
            });
        });
        viewModel.getPresenceObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Presence>>() {
                @Override
                public void onSuccess(List<Presence> presences) {
                    updatePresence();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> showSingleInfo());
                }
            });
        });
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (event.isGroupLeave() && TextUtils.equals(conversationId, event.message)) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (event.isChatRoomLeave() && TextUtils.equals(conversationId, event.message)) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (event.isMessageChange()) {
                showSnackBar(event.event);
            }
        });
        LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
            if (conversation == null) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
            if (conversation == null) {
                finish();
            }
        });
        LiveDataBus.get().with(DemoConstant.THREAD_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            mChatLayout.getChatMessageListLayout().refreshMessages();
        });
        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(this, event -> {
            updatePresence();
        });
        checkUnreadCount();
        setDefaultTitle();
        if (chatType == SINGLE_CHAT) {
            getPresenceData();
        }
    }

    private void updatePresence() {
        if(chatType == EaseChatType.SINGLE_CHAT) {
            Presence presence = DemoHelper.getInstance().getPresences().get(conversationId);
            if(presence != null) {
                DemoHelper.getInstance().getUsersManager().updateUserPresenceView(conversationId, binding.presenceView);
                binding.ivIcon.setVisibility(View.INVISIBLE);
            }else {
                showSingleInfo();
            }
        }
    }

    private void showSingleInfo() {
        DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, conversationId, binding.title, binding.ivIcon);
        binding.title.setVisibility(View.INVISIBLE);
        binding.presenceView.setVisibility(View.VISIBLE);
    }

    private void getPresenceData() {
        List<String> userIds = new ArrayList<>();
        userIds.add(conversationId);
        viewModel.fetchPresenceStatus(userIds);
    }

    /**
     * If conversation's unread count is not 0, then should notify to refresh
     */
    private void checkUnreadCount() {
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
        if (conversation != null && conversation.getUnreadMsgCount() > 0) {
            LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
        }
    }

    private void showSnackBar(String event) {
        Snackbar.make(binding.clTitle, event, Snackbar.LENGTH_SHORT).show();
    }

    private void setDefaultTitle() {
        if(chatType != SINGLE_CHAT) {
            boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, conversationId, binding.title, binding.ivIcon);
            if(!hasProvided) {
                setGroupInfo();
            }
        } else {
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, conversationId, binding.title, binding.ivIcon);
            binding.title.setVisibility(View.INVISIBLE);
            binding.subTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void setGroupInfo() {
        String title = "";
        if(chatType == EaseChatType.GROUP_CHAT) {
            title = GroupHelper.getGroupName(conversationId);
            binding.ivIcon.setImageResource(R.drawable.icon);
        }else if(chatType == EaseChatType.CHATROOM) {
            ChatRoom room = ChatClient.getInstance().chatroomManager().getChatRoom(conversationId);
            if(room == null) {
                viewModel.getChatRoom(conversationId);
                return;
            }
            title =  TextUtils.isEmpty(room.getName()) ? conversationId : room.getName();
            binding.ivIcon.setImageResource(R.drawable.icon);
        }
        binding.title.setText(title);
    }

    @Override
    public void onPresenceClick(View v) {
        ContactDetailActivity.actionStart(mContext, conversationId, true);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_audio_call:
                if(chatType==SINGLE_CHAT) {
                    EaseCallKit.getInstance().startSingleCall(SINGLE_VOICE_CALL,conversationId,null, CallSingleBaseActivity.class);
                }else{
                    // select members for voice call
                    MultiplyVideoSelectMemberContainerFragment fragment = new MultiplyVideoSelectMemberContainerFragment();
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("easeCallType",CONFERENCE_VOICE_CALL);
                    bundle.putString("groupId",conversationId);
                    fragment.setArguments(bundle);
                    fragment .show(getSupportFragmentManager(),"MultiplyVideoSelectMemberContainerFragment");
                }
               break;
            case R.id.ll_video_call:
                if(chatType==SINGLE_CHAT) {
                    EaseCallKit.getInstance().startSingleCall(SINGLE_VIDEO_CALL,conversationId,null, CallSingleBaseActivity.class);
                }else{
                    // select members for video call
                    MultiplyVideoSelectMemberContainerFragment fragment = new MultiplyVideoSelectMemberContainerFragment();
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("easeCallType",CONFERENCE_VIDEO_CALL);
                    bundle.putString("groupId",conversationId);
                    fragment.setArguments(bundle);
                    fragment .show(getSupportFragmentManager(),"MultiplyVideoSelectMemberContainerFragment");
                }
                break;
        }
        callSelectedDialog.dismiss();
    }
}
