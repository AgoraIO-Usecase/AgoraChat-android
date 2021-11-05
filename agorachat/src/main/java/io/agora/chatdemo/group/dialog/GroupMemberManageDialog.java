package io.agora.chatdemo.group.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.R;
import io.agora.chatdemo.group.adapter.GroupMemberManageAdapter;
import io.agora.chatdemo.group.model.GroupManageItemBean;

public class GroupMemberManageDialog extends EaseAlertDialog implements OnItemClickListener {
    private GroupMemberManageAdapter adapter;

    private RecyclerView rvDialog;
    private Button btnCancel;
    private OnItemClickListener itemClickListener;
    private List<GroupManageItemBean> data;

    public GroupMemberManageDialog(@NonNull Context context) {
        super(context);
    }

    public GroupMemberManageDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void setContentView(@NonNull View view) {
        super.setContentView(view);
        setDialogAttrs();
    }

    private void setDialogAttrs() {
        try {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_group_member_manage, null);
        setContentView(view);
        rvDialog = view.findViewById(R.id.rv_dialog);
        btnCancel = view.findViewById(R.id.btn_cancel);
        rvDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDialog.setHasFixedSize(true);

        ConcatAdapter concatAdapter = new ConcatAdapter();
        adapter = new GroupMemberManageAdapter();
        concatAdapter.addAdapter(adapter);
        rvDialog.setAdapter(concatAdapter);

        adapter.setData(this.data);

        adapter.setOnItemClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        dismiss();
        if(itemClickListener != null) {
            itemClickListener.onItemClick(view, position);
        }
    }

    private void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    private void setData(List<GroupManageItemBean> data) {
        this.data = data;
    }

    public static class Builder extends EaseAlertDialog.Builder<GroupMemberManageDialog> {
        private Context context;
        private Bundle bundle;
        private OnItemClickListener itemClickListener;
        private List<GroupManageItemBean> data;

        public Builder(Context context) {
            super(context);
            this.context = context;
            this.bundle = new Bundle();
        }

        public Builder setOnItemClickListener(OnItemClickListener listener) {
            this.itemClickListener = listener;
            return this;
        }

        public Builder setData(List<GroupManageItemBean> data) {
            this.data = data;
            return this;
        }

        @Override
        public EaseAlertDialog.Builder<GroupMemberManageDialog> setCustomDialog(GroupMemberManageDialog dialog) {
            dialog.setOnItemClickListener(itemClickListener);
            dialog.setData(this.data);
            return super.setCustomDialog(dialog);
        }
    }
}
