package io.agora.chatdemo.group.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityRemarkBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.model.MemberAttributeBean;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;

public class RemarkActivity extends BaseInitActivity implements View.OnClickListener {
    private String targetId;
    private String groupId;
    private String alias;
    private RemarkType remarkType;
    private ActivityRemarkBinding binding;
    private GroupDetailViewModel viewModel;
    private MemberAttributeBean memberAttributeBean;

    enum RemarkType {
        CONTACT,
        GROUP
    }

    @Override
    protected View getContentView() {
        binding = ActivityRemarkBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    public static void actionStart(Activity context,int requestCode, String targetId, String groupId, String alias) {
        Intent intent = new Intent(context, RemarkActivity.class);
        intent.putExtra("remarkType", RemarkType.GROUP);
        intent.putExtra("targetId", targetId);
        intent.putExtra("groupId", groupId);
        intent.putExtra("alias", alias);
        context.startActivityForResult(intent,requestCode);
    }

    public static void actionStart(Context context, String targetId, String alias) {
        Intent intent = new Intent(context, RemarkActivity.class);
        intent.putExtra("remarkType", RemarkType.CONTACT);
        intent.putExtra("targetId", targetId);
        intent.putExtra("alias", alias);
        context.startActivity(intent);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        remarkType = (RemarkType) getIntent().getSerializableExtra("remarkType");
        targetId = getIntent().getStringExtra("targetId");
        groupId = getIntent().getStringExtra("groupId");
        alias = getIntent().getStringExtra("alias");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.setMemberAttributeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, MemberAttributeBean>>() {
                @Override
                public void onSuccess(@Nullable Map<String, MemberAttributeBean> data) {
                    if (data != null) {
                        for (Map.Entry<String, MemberAttributeBean> entry : data.entrySet()) {
                            // map中只有一条数据 此页面设置只提供设置单个id
                            memberAttributeBean = DemoHelper.getInstance().getMemberAttribute(groupId, entry.getKey());
                            //发送通知
                            EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
                            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(event);
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
                }
            });
        });


    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.ivBack.setOnClickListener(this);
        binding.ivDelete.setOnClickListener(this);
        binding.tvDone.setOnClickListener(this);
        binding.edtRemark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.toString().trim().length();
                binding.tvCount.setText(length + "/50");
                if (length == 0) {
                    binding.ivDelete.setVisibility(View.GONE);
                    binding.tvDone.setEnabled(false);
                    binding.edtRemark.requestFocus();
                } else {
                    if (TextUtils.equals(s.toString().trim(), alias)) {
                        binding.tvDone.setEnabled(false);
                    } else {
                        binding.tvDone.setEnabled(true);
                    }
                    binding.ivDelete.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.edtRemark.setText(alias == null ? "" : alias);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_done:
                remark(binding.edtRemark.getText().toString().trim());
                break;
            case R.id.iv_back:
                checkAlias(binding.edtRemark.getText().toString().trim());
                break;
            case R.id.iv_delete:
                binding.edtRemark.setText("");
                break;
        }
    }

    private void checkAlias(String content) {

        if (!TextUtils.equals(content, alias)) {
            showConfirmDialog();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void showConfirmDialog() {
        new SimpleDialog.Builder(this)
                .setTitle(R.string.demo_discard_alias_title)
                .setContent(R.string.demo_discard_alias_content)
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }).show();
    }

    private void remark(String content) {
        if (remarkType == RemarkType.GROUP) {
            //修改我在群里的昵称
            viewModel.setGroupMemberAttributes(groupId, targetId, content);
        }
    }


}