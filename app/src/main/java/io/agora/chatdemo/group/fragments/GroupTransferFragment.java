package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.UUID;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;

public class GroupTransferFragment extends GroupAllMembersFragment {
    private boolean isLeaveAfterTransfer;
    private String newOwner;
    private String newOwner_nickname;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            isLeaveAfterTransfer = bundle.getBoolean("is_leave_after_transfer", false);
        }
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        memberAuthorityViewModel.getTransferOwnerObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ChatMessage msg = ChatMessage.createSendMessage(ChatMessage.Type.TXT);
                    msg.setChatType(ChatMessage.ChatType.Chat);
                    msg.setTo(groupId);
                    msg.setMsgId(UUID.randomUUID().toString());
                    msg.setAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, true);
                    msg.setAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE, DemoConstant.SYSTEM_CHANGE_OWNER);
                    msg.setAttribute(DemoConstant.ID_OR_NICKNAME,TextUtils.isEmpty(newOwner_nickname)? newOwner : newOwner_nickname);
                    msg.addBody(new TextMessageBody( getString(R.string.group_change_owner,TextUtils.isEmpty(newOwner_nickname)? newOwner : newOwner_nickname)));
                    msg.setStatus(ChatMessage.Status.SUCCESS);
                    // save invitation as messages
                    ChatClient.getInstance().chatManager().saveMessage(msg);
                    mContext.finish();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_OWNER_TRANSFER, EaseEvent.TYPE.GROUP));
                }
            });
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        managersAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EaseUser item = managersAdapter.getItem(position);
                if(TextUtils.equals(item.getUsername(), DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
                    return;
                }
                if(!GroupHelper.isOwner(group)) {
                    return;
                }
                showTransferDialog(item);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        EaseUser item = listAdapter.getItem(position);
        if(TextUtils.equals(item.getUsername(), DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
            return;
        }
        if(!GroupHelper.isOwner(group)) {
            return;
        }
        showTransferDialog(item);
    }

    private void showTransferDialog(EaseUser item) {
        new SimpleDialog.Builder(mContext)
                .setTitle(getString(isLeaveAfterTransfer ? R.string.group_members_action_transfer_and_leave_title
                        : R.string.group_members_action_transfer_title, item.getNickname()))
                .setOnConfirmClickListener(isLeaveAfterTransfer ? R.string.group_members_action_transfer_and_leave_confirm
                        : R.string.group_members_action_transfer_confirm
                        , new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        newOwner = item.getUsername();
                        newOwner_nickname = item.getNickname();
                        memberAuthorityViewModel.changeOwner(groupId, item.getUsername());
                    }
                })
                .setConfirmColor(R.color.color_alert)
                .showCancelButton(true)
                .show();
    }

}
