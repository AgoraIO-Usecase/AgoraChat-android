package io.agora.chatdemo.chatthread;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.Fragment;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.activities.EaseChatThreadCreateActivity;
import io.agora.chat.uikit.chat.interfaces.OnAddMsgAttrsBeforeSendEvent;
import io.agora.chat.uikit.chat.interfaces.OnChatExtendMenuItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnChatRecordTouchListener;
import io.agora.chat.uikit.chatthread.EaseChatThreadCreateFragment;
import io.agora.chat.uikit.chatthread.interfaces.EaseChatThreadParentMsgViewProvider;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityThreadCreateBinding;
import io.agora.chatdemo.general.permission.PermissionsManager;
import io.agora.util.EMLog;

public class ChatThreadCreateActivity extends BaseInitActivity {
    private ActivityThreadCreateBinding binding;
    public String parentId;
    public String messageId;

    public static void actionStart(Context context, String parentId, String messageId) {
        Intent intent = new Intent(context, ChatThreadCreateActivity.class);
        intent.putExtra("parentId", parentId);
        intent.putExtra("messageId", messageId);
        context.startActivity(intent);
    }

    @Override
    protected View getContentView() {
        binding = ActivityThreadCreateBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        parentId = intent.getStringExtra("parentId");
        messageId = intent.getStringExtra("messageId");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("create_chat_thread");
        if(fragment == null) {
            EaseChatThreadCreateFragment.Builder builder = new EaseChatThreadCreateFragment.Builder(parentId, messageId)
                    .useHeader(true)
                    .setHeaderBackPressListener(new EaseTitleBar.OnBackPressListener() {
                        @Override
                        public void onBackPress(View view) {
                            onBackPressed();
                        }
                    })
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
                    .setThreadParentMsgViewProvider(new EaseChatThreadParentMsgViewProvider() {
                        @Override
                        public View parentMsgView(ChatMessage message) {
                            // Add your parent view
                            return null;
                        }
                    })
                    .setOnAddMsgAttrsBeforeSendEvent(new OnAddMsgAttrsBeforeSendEvent() {
                        @Override
                        public void addMsgAttrsBeforeSend(ChatMessage message) {

                        }
                    });
            fragment = builder.build();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment, fragment, "create_chat_thread").commit();
    }
}
