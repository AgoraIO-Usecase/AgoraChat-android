package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;

public class ChatReportCompleteActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_report_complete);

        findViewById(R.id.complete_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ChatReportCompleteActivity.class);
        context.startActivity(intent);
    }
}
