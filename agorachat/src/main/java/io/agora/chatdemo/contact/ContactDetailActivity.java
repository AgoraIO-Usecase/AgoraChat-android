package io.agora.chatdemo.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.contact.viewmodels.ContactDetailViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.widget.ArrowItemView;

public class ContactDetailActivity extends BaseInitActivity implements View.OnClickListener {
    private String username;
    private EaseTitleBar toolbar_contact_detail;
    private EaseImageView iv_avatar;
    private TextView tv_nickname;
    private TextView tv_id;
    private EaseImageView iv_chat;
    private ArrowItemView item_block_contact;
    private ArrowItemView item_delete_block;
    private ContactDetailViewModel viewModel;

    public static void actionStart(Context context, String username) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra("username", username);
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
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        toolbar_contact_detail = findViewById(R.id.toolbar_contact_detail);
        iv_avatar = findViewById(R.id.iv_avatar);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_id = findViewById(R.id.tv_id);
        iv_chat = findViewById(R.id.iv_chat);
        item_block_contact = findViewById(R.id.item_block_contact);
        item_delete_block = findViewById(R.id.item_delete_block);
        EaseUserUtils.setUserAvatarStyle(iv_avatar);
    }

    @Override
    protected void initListener() {
        super.initListener();
        iv_chat.setOnClickListener(this);
        item_block_contact.setOnClickListener(this);
        item_delete_block.setOnClickListener(this);
        toolbar_contact_detail.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(mContext).get(ContactDetailViewModel.class);
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
        viewModel.getUserInfoById(username, true);
    }

    private void sendEvent() {
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE)
                .postValue(EaseEvent.create(DemoConstant.CONTACT_UPDATE, EaseEvent.TYPE.CONTACT));
    }

    private void updateLayout(EaseUser user) {
        tv_nickname.setText(user.getNickname());
        tv_id.setText(getString(R.string.contact_detail_show_id, user.getUsername()));
        Glide.with(mContext)
                .load(user.getAvatar())
                .placeholder(R.drawable.ease_default_avatar)
                .error(R.drawable.ease_default_avatar)
                .into(iv_avatar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_chat :
                showToast("聊天");
                break;
            case R.id.item_block_contact :
                //viewModel.addUserToBlackList(username, true);
                break;
            case R.id.item_delete_block :
                //viewModel.deleteContact(username);
                break;
        }
    }
}
