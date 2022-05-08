package io.agora.chatdemo.chatthread;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.activities.EaseChatThreadActivity;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.interfaces.OnChatLayoutFinishInflateListener;
import io.agora.chat.uikit.chat.interfaces.OnMessageSendCallBack;
import io.agora.chat.uikit.chatthread.EaseChatThreadFragment;
import io.agora.chat.uikit.menu.EasePopupWindow;
import io.agora.chat.uikit.chatthread.EaseChatThreadRole;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chatthread.adapter.ChatThreadCustomMessageAdapter;
import io.agora.chatdemo.databinding.LayoutThreadSettingMenuBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.chatthread.viewmodel.ChatThreadViewModel;
import io.agora.util.EMLog;

/**
 * The example that how to extends EaseThreadChatActivity, developer can extends {@link EaseChatThreadFragment}
 * and load it to your activity also.
 */
public class ChatThreadActivity extends EaseChatThreadActivity {
    private EaseTitleBar titleBar;
    private ChatThreadViewModel viewModel;

    public static void actionStart(Context context, String conversationId, String parentMsgId) {
        Intent intent = new Intent(context, ChatThreadActivity.class);
        intent.putExtra("parentMsgId", parentMsgId);
        intent.putExtra("conversationId", conversationId);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, String conversationId, String parentMsgId, String parentId) {
        Intent intent = new Intent(context, ChatThreadActivity.class);
        intent.putExtra("parentMsgId", parentMsgId);
        intent.putExtra("conversationId", conversationId);
        intent.putExtra("parentId", parentId);
        context.startActivity(intent);
    }

    @Override
    public void setChildFragmentBuilder(EaseChatFragment.Builder builder) {
        super.setChildFragmentBuilder(builder);
        builder.setOnChatLayoutFinishInflateListener(new OnChatLayoutFinishInflateListener() {

            @Override
            public void onTitleBarFinishInflate(EaseTitleBar titleBar) {
                ChatThreadActivity.this.titleBar = titleBar;
                setThreadTitle();
            }
        })
        .setCustomAdapter(new ChatThreadCustomMessageAdapter())
        .setCustomFragment(new ChatThreadFragment())
        .setOnMessageSendCallBack(new OnMessageSendCallBack() {
            @Override
            public void onSuccess(ChatMessage message) {
                ToastUtils.showToast(R.string.chat_thread_message_send_success);
            }

            @Override
            public void onError(int code, String errorMsg) {
                ToastUtils.showFailToast(errorMsg);
            }
        });
    }

    private void setThreadTitle() {
        if(titleBar != null) {
            titleBar.setRightImageResource(R.drawable.chat_settings_more);
            titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
                @Override
                public void onRightClick(View view) {
                    // hide soft keyboard first
                    hideSoftKeyboard();
                    showSettingMenu();
                }
            });
        }
    }

    @Override
    public void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ChatThreadViewModel.class);
        viewModel.getResultObservable().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> booleanResource) {
                parseResource(booleanResource, new OnResourceParseCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable Boolean data) {
                        finish();
                        LiveDataBus.get().with(DemoConstant.THREAD_CHANGE).postValue(EaseEvent.create(DemoConstant.THREAD_LEAVE, EaseEvent.TYPE.THREAD, conversationId));
                    }

                    @Override
                    public void onLoading(@Nullable Boolean data) {
                        super.onLoading(data);
                        showLoading();
                    }

                    @Override
                    public void onHideLoading() {
                        super.onHideLoading();
                        dismissLoading();
                    }
                });
            }
        });
        viewModel.getDisbandObservable().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> booleanResource) {
                parseResource(booleanResource, new OnResourceParseCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable Boolean data) {
                        removeLocalMessage();
                        finish();
                        LiveDataBus.get().with(DemoConstant.THREAD_CHANGE).postValue(EaseEvent.create(DemoConstant.THREAD_LEAVE, EaseEvent.TYPE.THREAD, conversationId));
                    }

                    @Override
                    public void onLoading(@Nullable Boolean data) {
                        super.onLoading(data);
                        showLoading();
                    }

                    @Override
                    public void onHideLoading() {
                        super.onHideLoading();
                        dismissLoading();
                    }
                });
            }
        });

    }

    @Override
    protected void joinChatThreadFailed(int errorCode, String message) {
        super.joinChatThreadFailed(errorCode, message);
        ToastUtils.showFailToast(message);
    }

    private void removeLocalMessage() {
        ChatMessage message = ChatClient.getInstance().chatManager().getMessage(parentMsgId);
        if(message != null) {
            Conversation conversation = ChatClient.getInstance().chatManager().getConversation(message.conversationId());
            conversation.removeMessage(conversationId);
        }
    }

    private void leaveThread() {
        new SimpleDialog.Builder(this)
                .setTitle(R.string.thread_leave_hint_title)
                .setOnConfirmClickListener(R.string.thread_leave_hint_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.leaveThread(conversationId);
                    }
                })
                .setConfirmColor(R.color.color_main_blue)
                .showCancelButton(true)
                .setCancelColor(R.color.color_main_blue)
                .show();
    }

    private void disbandThread() {
        new SimpleDialog.Builder(this)
                .setTitle(R.string.thread_disband_hint_title)
                .setOnConfirmClickListener(R.string.thread_disband_hint_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.disbandThread(conversationId, parentMsgId);
                    }
                })
                .setConfirmColor(R.color.color_main_blue)
                .showCancelButton(true)
                .setCancelColor(R.color.color_main_blue)
                .show();
    }

    /**
     * Parse Resource<T>
     * @param response
     * @param callback
     * @param <T>
     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(response == null) {
            return;
        }
        if(response.status == Status.SUCCESS) {
            callback.onHideLoading();
            callback.onSuccess(response.data);
        }else if(response.status == Status.ERROR) {
            callback.onHideLoading();
            if(!callback.hideErrorMsg) {
                ToastUtils.showToast(response.getMessage());
            }
            callback.onError(response.errorCode, response.getMessage());
        }else if(response.status == Status.LOADING) {
            callback.onLoading(response.data);
        }
    }

    private void showSettingMenu() {
        if(threadRole == EaseChatThreadRole.UNKNOWN) {
            EMLog.e("ThreadChatActivity", "Unknown thread role!");
            return;
        }
        EasePopupWindow pMenu = new EasePopupWindow(this, false);
        LayoutThreadSettingMenuBinding menuBinding = LayoutThreadSettingMenuBinding.inflate(getLayoutInflater());
        pMenu.setContentView(menuBinding.getRoot());
        // Set screen's alpha
        pMenu.setBackgroundAlpha(0.3f);
        // Set popup window's background alpha
        menuBinding.getRoot().setAlpha(0.8f);
        pMenu.showAtLocation(binding.getRoot(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        ViewGroup.LayoutParams layoutParams = menuBinding.getRoot().getLayoutParams();
        layoutParams.width = (int) EaseUtils.getScreenInfo(this)[0];
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        menuBinding.expandIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pMenu.dismiss();
                return true;
            }
        });

        if(threadRole == EaseChatThreadRole.GROUP_ADMIN) {
            menuBinding.itemThreadEdit.setVisibility(View.VISIBLE);
            menuBinding.itemThreadDisband.setVisibility(View.VISIBLE);
        }else if(threadRole == EaseChatThreadRole.CREATOR) {
            menuBinding.itemThreadEdit.setVisibility(View.VISIBLE);
            menuBinding.itemThreadDisband.setVisibility(View.GONE);
        }else {
            menuBinding.itemThreadEdit.setVisibility(View.GONE);
            menuBinding.itemThreadDisband.setVisibility(View.GONE);
        }

        menuBinding.itemThreadMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pMenu.dismiss();
                skipToThreadMembers();
            }
        });

        menuBinding.itemThreadNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pMenu.dismiss();
            }
        });

        menuBinding.itemThreadEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pMenu.dismiss();
                skipToEditLayout();
            }
        });

        menuBinding.itemThreadLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pMenu.dismiss();
                leaveThread();
            }
        });

        menuBinding.itemThreadDisband.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pMenu.dismiss();
                disbandThread();
            }
        });

    }

    private void skipToThreadMembers() {
        ChatThreadMembersActivity.actionStart(mContext, conversationId, threadRole.ordinal());
    }

    private void skipToEditLayout() {
        String threadName = titleBar.getTitle().getText().toString().trim();
        ChatThreadEditActivity.actionStart(mContext, conversationId, threadName);
    }

}
