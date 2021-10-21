package io.agora.chatdemo.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;

public class ContactDetailActivity extends BaseActivity {

    public static void actionStart(Context context, String username) {
        Intent intent = new Intent(context, ContactDetailActivity.class);
        intent.putExtra("username", username);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
    }
}
