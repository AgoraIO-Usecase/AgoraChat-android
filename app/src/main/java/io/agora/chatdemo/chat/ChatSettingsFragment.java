package io.agora.chatdemo.chat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.chat.viewmodel.ChatSettingsViewModel;
import io.agora.chatdemo.databinding.FragmentChatSettingsBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.widget.SwitchItemView;
import io.agora.chatdemo.me.LanguageActivity;
import io.agora.chatdemo.me.TranslationHelper;

public class ChatSettingsFragment extends BaseBottomSheetFragment implements SwitchItemView.OnCheckedChangeListener, View.OnClickListener {
    private FragmentChatSettingsBinding binding;
    private String conversationId;
    private ChatSettingsViewModel viewModel;
    private EaseChatType chatType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatSettingsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            conversationId = bundle.getString(DemoConstant.EXTRA_CONVERSATION_ID);
            chatType = EaseChatType.from(bundle.getInt(DemoConstant.EXTRA_CHAT_TYPE));
        }
    }

    @Override
    protected void initView() {
        super.initView();
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
        String extField = conversation.getExtField();
        binding.itemToTop.getSwitch().setChecked(!TextUtils.isEmpty(extField) && EaseUtils.isTimestamp(extField));
        binding.itemMuteNotification.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] autoLanguage = TranslationHelper.getLanguageByType(DemoConstant.TRANSLATION_TYPE_AUTO, conversationId);
        if (TextUtils.isEmpty(autoLanguage[1])){
            binding.settingAutoTranslation.setContent("");
        }else {
            binding.settingAutoTranslation.setContent(autoLanguage[1]);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.itemSearchMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchMessageActivity.actionStart(getActivity(), conversationId, chatType);
            }
        });
        binding.itemClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        binding.itemMuteNotification.setOnCheckedChangeListener(this);
        binding.itemToTop.setOnCheckedChangeListener(this);
        binding.settingAutoTranslation.setOnClickListener(this);
    }

    private void showDialog() {
        new SimpleDialog.Builder(((BaseActivity)mContext))
                .setTitle(R.string.chat_settings_clear_history_warning)
                .setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.clearHistory(conversationId);
                    }
                })
                .showCancelButton(true)
                .show();
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(((BaseActivity)mContext)).get(ChatSettingsViewModel.class);
        viewModel.getSetNoPushUsersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    binding.itemMuteNotification.getSwitch().setChecked(data);
                }
            });
        });

        viewModel.getNoPushUsersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(@Nullable List<String> data) {
                    binding.itemMuteNotification.getSwitch().setChecked((data != null && data.contains(conversationId)));
                }
            });
        });

        viewModel.getSetNoPushGroupsObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    binding.itemMuteNotification.getSwitch().setChecked(data);
                }
            });
        });

        viewModel.getNoPushGroupsObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(@Nullable List<String> data) {
                    binding.itemMuteNotification.getSwitch().setChecked((data != null && data.contains(conversationId)));
                }
            });
        });

        viewModel.getClearHistoryObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    hide();
                    LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE).postValue(new EaseEvent(DemoConstant.CONTACT_DECLINE, EaseEvent.TYPE.MESSAGE));
                }
            });
        });

        if(chatType == EaseChatType.SINGLE_CHAT) {
            viewModel.getNoPushUsers();
        }else {
            viewModel.getNoPushGroups();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.item_mute_notification:
                break;
            case R.id.item_to_top:
                Conversation conversation = ChatClient.getInstance().chatManager().getConversation(conversationId);
                if(isChecked) {
                    conversation.setExtField(System.currentTimeMillis()+"");
                }else {
                    conversation.setExtField("");
                }
                LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_auto_translation:
                LanguageActivity.actionStart(mContext, DemoConstant.TRANSLATION_TYPE_AUTO,1,conversationId);
                break;
            default:
                break;
        }
    }
}
