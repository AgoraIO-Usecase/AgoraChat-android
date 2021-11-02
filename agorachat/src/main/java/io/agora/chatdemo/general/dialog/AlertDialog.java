package io.agora.chatdemo.general.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chatdemo.R;


/**
 * Sample:
 * val dialog = AlertDialog.Builder(this)
 *                 .setContentView(R.layout.dialog_reset_pwd)
 *                 .setText(R.id.tv_message, msg)
 *                 .setLayoutParams(UIUtils.dp2px(this, 256), ViewGroup.LayoutParams.WRAP_CONTENT)
 *                 .show()
 *         dialog.setOnClickListener(R.id.btn_input_again, object : View.OnClickListener {
 *             override fun onClick(v: View?) {
 *                 dialog.dismiss()
 *             }
 *         })
 */
public class AlertDialog extends Dialog {
    //具体的实现类
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

    /**
     * 建造者模式的实现
     */
    public static class Builder {
        //容器 用来存储事先设置的参数
        private final AlertController.AlertParams P;
        private AlertDialog dialog;

        public Builder(Context context) {
            this(context, R.style.dialog);
        }

        public Builder(Context context, int themeResId) {
            P = new AlertController.AlertParams(
                    context, themeResId);
        }

        /**
         * 以资源id形式设置布局
         *
         * @param contentViewId
         * @return
         */
        public Builder setContentView(int contentViewId) {
            P.contentViewId = contentViewId;
            return this;
        }

        /**
         * 以view对象形式设置布局
         *
         * @param contentView
         * @return
         */
        public Builder setContentView(View contentView) {
            P.contentView = contentView;
            return this;
        }

        /**
         * 根据控件id来设置文字内容
         *
         * @param viewId
         * @param text
         * @return
         */
        public Builder setText(int viewId, String text) {
            P.texts.put(viewId, text);
            return this;
        }

        public Builder setText(int viewId, CharSequence text) {
            P.texts.put(viewId, text);
            return this;
        }
        /**
         * 根据控件id来设置图片
         *
         * @param viewId
         * @param imageId
         * @return
         */
        public Builder setImageview(int viewId, int imageId) {
            P.imageViews.put(viewId, imageId);
            return this;
        }

        /**
         * 根据控件id来设置监听
         *
         * @param viewId
         * @param listener
         * @return
         */
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

        /**
         * 设置全宽
         *
         * @return
         */
        public Builder setFullWidth() {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        /**
         * 设置底部弹出动画
         *
         * @return
         */
        public Builder setFromBottomAnimation() {
            P.mAnimation = R.style.dialog_from_bottom_anim;
            return this;
        }

        /**
         * 设置gravity等属性
         *
         * @param gravity
         * @return
         */
        public Builder setGravity(int gravity) {
            P.mGravity = gravity;
            return this;
        }

        /**
         * 设置动画效果
         *
         * @param animation
         * @return
         */
        public Builder setAnimation(int animation) {
            P.mAnimation = animation;
            return this;
        }

        /**
         * 设置布局宽高等参数
         *
         * @param width
         * @param height
         * @return
         */
        public Builder setLayoutParams(int width, int height) {
            P.mWidth = width;
            P.mHeight = height;
            return this;
        }
    }

}
