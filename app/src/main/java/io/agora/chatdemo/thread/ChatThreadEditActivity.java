package io.agora.chatdemo.thread;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.uikit.utils.EaseEditTextUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityThreadEditBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.thread.viewmodel.ChatThreadEditViewModel;

public class ChatThreadEditActivity extends BaseInitActivity {
    private ActivityThreadEditBinding binding;
    private String threadId;
    private String threadName;
    private Drawable clearDrawable;
    private ChatThreadEditViewModel viewModel;

    public static void actionStart(Context context, String threadId, String threadName) {
        Intent intent = new Intent(context, ChatThreadEditActivity.class);
        intent.putExtra("threadId", threadId);
        intent.putExtra("threadName", threadName);
        context.startActivity(intent);
    }

    @Override
    protected View getContentView() {
        binding = ActivityThreadEditBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        threadId = intent.getStringExtra("threadId");
        threadName = intent.getStringExtra("threadName");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        clearDrawable = ContextCompat.getDrawable(mContext, R.drawable.clear);
        if(!TextUtils.isEmpty(threadName)) {
            binding.etInputName.setText(threadName);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.etInputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(s)) {
                    EaseEditTextUtils.showRightDrawable(binding.etInputName, null);
                }else {
                    EaseEditTextUtils.showRightDrawable(binding.etInputName, clearDrawable);
                }
            }
        });
        binding.titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        binding.titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                changeThreadName();
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ChatThreadEditViewModel.class);
        viewModel.getResultObservable().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> booleanResource) {
                parseResource(booleanResource, new OnResourceParseCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable Boolean data) {
                        LiveDataBus.get().with(DemoConstant.THREAD_CHANGE).postValue(EaseEvent.create(DemoConstant.THREAD_CHANGE, EaseEvent.TYPE.THREAD, threadId));
                        finish();
                    }
                });
            }
        });
    }

    private void changeThreadName() {
        String name = binding.etInputName.getText().toString().trim();
        if(TextUtils.isEmpty(name)) {
            showToast("New thread name not be null");
            return;
        }
        viewModel.changeThreadName(threadId, name);
    }
}
