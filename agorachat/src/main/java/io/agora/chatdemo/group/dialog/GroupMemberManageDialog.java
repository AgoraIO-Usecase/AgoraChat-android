package io.agora.chatdemo.group.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.widget.DividerItemDecoration;
import io.agora.chatdemo.group.adapter.GroupMemberManageAdapter;
import io.agora.chatdemo.group.model.GroupManageItemBean;

public class GroupMemberManageDialog extends EaseAlertDialog implements OnItemClickListener {
    private GroupMemberManageAdapter adapter;

    private RecyclerView rvDialog;
    private Button btnCancel;
    private OnItemClickListener itemClickListener;
    private List<GroupManageItemBean> data;
    private String username;
    private TextView tvName;

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

    public void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_group_member_manage, null);
        setContentView(view);
        rvDialog = view.findViewById(R.id.rv_dialog);
        btnCancel = view.findViewById(R.id.btn_cancel);
        tvName = findViewById(R.id.tv_name);
        rvDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDialog.setHasFixedSize(true);

        ConcatAdapter concatAdapter = new ConcatAdapter();
        adapter = new GroupMemberManageAdapter();
        concatAdapter.addAdapter(adapter);
        rvDialog.setAdapter(concatAdapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, false);
        itemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_dialog_menu_list));
        rvDialog.addItemDecoration(itemDecoration);

        adapter.setData(this.data);

        adapter.setOnItemClickListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if(!TextUtils.isEmpty(username)) {
            tvName.setText(username);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        dismiss();
        if(itemClickListener != null) {
            itemClickListener.onItemClick(view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setData(List<GroupManageItemBean> data) {
        this.data = data;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
