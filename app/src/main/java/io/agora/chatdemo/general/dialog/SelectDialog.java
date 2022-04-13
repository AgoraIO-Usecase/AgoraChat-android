package io.agora.chatdemo.general.dialog;

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
import io.agora.chatdemo.general.adapter.SelectDialogAdapter;
import io.agora.chatdemo.general.models.SelectDialogItemBean;

public class SelectDialog extends EaseAlertDialog implements OnItemClickListener {
    private SelectDialogAdapter adapter;

    private RecyclerView rvDialog;
    private Button btnCancel;
    private OnItemClickListener itemClickListener;
    private List<SelectDialogItemBean> data;
    private String title;
    private TextView tvTitle;

    public SelectDialog(@NonNull Context context) {
        super(context);
    }

    public SelectDialog(@NonNull Context context, int themeResId) {
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
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_group_file_manage, null);
        setContentView(view);
        rvDialog = view.findViewById(R.id.rv_dialog);
        btnCancel = view.findViewById(R.id.btn_cancel);
        tvTitle = findViewById(R.id.tv_title);
        rvDialog.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDialog.setHasFixedSize(true);

        ConcatAdapter concatAdapter = new ConcatAdapter();
        adapter = new SelectDialogAdapter();
        adapter.hideEmptyView(true);
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

        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        } else {
            tvTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        dismiss();
        if (itemClickListener != null) {
            itemClickListener.onItemClick(view, position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setData(List<SelectDialogItemBean> data) {
        this.data = data;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
