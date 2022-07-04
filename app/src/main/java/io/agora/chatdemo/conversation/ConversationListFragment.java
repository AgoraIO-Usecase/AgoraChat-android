package io.agora.chatdemo.conversation;

import static io.agora.chat.uikit.utils.EaseImageUtils.setDrawableSize;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.chat.ChatRoom;
import io.agora.chat.Conversation;
import io.agora.chat.Group;
import io.agora.chat.Presence;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.adapter.EaseConversationListAdapter;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chat.uikit.interfaces.OnEaseChatConnectionListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.models.PresenceData;
import io.agora.chatdemo.general.utils.EasePresenceUtil;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.general.widget.EasePresenceView;
import io.agora.chatdemo.general.widget.EaseSearchEditText;
import io.agora.chatdemo.global.BottomSheetContainerFragment;
import io.agora.chatdemo.global.GlobalEventsMonitor;
import io.agora.chatdemo.me.CustomPresenceActivity;
import io.agora.util.EMLog;

public class ConversationListFragment extends EaseConversationListFragment implements EasePresenceView.OnPresenceClickListener, View.OnClickListener {

    private EaseUser userInfo;
    private EasePresenceView presenceView;
    private PresenceViewModel viewModel;
    private AlertDialog changePresenceDialog;
    private int currentPresence;
    private TextView customView;
    private String presenceString;

    private TextView mNetworkDisconnectedTip;
    private List<EaseConversationInfo> mLastData;
    private EaseConversationListAdapter mAdapter;
    private View titleBarLayout;

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBarLayout = LayoutInflater.from(mContext).inflate(R.layout.layout_conversation_list_title_bar, null);
        ViewGroup.LayoutParams titleBarParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.ease_common_title_bar_height));
        titleBarLayout.setLayoutParams(titleBarParams);
        llRoot.addView(titleBarLayout, 0);
        ImageView rightIcon = titleBarLayout.findViewById(R.id.right_image);
        rightIcon.setImageResource(R.drawable.main_add_top);
        // Set toolbar's icon
        EaseImageView icon = titleBarLayout.findViewById(R.id.iv_icon);
        icon.setImageResource(R.drawable.chat_toolbar_icon);
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

        presenceView = titleBarLayout.findViewById(R.id.presence_view);
        if(presenceView != null) {
            presenceView.setVisibility(View.VISIBLE);
            presenceView.setPresenceTextViewArrowVisible(true);
            presenceView.setNameTextViewVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) presenceView.getLayoutParams();
            params.setMargins(UIUtils.dp2px(mContext, 16), 0, 0, 0);
            presenceView.setLayoutParams(params);
        }

        userInfo = DemoHelper.getInstance().getUsersManager().getCurrentUserInfo();
        updatePresence();

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
    public void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(PresenceViewModel.class);
        viewModel.getPublishObservable().observe(this, response -> {
            if (response.status == Status.SUCCESS) {
                updatePresence();
            }
        });
    }

    private void updatePresence() {
        DemoHelper.getInstance().getUsersManager().updateUserPresenceView(userInfo.getUsername(), presenceView);
    }

    @Override
    public void initListener() {
        super.initListener();
        titleBarLayout.findViewById(R.id.right_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new BottomSheetContainerFragment().show(getChildFragmentManager(), "ContainerFragment");
            }
        });
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                refreshList();
                updatePresence();
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
        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(getViewLifecycleOwner(), event -> {
            updatePresence();
        });

        if(presenceView != null) {
            presenceView.setOnPresenceClickListener(this);
        }
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

    public void onPresenceClick(View v) {
        showChangePresenceDialog();
    }

    private void showChangePresenceDialog() {
        if (changePresenceDialog == null) {
            changePresenceDialog = new AlertDialog.Builder(mContext)
                    .setContentView(R.layout.dialog_conversation_fg_change_presence)
                    .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(true)
                    .setOnClickListener(R.id.btn_cancel, this)
                    .setOnClickListener(R.id.tv_online, this)
                    .setOnClickListener(R.id.tv_busy, this)
                    .setOnClickListener(R.id.tv_not_disturb, this)
                    .setOnClickListener(R.id.tv_leave, this)
                    .setOnClickListener(R.id.tv_custom, this)
                    .show();
            initDialogIcon();
        } else {
            changePresenceDialog.show();
        }
        initDialog();
    }

    private void initDialog() {
        customView = changePresenceDialog.getViewById(R.id.tv_custom);
        Presence presence = DemoHelper.getInstance().getPresences().get(DemoHelper.getInstance().getUsersManager().getCurrentUserID());
        presenceString = EasePresenceUtil.getPresenceString(mContext, presence);
        if (TextUtils.equals(presenceString, getString(PresenceData.ONLINE.getPresence()))) {
            currentPresence = R.id.tv_online;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.BUSY.getPresence()))) {
            currentPresence = R.id.tv_busy;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.DO_NOT_DISTURB.getPresence()))) {
            currentPresence = R.id.tv_not_disturb;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.LEAVE.getPresence()))) {
            currentPresence = R.id.tv_leave;
        } else {
            currentPresence = R.id.tv_custom;
            customView.setText(presenceString);
        }
    }

    private void initDialogIcon() {
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_online), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_busy), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_not_disturb), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_leave), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_custom), UIUtils.dp2px(mContext, 20));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                changePresenceDialog.dismiss();
                break;
            case R.id.tv_online:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId(), getString(PresenceData.ONLINE.getPresence()));
                } else {
                    viewModel.publishPresence("");
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_online;
                break;
            case R.id.tv_busy:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId(), getString(PresenceData.BUSY.getPresence()));
                } else {
                    viewModel.publishPresence(getString(PresenceData.BUSY.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_busy;
                break;
            case R.id.tv_not_disturb:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId(), getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                } else {
                    viewModel.publishPresence(getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_not_disturb;
                break;
            case R.id.tv_leave:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId(), getString(PresenceData.LEAVE.getPresence()));
                } else {
                    viewModel.publishPresence(getString(PresenceData.LEAVE.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_leave;
                break;
            case R.id.tv_custom:
                Intent intent = new Intent(mContext, CustomPresenceActivity.class);
                startActivityForResult(intent, DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_CONVERSATIONFRAGMENT);
                changePresenceDialog.dismiss();
                break;
        }


    }

    private void showConfirmDialog(int id, String target) {
        new SimpleDialog.Builder((BaseActivity) mContext)
                .setTitle(R.string.dialog_clear_presence_title)
                .setContent(getString(R.string.dialog_clear_presence_content, customView.getText().toString().trim(), target))
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        switch (id) {
                            case R.id.tv_online:
                                viewModel.publishPresence("");
                                break;
                            case R.id.tv_busy:
                                viewModel.publishPresence(getString(PresenceData.BUSY.getPresence()));
                                break;
                            case R.id.tv_not_disturb:
                                viewModel.publishPresence(getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                                break;
                            case R.id.tv_leave:
                                viewModel.publishPresence(getString(PresenceData.LEAVE.getPresence()));
                                break;
                        }
                        customView.setText(R.string.ease_presence_custom);
                        changePresenceDialog.dismiss();
                    }
                })
                .setOnCancelClickListener(new SimpleDialog.onCancelClickListener() {
                    @Override
                    public void onCancelClick(View view) {
                        currentPresence = R.id.tv_custom;
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_CONVERSATIONFRAGMENT && resultCode == DemoConstant.PRESENCE_RESULTCODE) {
            String customPresence = data.getStringExtra(DemoConstant.PRESENCE_CUSTOM);
            if (!TextUtils.isEmpty(customPresence)) {
                viewModel.publishPresence(customPresence);
            }
        }

    }
}
