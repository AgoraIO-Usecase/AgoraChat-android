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

        public final Context mContext;
        public final int mThemeResId;//theme
        public boolean mCancelable = true;//Whether to click cancel outside the area
        public DialogInterface.OnCancelListener mOnCancelListener;//cancel listening
        public DialogInterface.OnDismissListener mOnDismissListener;//window disappearing listener
        public DialogInterface.OnKeyListener mOnKeyListener;//Click to listen
        public SparseArray<CharSequence> texts;//container to hold the set text
        public ArrayMap<Integer, Integer> imageViews;//Container for pictures of settings
        public SparseArray<View.OnClickListener> listeners;//Container used to install listeners for settings
        public int contentViewId;
        public View contentView;
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;//set dialog width
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;//set dialog height
        public int mAnimation = 0;//set dialog animation
        public int mGravity = Gravity.CENTER;


        public AlertParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
            texts = new SparseArray();
            listeners = new SparseArray();
            imageViews = new ArrayMap<>();
        }

        /**
         * Set the parameters into mAlert to achieve
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
                throw new IllegalArgumentException("Not set layout");
            }
            //set dialog layout
            mAlert.getDialog().setContentView(viewHelper.getContentView());
            //set dialog helper class
            mAlert.setViewHelper(viewHelper);
            //set dialog text
            int textsSize = texts.size();
            for (int i = 0; i < textsSize; i++) {
                mAlert.setText(texts.keyAt(i), texts.valueAt(i));
            }
            //set dialog picture
            int imageViewsCount = imageViews.size();
            for (int i = 0; i < imageViewsCount; i++) {
                mAlert.setImageView(imageViews.keyAt(i), imageViews.valueAt(i));
            }
            //set dialog listener
            int listenerSize = listeners.size();
            for (int i = 0; i < listenerSize; i++) {
                mAlert.setOnClickListener(listeners.keyAt(i), listeners.valueAt(i));
            }
            Window window = mAlert.getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            //set width and height
            if (mWidth != 0) {
                params.width = mWidth;
                params.height = mHeight;
            }
            window.setAttributes(params);
            window.setGravity(mGravity);
            //set animation
            if (mAnimation != 0) {
                window.setWindowAnimations(mAnimation);
            }

        }
    }

    /**
     * set picture
     *
     * @param viewId
     * @param resId
     */
    private void setImageView(int viewId, int resId) {
        viewHelper.setImageView(viewId, resId);
    }

    /**
     * set listener
     *
     * @param viewId
     * @param onClickListener
     */
    void setOnClickListener(int viewId, View.OnClickListener onClickListener) {
        viewHelper.setOnClickListener(viewId, onClickListener);
    }

    /**
     * set viewHelper
     *
     * @param viewHelper
     */
    private void setViewHelper(DialogViewHelper viewHelper) {
        this.viewHelper = viewHelper;
    }

    /**
     * set text
     *
     * @param viewId
     * @param text
     */
    void setText(int viewId, CharSequence text) {
        viewHelper.setText(viewId, text);
    }
}
