package io.agora.chatdemo.general.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chatdemo.R;


/**
 * new AlertDialog.Builder(mContext)
 *                 .setContentView(R.layout.dialog_me_setting_userinfo)
 *                 .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
 *                 .setGravity(Gravity.BOTTOM)
 *                 .setCancelable(true)
 *                 .setOnClickListener(R.id.tv_copy_id, this)
 *                 .show();
 */
public class AlertDialog extends Dialog {
    final AlertController mAlert;

    public AlertDialog(@NonNull Context context) {
        this(context, 0);

    }

    public AlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mAlert = new AlertController(getContext(), this, getWindow());
    }

    public void setText(int viewId, String text) {
        mAlert.setText(viewId, text);
    }

    public void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        mAlert.setOnClickListener(viewId, onClickListener);
    }

    public <T extends View> T getViewById(int viewId) {
        return mAlert.getViewById(viewId);
    }

    public static class Builder {
        private final AlertController.AlertParams P;
        private AlertDialog dialog;

        public Builder(Context context) {
            this(context, R.style.dialog);
        }

        public Builder(Context context, int themeResId) {
            P = new AlertController.AlertParams(
                    context, themeResId);
        }

        public Builder setContentView(int contentViewId) {
            P.contentViewId = contentViewId;
            return this;
        }

        public Builder setContentView(View contentView) {
            P.contentView = contentView;
            return this;
        }

        public Builder setText(int viewId, String text) {
            P.texts.put(viewId, text);
            return this;
        }

        public Builder setText(int viewId, CharSequence text) {
            P.texts.put(viewId, text);
            return this;
        }

        public Builder setImageview(int viewId, int imageId) {
            P.imageViews.put(viewId, imageId);
            return this;
        }

        public Builder setOnClickListener(int viewId, View.OnClickListener listener) {
            P.listeners.put(viewId, listener);
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            P.mCancelable = cancelable;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        public AlertDialog create() {
            final AlertDialog dialog = new AlertDialog(P.mContext, P.mThemeResId);
            P.apply(dialog.mAlert);
            dialog.setCancelable(P.mCancelable);
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        public AlertDialog show() {
            dialog = create();
            dialog.show();
            return dialog;
        }

        public void dismiss() {
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        public Builder setFullWidth() {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        public Builder setFromBottomAnimation() {
            P.mAnimation = R.style.dialog_from_bottom_anim;
            return this;
        }

        public Builder setGravity(int gravity) {
            P.mGravity = gravity;
            return this;
        }

        public Builder setAnimation(int animation) {
            P.mAnimation = animation;
            return this;
        }

        public Builder setLayoutParams(int width, int height) {
            P.mWidth = width;
            P.mHeight = height;
            return this;
        }
    }

}
