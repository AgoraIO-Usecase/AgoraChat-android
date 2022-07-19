package io.agora.chatdemo.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.base.BaseDialogFragment;

public class ReportDialogFragment extends BaseDialogFragment {
    private TextView tvTitle;
    private View viewDivider;
    private RecyclerView rvDialogList;
    private TextView btnCancel;
    private EaseBaseRecyclerViewAdapter adapter;

    private String title;
    private String cancel;
    private int cancelColor;
    private OnDialogItemClickListener itemClickListener;
    private List<String> data;
    private OnDialogCancelClickListener cancelClickListener;
    private int animations;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_report_layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogParams();
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        if(animations != 0) {
            try {
                getDialog().getWindow().setWindowAnimations(animations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tvTitle = findViewById(R.id.tv_title);
        viewDivider = findViewById(R.id.view_divider);
        rvDialogList = findViewById(R.id.rv_dialog_list);
        btnCancel = findViewById(R.id.btn_cancel);

        if(TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
            viewDivider.setVisibility(View.GONE);
        }else {
            tvTitle.setVisibility(View.VISIBLE);
            viewDivider.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }

        if(TextUtils.isEmpty(cancel)) {
            btnCancel.setText(getString(R.string.dialog_btn_cancel));
        }else {
            btnCancel.setText(cancel);
        }

        if(cancelColor != 0) {
            btnCancel.setTextColor(cancelColor);
        }
    }

    @Override
    public void initListener() {
        super.initListener();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(cancelClickListener != null) {
                    cancelClickListener.OnCancel(v);
                }
            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        rvDialogList.setLayoutManager(new LinearLayoutManager(mContext));
        if(adapter == null) {
            adapter = getDefaultAdapter();
        }
        rvDialogList.setAdapter(adapter);

        rvDialogList.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        adapter.setData(data);

        this.adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();
                if(itemClickListener != null) {
                    itemClickListener.OnItemClick(view, position);
                }
            }
        });
    }


    public static class Builder {
        private BaseActivity context;
        private String title;
        private EaseBaseRecyclerViewAdapter adapter;
        private List<String> data;
        private OnDialogItemClickListener clickListener;
        private String cancel;
        private int cancelColor;
        private OnDialogCancelClickListener cancelClickListener;
        private Bundle bundle;
        private int animations;

        public Builder(BaseActivity context){
            this.context = context;
        }

        public Builder setTitle(@StringRes int title) {
            this.title = context.getString(title);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setAdapter(EaseBaseRecyclerViewAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        public Builder setData(List<String> data) {
            this.data = data;
            return this;
        }

        public Builder setData(String[] data) {
            this.data = Arrays.asList(data);
            return this;
        }

        public Builder setOnItemClickListener(OnDialogItemClickListener listener) {
            this.clickListener = listener;
            return this;
        }

        public Builder setOnCancelClickListener(@StringRes int cancel, OnDialogCancelClickListener listener) {
            this.cancel = context.getString(cancel);
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setOnCancelClickListener(String cancel, OnDialogCancelClickListener listener) {
            this.cancel = cancel;
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setCancelColorRes(@ColorRes int color) {
            this.cancelColor = ContextCompat.getColor(context, color);
            return this;
        }

        public Builder setCancelColor(@ColorInt int color) {
            this.cancelColor = color;
            return this;
        }

        public Builder setArgument(Bundle bundle) {
            this.bundle = bundle;
            return this;
        }

        public Builder setWindowAnimations(@StyleRes int animations) {
            this.animations = animations;
            return this;
        }

        public ReportDialogFragment build() {
            ReportDialogFragment fragment = new ReportDialogFragment();
            fragment.setTitle(title);
            fragment.setAdapter(adapter);
            fragment.setData(data);
            fragment.setOnItemClickListener(this.clickListener);
            fragment.setCancel(cancel);
            fragment.setCancelColor(cancelColor);
            fragment.setOnCancelClickListener(this.cancelClickListener);
            fragment.setArguments(this.bundle);
            fragment.setWindowAnimations(animations);
            return fragment;
        }

        public ReportDialogFragment  show() {
            ReportDialogFragment fragment = build();
            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragment.show(transaction, null);
            return fragment;
        }

    }

    private void setCancelColor(int cancelColor) {
        this.cancelColor = cancelColor;
    }

    private void setWindowAnimations(int animations) {
        this.animations = animations;
    }

    private void setData(List<String> data) {
        this.data = data;
    }

    private void setOnCancelClickListener(OnDialogCancelClickListener cancelClickListener) {
        this.cancelClickListener = cancelClickListener;
    }

    private void setCancel(String cancel) {
        this.cancel = cancel;
    }

    private void setOnItemClickListener(OnDialogItemClickListener clickListener) {
        this.itemClickListener = clickListener;
    }

    private void setAdapter(EaseBaseRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

    private EaseBaseRecyclerViewAdapter getDefaultAdapter() {
        return new DefaultAdapter();
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public interface OnDialogItemClickListener {
        void OnItemClick(View view, int position);
    }

    public interface OnDialogCancelClickListener {
        void OnCancel(View view);
    }


    private class DefaultAdapter extends EaseBaseRecyclerViewAdapter<String> {

        @Override
        public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_default_list_item, parent, false);
            return new MyViewHolder(view);
        }

        private class MyViewHolder extends ViewHolder<String> {
            private TextView content;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            @Override
            public void initView(View itemView) {
                content = findViewById(R.id.tv_title);
            }

            @Override
            public void setData(String item, int position) {
                content.setText(item);
            }
        }
    }
}
