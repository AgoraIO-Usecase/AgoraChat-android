package io.agora.chatdemo.thread;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.agora.chat.uikit.activities.EaseThreadChatActivity;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.interfaces.OnChatLayoutFinishInflateListener;
import io.agora.chat.uikit.menu.EasePopupWindow;
import io.agora.chat.uikit.thread.EaseThreadRole;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.LayoutThreadSettingMenuBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.thread.viewmodel.ThreadChatViewModel;
import io.agora.util.EMLog;

public class ThreadChatActivity extends EaseThreadChatActivity {
    private EaseTitleBar titleBar;
    private ThreadChatViewModel viewModel;

    @Override
    public void initView() {
        super.initView();
        if(titleBar != null) {
            titleBar.setRightImageResource(R.drawable.chat_settings_more);
        }
    }

    @Override
    public void setChildFragmentBuilder(EaseChatFragment.Builder builder) {
        super.setChildFragmentBuilder(builder);
        builder.setOnChatLayoutFinishInflateListener(new OnChatLayoutFinishInflateListener() {

            @Override
            public void onTitleBarFinishInflate(EaseTitleBar titleBar) {
                ThreadChatActivity.this.titleBar = titleBar;
            }
        });
    }

    @Override
    public void initListener() {
        super.initListener();
        if(titleBar != null) {
            titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
                @Override
                public void onRightClick(View view) {
                    showSettingMenu();
                }
            });
        }
    }

    @Override
    public void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ThreadChatViewModel.class);
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
                        viewModel.disbandThread(conversationId);
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
        if(threadRole == EaseThreadRole.UNKNOWN) {
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

        if(threadRole == EaseThreadRole.GROUP_ADMIN) {
            menuBinding.itemThreadEdit.setVisibility(View.VISIBLE);
            menuBinding.itemThreadDisband.setVisibility(View.VISIBLE);
        }else if(threadRole == EaseThreadRole.CREATOR) {
            menuBinding.itemThreadEdit.setVisibility(View.VISIBLE);
            menuBinding.itemThreadDisband.setVisibility(View.GONE);
        }else {
            menuBinding.itemThreadEdit.setVisibility(View.GONE);
            menuBinding.itemThreadDisband.setVisibility(View.GONE);
        }

        menuBinding.itemThreadMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToThreadMembers();
            }
        });

        menuBinding.itemThreadNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        menuBinding.itemThreadEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipToEditLayout();
            }
        });

        menuBinding.itemThreadLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveThread();
            }
        });

        menuBinding.itemThreadDisband.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disbandThread();
            }
        });

    }

    private void skipToThreadMembers() {
        ThreadMembersActivity.actionStart(mContext, conversationId);
    }

    private void skipToEditLayout() {
        String content = titleBar.getSubTitle().getText().toString().trim();
        String threadName = content.replace("#", "").trim();
        ThreadEditActivity.actionStart(mContext, conversationId, threadName);
    }

}
