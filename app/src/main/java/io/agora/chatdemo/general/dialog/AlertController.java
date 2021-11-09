package io.agora.chatdemo.general.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.collection.ArrayMap;


class AlertController {
    private final Context mContext;
    private final Dialog dialog;
    private final Window window;
    private DialogViewHelper viewHelper;

    public AlertController(Context context, Dialog dialog, Window window) {
        this.mContext = context;
        this.dialog = dialog;
        this.window = window;

    }

    public Context getmContext() {
        return mContext;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public Window getWindow() {
        return window;
    }

    public <T extends View> T getViewById(int viewId) {
        return viewHelper.getViewById(viewId);
    }

    static class AlertParams {

        public final Context mContext;//上下文
        public final int mThemeResId;//主题
        public boolean mCancelable = true;//区域外是否点击取消
        public DialogInterface.OnCancelListener mOnCancelListener;//取消监听
        public DialogInterface.OnDismissListener mOnDismissListener;//窗口小时监听
        public DialogInterface.OnKeyListener mOnKeyListener;//点击监听
        public SparseArray<CharSequence> texts;//容器 用来装设置的文本
        public ArrayMap<Integer, Integer> imageViews;//容器 用来装设置的图片
        public SparseArray<View.OnClickListener> listeners;//容器 用来装设置的监听
        public int contentViewId;//dialog内容布局的资源id
        public View contentView;//dialog内容布局的内容实例
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;//设置dialog宽度
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;//设置dialog高度
        public int mAnimation = 0;//设置dia弹出动画
        public int mGravity = Gravity.CENTER;//设置dialog的gravity属性


        public AlertParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
            texts = new SparseArray();
            listeners = new SparseArray();
            imageViews = new ArrayMap<>();
        }

        /**
         * 将参数具体设置进mAlert中去实现
         *
         * @param mAlert
         */
        public void apply(AlertController mAlert) {
            DialogViewHelper viewHelper = null;
            if (contentView != null) {
                viewHelper = new DialogViewHelper(mContext, contentView);
            }
            if (contentViewId != 0) {
                viewHelper = new DialogViewHelper(mContext, contentViewId);
            }
            if (viewHelper == null) {
                throw new IllegalArgumentException("未设置布局");
            }
            //给dialog设置布局
            mAlert.getDialog().setContentView(viewHelper.getContentView());
            //给dialog设置view的帮助类
            mAlert.setViewHelper(viewHelper);
            //给dialog设置文本
            int textsSize = texts.size();
            for (int i = 0; i < textsSize; i++) {
                mAlert.setText(texts.keyAt(i), texts.valueAt(i));
            }
            //给dialog设置图片
            int imageViewsCount = imageViews.size();
            for (int i = 0; i < imageViewsCount; i++) {
                mAlert.setImageView(imageViews.keyAt(i), imageViews.valueAt(i));
            }
            //给dialog设置监听
            int listenerSize = listeners.size();
            for (int i = 0; i < listenerSize; i++) {
                mAlert.setOnClickListener(listeners.keyAt(i), listeners.valueAt(i));
            }
            //设置宽高、弹出效果等
            Window window = mAlert.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            //设置宽度
            if (mWidth != 0) {
                params.width = mWidth;
                params.height = mHeight;
            }
            window.setAttributes(params);
            //设置dialog的gravity属性
            window.setGravity(mGravity);
            //设置弹出动画
            if (mAnimation != 0) {
                window.setWindowAnimations(mAnimation);
            }

        }
    }

    /**
     * 设置图片，具体工作交给viewhelper去干
     *
     * @param viewId
     * @param resId
     */
    private void setImageView(int viewId, int resId) {
        viewHelper.setImageView(viewId, resId);
    }

    /**
     * 设置控件点击监听，具体工作交给viewHelper去干
     *
     * @param viewId
     * @param onClickListener
     */
    void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        viewHelper.setOnClickListener(viewId, onClickListener);
    }

    /**
     * 设置view帮助类
     *
     * @param viewHelper
     */
    private void setViewHelper(DialogViewHelper viewHelper) {
        this.viewHelper = viewHelper;
    }

    /**
     * 设置控件文本，具体工作交给viewHelper去干
     *
     * @param viewId
     * @param text
     */
    void setText(int viewId, CharSequence text) {
        viewHelper.setText(viewId, text);
    }
}
