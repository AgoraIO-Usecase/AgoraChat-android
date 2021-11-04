package io.agora.chatdemo.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.dialog.SimpleDialog;

public class DisbandGroupDialog extends SimpleDialog {
    private Button btnDialogDisband;
    private Button btnDialogTransfer;
    private Button btnDialogCancel;
    private View.OnClickListener transferClickListener;


    @Override
    public int getMiddleLayoutId() {
        return R.layout.layout_dialog_disband_group;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        TextView tvContent = findViewById(R.id.tv_content);
        if(!TextUtils.isEmpty(content)) {
            tvContent.setText(content);
        }
        btnDialogDisband = findViewById(R.id.btn_dialog_disband);
        btnDialogTransfer = findViewById(R.id.btn_dialog_transfer);
        btnDialogCancel = findViewById(R.id.btn_dialog_cancel);
    }

    @Override
    public void initListener() {
        super.initListener();
        btnDialogDisband.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirmClick(v);
            }
        });
        btnDialogTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(transferClickListener != null) {
                    transferClickListener.onClick(v);
                }
            }
        });
        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelClick(v);
            }
        });
    }

    private void setOnTransferClickListener(View.OnClickListener transferClickListener) {
        this.transferClickListener = transferClickListener;
    }

    public static class Builder extends SimpleDialog.Builder {

        private View.OnClickListener cancelClickListener;

        public Builder(BaseActivity context) {
            super(context);
        }

        @Override
        protected SimpleDialog getFragment() {
            DisbandGroupDialog dialog = new DisbandGroupDialog();
            dialog.setOnTransferClickListener(this.cancelClickListener);
            return dialog;
        }

        public Builder setOnTransferClickListener(View.OnClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        @Override
        public SimpleDialog build() {
            return super.build();
        }
    }

}
