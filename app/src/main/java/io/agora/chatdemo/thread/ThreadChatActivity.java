package io.agora.chatdemo.thread;

import io.agora.chat.uikit.activities.EaseThreadChatActivity;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.EaseChatLayout;
import io.agora.chat.uikit.chat.interfaces.OnChatLayoutFinishInflateListener;
import io.agora.chat.uikit.widget.EaseTitleBar;

public class ThreadChatActivity extends EaseThreadChatActivity {
    private EaseTitleBar titleBar;
    private EaseChatLayout chatLayout;

    @Override
    public void initView() {
        super.initView();

    }

    @Override
    public void setChildFragmentBuilder(EaseChatFragment.Builder builder) {
        super.setChildFragmentBuilder(builder);
        builder.setOnChatLayoutFinishInflateListener(new OnChatLayoutFinishInflateListener() {
            @Override
            public void onChatListFinishInflate(EaseChatLayout chatLayout) {
                ThreadChatActivity.this.chatLayout = chatLayout;
            }

            @Override
            public void onTitleBarFinishInflate(EaseTitleBar titleBar) {
                ThreadChatActivity.this.titleBar = titleBar;
            }
        });
    }
}
