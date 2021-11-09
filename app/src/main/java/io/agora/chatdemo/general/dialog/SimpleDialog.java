package io.agora.chatdemo.general.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Field;

import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.base.BaseDialogFragment;

public class SimpleDialog extends BaseDialogFragment implements View.OnClickListener {
    public TextView mTvDialogTitle;
    public Button mBtnDialogCancel;
    public Button mBtnDialogConfirm;
    public OnConfirmClickListener mOnConfirmClickListener;
    public onCancelClickListener mOnCancelClickListener;
    public Group mGroupMiddle;

    public String title;
    public String content;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_dialog_base;
    }

    @Override
    public void setChildView(View view) {
        super.setChildView(view);
        int layoutId = getMiddleLayoutId();
        if(layoutId > 0) {
            addMiddleLayout(view, layoutId);
        }
    }

    private void addMiddleLayout(View view, @LayoutRes int layoutId) {
        RelativeLayout middleParent = view.findViewById(R.id.rl_dialog_middle);
        if(middleParent != null) {
            LayoutInflater.from(mContext).inflate(layoutId, middleParent);
            //同时使middleParent可见
            view.findViewById(R.id.rl_dialog_middle).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //宽度填满，高度自适应
        try {
            Window dialogWindow = getDialog().getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialogWindow.setAttributes(lp);

            View view = getView();
            if(view != null) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if(params instanceof FrameLayout.LayoutParams) {
                    int margin = (int) EaseUtils.dip2px(mContext, 30);
                    ((FrameLayout.LayoutParams) params).setMargins(margin, 0, margin, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int showAllowingStateLoss(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        try {
            Field dismissed = SimpleDialog.class.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Field shown = SimpleDialog.class.getDeclaredField("mShownByMe");
            shown.setAccessible(true);
            shown.set(this, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        transaction.add(this, tag);
        try {
            Field viewDestroyed = SimpleDialog.class.getDeclaredField("mViewDestroyed");
            viewDestroyed.setAccessible(true);
            viewDestroyed.set(this, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        int mBackStackId = transaction.commitAllowingStateLoss();
        try {
            Field backStackId = SimpleDialog.class.getDeclaredField("mBackStackId");
            backStackId.setAccessible(true);
            backStackId.set(this, mBackStackId);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mBackStackId;
    }

    /**
     * 获取中间布局的id
     * @return
     */
    public int getMiddleLayoutId() {
        return 0;
    }

    public void initView(Bundle savedInstanceState) {
        mTvDialogTitle = findViewById(R.id.tv_dialog_title);
        mBtnDialogCancel = findViewById(R.id.btn_dialog_cancel);
        mBtnDialogConfirm = findViewById(R.id.btn_dialog_confirm);
        mGroupMiddle = findViewById(R.id.group_middle);

        Bundle bundle = getArguments();
        if(bundle != null) {
            title = bundle.getString("title");
            if(!TextUtils.isEmpty(title)) {
                mTvDialogTitle.setText(title);
            }
            int titleColor = bundle.getInt("title_color", -1);
            if(titleColor != -1) {
                mTvDialogTitle.setTextColor(titleColor);
            }
            float titleSize = bundle.getFloat("title_size");
            if(titleSize != 0) {
                mTvDialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize);
            }
            content = bundle.getString("content");
            String confirm = bundle.getString("confirm_text");
            if(!TextUtils.isEmpty(confirm)) {
                mBtnDialogConfirm.setText(confirm);
            }
            int confirmColor = bundle.getInt("confirm_color", -1);
            if(confirmColor != -1) {
                mBtnDialogConfirm.setTextColor(confirmColor);
            }
            String cancel = bundle.getString("cancel_text");
            if(!TextUtils.isEmpty(cancel)) {
                mBtnDialogCancel.setText(cancel);
            }
            int cancelColor = bundle.getInt("cancel_color", -1);
            if(cancelColor != -1) {
                mBtnDialogCancel.setTextColor(cancelColor);
            }
            boolean showCancel = bundle.getBoolean("show_cancel");
            if(showCancel) {
                mGroupMiddle.setVisibility(View.VISIBLE);
            }
            boolean hideConfirm = bundle.getBoolean("hide_confirm");
            if(hideConfirm) {
                mBtnDialogConfirm.setVisibility(View.GONE);
            }else{
                mBtnDialogConfirm.setVisibility(View.VISIBLE);
            }
            boolean cancelOutsideTouch = bundle.getBoolean("cancel_outside_touch");
            if(getDialog() != null) {
                getDialog().setCanceledOnTouchOutside(cancelOutsideTouch);
            }
            boolean cancelable = bundle.getBoolean("set_cancelable");
            setCancelable(cancelable);
        }

        checkContent();
    }

    private void checkContent() {
        if(!TextUtils.isEmpty(content)) {
            // If child not set middle content layout, use default
            if(getMiddleLayoutId() == 0) {
                addMiddleLayout(requireView(), R.layout.layout_dialog_fragment_middle);
                TextView tvContent = findViewById(R.id.tv_content);
                tvContent.setText(content);

            }
        }
    }

    public void initListener() {
        mBtnDialogCancel.setOnClickListener(this);
        mBtnDialogConfirm.setOnClickListener(this);
    }

    public void initData() {}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel :
                onCancelClick(v);
                break;
            case R.id.btn_dialog_confirm:
                onConfirmClick(v);
                break;
        }
    }

    /**
     * 设置确定按钮的点击事件
     * @param listener
     */
    private void setOnConfirmClickListener(OnConfirmClickListener listener) {
        this.mOnConfirmClickListener = listener;
    }

    /**
     * 设置取消事件
     * @param cancelClickListener
     */
    private void setOnCancelClickListener(onCancelClickListener cancelClickListener) {
        this.mOnCancelClickListener = cancelClickListener;
    }

    /**
     * 点击了取消按钮
     * @param v
     */
    public void onCancelClick(View v) {
        dismiss();
        if(mOnCancelClickListener != null) {
            mOnCancelClickListener.onCancelClick(v);
        }
    }

    /**
     * 点击了确认按钮
     * @param v
     */
    public void onConfirmClick(View v) {
        dismiss();
        if(mOnConfirmClickListener != null) {
            mOnConfirmClickListener.onConfirmClick(v);
        }
    }

    /**
     * 确定事件的点击事件
     */
    public interface OnConfirmClickListener {
        void onConfirmClick(View view);
    }

    /**
     * 点击取消
     */
    public interface onCancelClickListener {
        void onCancelClick(View view);
    }

    public static class Builder {
        public BaseActivity context;
        private OnConfirmClickListener listener;
        private onCancelClickListener cancelClickListener;
        private Bundle bundle;

        public Builder(BaseActivity context) {
            this.context = context;
            this.bundle = new Bundle();
        }

        public Builder setTitle(@StringRes int title) {
            this.bundle.putString("title", context.getString(title));
            return this;
        }

        public Builder setTitle(String title) {
            this.bundle.putString("title", title);
            return this;
        }

        public Builder setTitleColor(@ColorRes int color) {
            this.bundle.putInt("title_color", ContextCompat.getColor(context, color));
            return this;
        }

        public Builder setTitleColorInt(@ColorInt int color) {
            this.bundle.putInt("title_color", color);
            return this;
        }

        public Builder setTitleSize(float size) {
            this.bundle.putFloat("title_size", size);
            return this;
        }

        public Builder setContent(@StringRes int content) {
            this.bundle.putString("content", context.getString(content));
            return this;
        }

        public Builder setContent(String content) {
            this.bundle.putString("content", content);
            return this;
        }

        public Builder showCancelButton(boolean showCancel) {
            this.bundle.putBoolean("show_cancel", showCancel);
            return this;
        }

        public Builder hideConfirmButton(boolean hide) {
            this.bundle.putBoolean("hide_confirm", hide);
            return this;
        }

        public Builder setCanceledOnTouchOutside(boolean cancel) {
            this.bundle.putBoolean("cancel_outside_touch", cancel);
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.bundle.putBoolean("set_cancelable", cancelable);
            return this;
        }

        public Builder setOnConfirmClickListener(@StringRes int confirm, OnConfirmClickListener listener) {
            this.bundle.putString("confirm_text", context.getString(confirm));
            this.listener = listener;
            return this;
        }

        public Builder setOnConfirmClickListener(String confirm, OnConfirmClickListener listener) {
            this.bundle.putString("confirm_text", confirm);
            this.listener = listener;
            return this;
        }

        public Builder setOnConfirmClickListener(OnConfirmClickListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder setConfirmColor(@ColorRes int color) {
            this.bundle.putInt("confirm_color", ContextCompat.getColor(context, color));
            return this;
        }

        public Builder setConfirmColorInt(@ColorInt int color) {
            this.bundle.putInt("confirm_color", color);
            return this;
        }

        public Builder setOnCancelClickListener(@StringRes int cancel, onCancelClickListener listener) {
            this.bundle.putString("cancel_text", context.getString(cancel));
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setOnCancelClickListener(String cancel, onCancelClickListener listener) {
            this.bundle.putString("cancel_text", cancel);
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setOnCancelClickListener(onCancelClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        public Builder setCancelColor(@ColorRes int color) {
            this.bundle.putInt("cancel_color", ContextCompat.getColor(context, color));
            return this;
        }

        public Builder setCancelColorInt(@ColorInt int color) {
            this.bundle.putInt("cancel_color", color);
            return this;
        }

        public Builder setArgument(Bundle bundle) {
            this.bundle.putAll(bundle);
            return this;
        }

        public SimpleDialog build() {
            SimpleDialog fragment = getFragment();
            fragment.setOnConfirmClickListener(this.listener);
            fragment.setOnCancelClickListener(cancelClickListener);
            fragment.setArguments(bundle);
            return fragment;
        }

        protected SimpleDialog getFragment() {
            return new SimpleDialog();
        }

        public SimpleDialog show() {
            SimpleDialog fragment = build();
            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragment.showAllowingStateLoss(transaction, null);
            return fragment;
        }
    }
}
