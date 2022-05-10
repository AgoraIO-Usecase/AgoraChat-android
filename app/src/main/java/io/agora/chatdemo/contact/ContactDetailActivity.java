package io.agora.chatdemo.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.Presence;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.utils.EasePresenceUtil;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.contact.viewmodels.ContactDetailViewModel;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.widget.ArrowItemView;

public class ContactDetailActivity extends BaseInitActivity implements View.OnClickListener {
    private String username;
    private EaseTitleBar toolbarContactDetail;
    private EaseImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvId;
    private EaseImageView ivChat;
    private ArrowItemView itemBlockContact;
    private ArrowItemView itemDeleteBlock;
    private ContactDetailViewModel viewModel;
    private boolean fromChat;
    private EaseImageView ivPresence;
    private PresenceViewModel presenceViewModel;

    public static void actionStart(Context context, String username) {
        actionStart(context, username, false);
    }

    public static void actionStart(Context context, String username, boolean isFromChat) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("from_chat", isFromChat);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_contact_detail;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        username = getIntent().getStringExtra("username");
        fromChat = getIntent().getBooleanExtra("from_chat", false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        toolbarContactDetail = findViewById(R.id.toolbar_contact_detail);
        ivAvatar = findViewById(R.id.iv_avatar);
        tvNickname = findViewById(R.id.tv_nickname);
        tvId = findViewById(R.id.tv_id);
        ivChat = findViewById(R.id.iv_chat);
        ivPresence = findViewById(R.id.iv_user_presence);
        TextView tvChat = findViewById(R.id.tv_chat);
        itemBlockContact = findViewById(R.id.item_block_contact);
        itemDeleteBlock = findViewById(R.id.item_delete_block);
        if(fromChat) {
            ivChat.setVisibility(View.GONE);
            tvChat.setVisibility(View.GONE);
        }
        ivPresence.setVisibility(View.VISIBLE);
        EaseUserUtils.setUserAvatarStyle(ivAvatar);
    }

    @Override
    protected void initListener() {
        super.initListener();
        ivChat.setOnClickListener(this);
        itemBlockContact.setOnClickListener(this);
        itemDeleteBlock.setOnClickListener(this);
        toolbarContactDetail.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                finish();
            }
        });

        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(mContext, event -> {
            updatePresence();
        });
    }

    private void updatePresence() {
        Presence presence = DemoHelper.getInstance().getPresences().get(username);
        ivPresence.setImageResource(EasePresenceUtil.getPresenceIcon(mContext,presence));
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(mContext).get(ContactDetailViewModel.class);
        presenceViewModel=new ViewModelProvider(mContext).get(PresenceViewModel.class);
        viewModel.blackObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE).postValue(EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT));
                    finish();
                }
            });
        });
        viewModel.deleteObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE).postValue(EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT));
                    finish();
                }
            });
        });
        viewModel.userInfoObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EaseUser>() {
                @Override
                public void onSuccess(EaseUser data) {
                    updateLayout(data);
                    sendEvent();
                }
            });
        });
        presenceViewModel.presencesObservable().observe(this,response->{
            parseResource(response, new OnResourceParseCallback<List<Presence>>() {
                @Override
                public void onSuccess(@Nullable List<Presence> data) {
                    updatePresence();
                }
            });
        });
        viewModel.getUserInfoById(username, true);

        presenceViewModel.subscribePresences(username, 7 * 24 * 60 * 60);
    }

    private void sendEvent() {
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE)
                .postValue(EaseEvent.create(DemoConstant.CONTACT_UPDATE, EaseEvent.TYPE.CONTACT));
    }

    private void updateLayout(EaseUser user) {
        DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, username, tvNickname, ivAvatar);
        tvId.setText(getString(R.string.show_agora_chat_id, user.getUsername()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_chat :
                skipToChat();
                break;
            case R.id.item_block_contact :
                showBlockDialog();
                break;
            case R.id.item_delete_block :
                showDeleteDialog();
                break;
        }
    }

    private void showBlockDialog() {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.contact_detail_block_title)
                .setContent(R.string.contact_detail_block_content)
                .setOnConfirmClickListener(R.string.contact_detail_block_confirm_text, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.addUserToBlackList(username, true);
                    }
                })
                .setConfirmColor(R.color.contact_color_block)
                .showCancelButton(true)
                .show();
    }

    private void showDeleteDialog() {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.contact_detail_delete_title)
                .setContent(R.string.contact_detail_delete_content)
                .setOnConfirmClickListener(R.string.contact_detail_delete_confirm_text, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.deleteContact(username);
                    }
                })
                .setConfirmColor(R.color.contact_color_block)
                .showCancelButton(true)
                .show();
    }

    private void skipToChat() {
        ChatActivity.actionStart(mContext, username, DemoConstant.CHATTYPE_SINGLE);
    }
}
