package io.agora.chatdemo.general.manager;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Refer to：https://blog.csdn.net/auccy/article/details/80664234
 */
public class SoftKeyboardChangeHelper {
    //View height change amount, if this amount is exceeded, the soft keyboard state has changed
    private static final int CHANG_VALUE = 200;
    /**
     * activity's root view
     */
    private View rootView;
    private int rootViewVisibleHeight;
    private OnSoftKeyboardChangeListener listener;

    public SoftKeyboardChangeHelper(Activity activity) {
        addRootViewListener(activity);
    }

    private void addRootViewListener(Activity activity) {
        rootView = activity.getWindow().getDecorView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);

                int visibleHeight = r.height();
                if(rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //The root view shows no change in height,
                // which can be interpreted as the soft keyboard show/hide status unchanged
                if(rootViewVisibleHeight == visibleHeight) {
                    return;
                }

                //If the height of the root view becomes smaller and exceeds the preset value,
                // the soft keyboard is considered to be displayed
                if(rootViewVisibleHeight - visibleHeight > CHANG_VALUE) {
                    if(listener != null) {
                        listener.keyboardShow(rootViewVisibleHeight - visibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //If the root view displays a higher height than the preset value, the soft keyboard is hidden
                if(visibleHeight - rootViewVisibleHeight > CHANG_VALUE) {
                    if(listener != null) {
                        listener.keyboardHide(visibleHeight - rootViewVisibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                }
            }
        });
    }

    /**
     * Set listener
     * @param activity
     * @param listener
     */
    public static void setOnSoftKeyboardChangeListener(Activity activity, OnSoftKeyboardChangeListener listener) {
        SoftKeyboardChangeHelper helper = new SoftKeyboardChangeHelper(activity);
        helper.setOnSoftKeyboardChangeListener(listener);
    }

    /**
     * Set soft keyboard change listener
     * @param listener
     */
    public void setOnSoftKeyboardChangeListener(OnSoftKeyboardChangeListener listener) {
        this.listener = listener;
    }

    public interface OnSoftKeyboardChangeListener {
        /**
         * keyboard display
         * @param height
         */
        void keyboardShow(int height);

        /**
         * keyboard hide
         * @param height
         */
        void keyboardHide(int height);
    }
}
