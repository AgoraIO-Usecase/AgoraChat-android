package io.agora.chatdemo.group.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.dialog.SimpleDialog;

public class EditGroupInfoDialog extends SimpleDialog {
    private TextView tvHint;
    private EditText etGroupInto;
    private ConfirmClickListener listener;

    @Override
    public int getMiddleLayoutId() {
        return R.layout.dialog_middle_edit;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        tvHint = findViewById(R.id.tv_hint);
        etGroupInto = findViewById(R.id.et_group_into);
        mBtnDialogCancel.setBackground(null);
        mBtnDialogConfirm.setBackground(null);
        if(getView() != null) {
            getView().setBackgroundColor(ContextCompat.getColor(mContext, R.color.dialog_edit_color_bg));
        }

        Bundle bundle = getArguments();
        if(bundle != null) {
            String textHint = bundle.getString("text_hint");
            if(!TextUtils.isEmpty(textHint)) {
                tvHint.setText(textHint);
            }
        }
        if(!TextUtils.isEmpty(content)) {
            etGroupInto.setText(content);
        }
    }

    @Override
    public void onConfirmClick(View v) {
        dismiss();
        if(this.listener != null) {
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
            EditGroupInfoDialog dialog = new EditGroupInfoDialog();
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
