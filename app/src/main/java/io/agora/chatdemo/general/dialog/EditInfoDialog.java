package io.agora.chatdemo.general.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Field;
import java.util.Objects;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.base.BaseDialogFragment;

public class EditInfoDialog extends BaseDialogFragment implements View.OnClickListener {
    public TextView mTvDialogTitle;
    public TextView mBtnDialogCancel;
    public TextView mBtnDialogConfirm;
    public EditText mEtDialogContent;
    public EditInfoDialog.OnConfirmClickListener mOnConfirmClickListener;
    public EditInfoDialog.onCancelClickListener mOnCancelClickListener;

    public String title;
    public String content;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_edit_dialog_info;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            Window dialogWindow = Objects.requireNonNull(getDialog()).getWindow();
            dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.BOTTOM;
            dialogWindow.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAllowingStateLoss(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        try {
            Field dismissed = SimpleDialog.class.getDeclaredField("mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(this, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        try {
            Field shown = SimpleDialog.class.getDeclaredField("mShownByMe");
            shown.setAccessible(true);
            shown.set(this, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        transaction.add(this, tag);
        try {
            Field viewDestroyed = SimpleDialog.class.getDeclaredField("mViewDestroyed");
            viewDestroyed.setAccessible(true);
            viewDestroyed.set(this, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        int mBackStackId = transaction.commitAllowingStateLoss();
        try {
            Field backStackId = SimpleDialog.class.getDeclaredField("mBackStackId");
            backStackId.setAccessible(true);
            backStackId.set(this, mBackStackId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void initView(Bundle savedInstanceState) {
        mTvDialogTitle = findViewById(R.id.tv_dialog_title);
        mBtnDialogCancel = findViewById(R.id.btn_dialog_cancel);
        mBtnDialogConfirm = findViewById(R.id.btn_dialog_confirm);
        mEtDialogContent = findViewById(R.id.dialog_content);

        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title");
            if (!TextUtils.isEmpty(title)) {
                mTvDialogTitle.setText(title);
            }
            int titleColor = bundle.getInt("title_color", -1);
            if (titleColor != -1) {
                mTvDialogTitle.setTextColor(titleColor);
            }
            float titleSize = bundle.getFloat("title_size");
            if (titleSize != 0) {
                mTvDialogTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize);
            }
            content = bundle.getString("content");
            if (!TextUtils.isEmpty(content)) {
                mEtDialogContent.setText(content);
                mEtDialogContent.setSelection(mEtDialogContent.getText().length(), mEtDialogContent.getText().length());
            }
            int confirmColor = bundle.getInt("confirm_color", -1);
            if (confirmColor != -1) {
                mBtnDialogConfirm.setTextColor(confirmColor);
            }
            String cancel = bundle.getString("cancel_text");
            if (!TextUtils.isEmpty(cancel)) {
                mBtnDialogCancel.setText(cancel);
            }
            int cancelColor = bundle.getInt("cancel_color", -1);
            if (cancelColor != -1) {
                mBtnDialogCancel.setTextColor(cancelColor);
            }

            boolean hideConfirm = bundle.getBoolean("hide_confirm");
            if (hideConfirm) {
                mBtnDialogConfirm.setVisibility(View.GONE);
            } else {
                mBtnDialogConfirm.setVisibility(View.VISIBLE);
            }
            boolean cancelOutsideTouch = bundle.getBoolean("cancel_outside_touch");
            if (getDialog() != null) {
                getDialog().setCanceledOnTouchOutside(cancelOutsideTouch);
            }
            boolean cancelable = bundle.getBoolean("set_cancelable");
            setCancelable(cancelable);
        }
    }

    public void initListener() {
        mBtnDialogCancel.setOnClickListener(this);
        mBtnDialogConfirm.setOnClickListener(this);
    }

    public void initData() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dialog_cancel:
                onCancelClick(v);
                break;
            case R.id.btn_dialog_confirm:
                onConfirmClick(v);
                break;
        }
    }

    private void setOnConfirmClickListener(EditInfoDialog.OnConfirmClickListener listener) {
        this.mOnConfirmClickListener = listener;
    }

    private void setOnCancelClickListener(EditInfoDialog.onCancelClickListener cancelClickListener) {
        this.mOnCancelClickListener = cancelClickListener;
    }

    public void onCancelClick(View v) {
        dismiss();
        if (mOnCancelClickListener != null) {
            mOnCancelClickListener.onCancelClick(v);
        }
    }

    public void onConfirmClick(View v) {
        dismiss();
        if (mOnConfirmClickListener != null) {
            mOnConfirmClickListener.onConfirmClick(v, mEtDialogContent.getText().toString());
        }
    }

    public interface OnConfirmClickListener {
        void onConfirmClick(View view, String content);
    }


    public interface onCancelClickListener {
        void onCancelClick(View view);
    }

    public static class Builder {
        public BaseActivity context;
        private EditInfoDialog.OnConfirmClickListener listener;
        private EditInfoDialog.onCancelClickListener cancelClickListener;
        protected Bundle bundle;

        public Builder(BaseActivity context) {
            this.context = context;
            this.bundle = new Bundle();
        }

        public EditInfoDialog.Builder setTitle(@StringRes int title) {
            this.bundle.putString("title", context.getString(title));
            return this;
        }

        public EditInfoDialog.Builder setTitle(String title) {
            this.bundle.putString("title", title);
            return this;
        }

        public EditInfoDialog.Builder setTitleColor(@ColorRes int color) {
            this.bundle.putInt("title_color", ContextCompat.getColor(context, color));
            return this;
        }

        public EditInfoDialog.Builder setTitleColorInt(@ColorInt int color) {
            this.bundle.putInt("title_color", color);
            return this;
        }

        public EditInfoDialog.Builder setTitleSize(float size) {
            this.bundle.putFloat("title_size", size);
            return this;
        }

        public EditInfoDialog.Builder setContent(@StringRes int content) {
            this.bundle.putString("content", context.getString(content));
            return this;
        }

        public EditInfoDialog.Builder setContent(String content) {
            this.bundle.putString("content", content);
            return this;
        }

        public EditInfoDialog.Builder showCancelButton(boolean showCancel) {
            this.bundle.putBoolean("show_cancel", showCancel);
            return this;
        }

        public EditInfoDialog.Builder hideConfirmButton(boolean hide) {
            this.bundle.putBoolean("hide_confirm", hide);
            return this;
        }

        public EditInfoDialog.Builder setCanceledOnTouchOutside(boolean cancel) {
            this.bundle.putBoolean("cancel_outside_touch", cancel);
            return this;
        }

        public EditInfoDialog.Builder setCancelable(boolean cancelable) {
            this.bundle.putBoolean("set_cancelable", cancelable);
            return this;
        }

        public EditInfoDialog.Builder setOnConfirmClickListener(EditInfoDialog.OnConfirmClickListener listener) {
            this.listener = listener;
            return this;
        }

        public EditInfoDialog.Builder setOnCancelClickListener(EditInfoDialog.onCancelClickListener listener) {
            this.cancelClickListener = listener;
            return this;
        }

        public EditInfoDialog.Builder setCancelColor(@ColorRes int color) {
            this.bundle.putInt("cancel_color", ContextCompat.getColor(context, color));
            return this;
        }

        public EditInfoDialog.Builder setCancelColorInt(@ColorInt int color) {
            this.bundle.putInt("cancel_color", color);
            return this;
        }

        public EditInfoDialog.Builder setArgument(Bundle bundle) {
            this.bundle.putAll(bundle);
            return this;
        }

        public EditInfoDialog build() {
            EditInfoDialog fragment = getFragment();
            fragment.setOnConfirmClickListener(this.listener);
            fragment.setOnCancelClickListener(cancelClickListener);
            fragment.setArguments(bundle);
            return fragment;
        }

        protected EditInfoDialog getFragment() {
            return new EditInfoDialog();
        }

        public EditInfoDialog show() {
            EditInfoDialog fragment = build();
            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragment.showAllowingStateLoss(transaction, null);
            return fragment;
        }
    }
}
