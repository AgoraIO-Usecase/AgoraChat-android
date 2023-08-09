package io.agora.chatdemo.group.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.dialog.SimpleDialog;

public class EditMemberAliasDialog extends SimpleDialog {
    private TextView tvHint;
    private EditText etGroupInto;
    private ImageView ivDelete;
    private ConfirmClickListener listener;

    @Override
    public int getMiddleLayoutId() {
        return R.layout.dialog_group_member_alias_edit;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        tvHint = findViewById(R.id.tv_hint);
        etGroupInto = findViewById(R.id.et_group_into);
        ivDelete = findViewById(R.id.iv_delete);


    }

    @Override
    public void initData() {
        super.initData();
        Bundle bundle = getArguments();
        if (bundle != null) {
            String textHint = bundle.getString("text_hint");
            if (!TextUtils.isEmpty(textHint)) {
                tvHint.setText(textHint);
            }
        }
        if (!TextUtils.isEmpty(content)) {
            etGroupInto.setText(content);
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etGroupInto.setText("");
            }
        });
        etGroupInto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.toString().trim().length();
                if(length==0) {
                    ivDelete.setVisibility(View.GONE);
                }else{
                    ivDelete.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onConfirmClick(View v) {
        dismiss();
        if (this.listener != null) {
            this.listener.onConfirmClick(v, etGroupInto.getText().toString().trim());
        }
    }

    private void setOnConfirmClickListener(ConfirmClickListener listener) {
        this.listener = listener;
    }

    public interface ConfirmClickListener {
        void onConfirmClick(View view, String content);
    }

    public static class Builder extends SimpleDialog.Builder {

        private ConfirmClickListener cancelClickListener;

        public Builder(BaseActivity context) {
            super(context);
        }

        @Override
        protected SimpleDialog getFragment() {
            EditMemberAliasDialog dialog = new EditMemberAliasDialog();
            dialog.setOnConfirmClickListener(this.cancelClickListener);
            return dialog;
        }

        public Builder setConfirmClickListener(ConfirmClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setHint(String hint) {
            this.bundle.putString("text_hint", hint);
            return this;
        }

        @Override
        public SimpleDialog build() {
            return super.build();
        }
    }
}
