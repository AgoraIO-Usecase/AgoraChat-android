package io.agora.chatdemo.general.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.lang.reflect.Field;

import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.R;

/**
 * Toast tool class, unify the Toast style, deal with the problem of repeated display, and deal with the problem of version 7.1.x crash
 */
public class ToastUtils {
    private static final int DEFAULT = 0;
    private static final int SUCCESS = 1;
    private static final int FAIL = 2;
    private static final int TOAST_LAST_TIME = 1000;
    private static Toast toast;

    /**
     * Pop up a successful toast
     * @param message
     */
    public static void showSuccessToast(String message) {
        showCenterToast(null, message, SUCCESS, TOAST_LAST_TIME);
    }

    /**
     * Pop up a successful toast
     * @param message
     */
    public static void showSuccessToast(@StringRes int message) {
        showCenterToast(0, message, SUCCESS, TOAST_LAST_TIME);
    }

    /**
     * Failed toast pops up
     * @param message
     */
    public static void showFailToast(String message) {
        showCenterToast(null, message, FAIL, TOAST_LAST_TIME);
    }

    /**
     * Failed toast pops up
     * @param message
     */
    public static void showFailToast(@StringRes int message) {
        showCenterToast(0, message, FAIL, TOAST_LAST_TIME);
    }

    /**
     * Pop up the default toast
     * @param message
     */
    public static void showToast(String message) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showBottomToast(null, message, DEFAULT, TOAST_LAST_TIME);
    }

    /**
     * Pop up the default toast
     * @param message
     */
    public static void showToast(@StringRes int message) {
        showBottomToast(0, message, DEFAULT, TOAST_LAST_TIME);
    }

    /**
     * Pop up a successful toast with a title
     * @param title
     * @param message
     */
    public static void showSuccessToast(String title, String message) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showCenterToast(title, message, SUCCESS, TOAST_LAST_TIME);
    }

    /**
     * Pop up a successful toast with a title
     * @param title
     * @param message
     */
    public static void showSuccessToast(@StringRes int title, @StringRes int message) {
        showCenterToast(title, message, SUCCESS, TOAST_LAST_TIME);
    }

    /**
     * A failed toast pops up with a title
     * @param title
     * @param message
     */
    public static void showFailToast(String title, String message) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showCenterToast(title, message, FAIL, TOAST_LAST_TIME);
    }

    /**
     * A failed toast pops up with a title
     * @param title
     * @param message
     */
    public static void showFailToast(@StringRes int title, @StringRes int message) {
        showCenterToast(title, message, FAIL, TOAST_LAST_TIME);
    }

    /**
     * A successful toast pops up, with a title, and the display duration can be set
     * @param title
     * @param message
     * @param duration
     */
    public static void showSuccessToast(String title, String message, int duration) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showCenterToast(title, message, SUCCESS, duration);
    }

    /**
     * A successful toast pops up, with a title, and the display duration can be set
     * @param title
     * @param message
     * @param duration
     */
    public static void showSuccessToast(@StringRes int title, @StringRes int message, int duration) {
        showCenterToast(title, message, SUCCESS, duration);
    }

    /**
     * The failed toast pops up, with a title, and the display duration can be set
     * @param title
     * @param message
     * @param duration
     */
    public static void showFailToast(String title, String message, int duration) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showCenterToast(title, message, FAIL, duration);
    }

    /**
     * The failed toast pops up, with a title, and the display duration can be set
     * @param title
     * @param message
     * @param duration
     */
    public static void showFailToast(@StringRes int title, @StringRes int message, int duration) {
        showCenterToast(title, message, FAIL, duration);
    }

    /**
     * Pop up toast, no icon, no title, you can set the display time
     * @param message
     * @param duration
     */
    public static void showToast(String message, int duration) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showCenterToast(null, message, DEFAULT, duration);
    }

    /**
     * Pop up toast, no icon, no title, you can set the display time
     * @param message
     * @param duration
     */
    public static void showToast(@StringRes int message, int duration) {
        showCenterToast(0, message, DEFAULT, duration);
    }

    /**
     * Display in the middle of the screen
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    public static void showCenterToast(String title, String message, int type, int duration) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.CENTER);
    }

    /**
     * Display in the middle of the screen
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    public static void showCenterToast(@StringRes int title, @StringRes int message, int type, int duration) {
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.CENTER);
    }

    /**
     * Show at the bottom of the screen
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    public static void showBottomToast(String title, String message, int type, int duration) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.BOTTOM);
    }

    /**
     * Show at the bottom of the screen
     * @param title
     * @param message
     * @param type
     * @param duration
     */
    public static void showBottomToast(@StringRes int title, @StringRes int message, int type, int duration) {
        showToast(DemoApplication.getInstance(), title, message, type, duration, Gravity.BOTTOM);
    }

    /**
     * Judging that the toast is not empty, choose cancel, because the toast is different in type (whether to display a picture) or whether it has a title, which will lead to different toast displays
     * @param context
     * @param title
     * @param message
     * @param type
     * @param duration
     * @param gravity
     */
    public static void showToast(Context context, @StringRes int title, @StringRes int message, int type, int duration, int gravity) {
        showToast(context, title == 0 ? null:context.getString(title), context.getString(message), type, duration, gravity);
    }

    /**
     * Judging that the toast is not empty, choose cancel, because the toast is different in type (whether to display a picture) or whether it has a title, which will lead to different toast displays
     * @param context
     * @param title
     * @param message
     * @param type
     * @param duration
     * @param gravity
     */
    public static void showToast(Context context, String title, String message, int type, int duration, int gravity) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        //保证在主线程中展示toast
        EaseThreadManager.getInstance().runOnMainThread(() -> {
            if(toast != null) {
                toast.cancel();
            }
            toast = getToast(context, title, message, type, duration, gravity);
            toast.show();
        });

    }

    private static Toast getToast(Context context, String title, String message, int type, int duration, int gravity) {
        Toast toast = new Toast(context);
        View toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        toast.setView(toastView);
        ImageView ivToast = toastView.findViewById(R.id.iv_toast);
        TextView tvToastTitle = toastView.findViewById(R.id.tv_toast_title);
        TextView tvToastContent = toastView.findViewById(R.id.tv_toast_content);
        if(TextUtils.isEmpty(title)) {
            tvToastTitle.setVisibility(View.GONE);
        }else {
            tvToastTitle.setVisibility(View.VISIBLE);
            tvToastTitle.setText(title);
        }

        if(!TextUtils.isEmpty(message)) {
            tvToastContent.setText(message);
        }

        ivToast.setVisibility(View.GONE);
        if(type == SUCCESS) {
            //ivToast.setImageResource(R.drawable.em_toast_success);
        }else if(type == FAIL) {
            //ivToast.setImageResource(R.drawable.em_toast_fail);
        }else {
            ivToast.setVisibility(View.GONE);
        }
        int yOffset = 0;
        if(gravity == Gravity.BOTTOM || gravity == Gravity.TOP) {
            yOffset = (int) EaseUtils.dip2px(context, 50);
        }
        toast.setDuration(duration);
        toast.setGravity(gravity, 0, yOffset);
        hookToast(toast);
        return toast;
    }

    /**
     * fix 7.1.x version toast crash
     * @param toast
     */
    private static void hookToast(Toast toast) {
        Class<Toast> cToast = Toast.class;
        try {
            //private TN
            Field fTn = cToast.getDeclaredField("mTN");
            fTn.setAccessible(true);

            //get TN
            Object oTn = fTn.get(toast);
            //get TN class or Field.getType()
            Class<?> cTn = oTn.getClass();
            Field fHandle = cTn.getDeclaredField("mHandler");

            //reset->mHandler
            fHandle.setAccessible(true);
            fHandle.set(oTn, new HandlerProxy((Handler) fHandle.get(oTn)));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static class HandlerProxy extends Handler {

        private Handler mHandler;

        public HandlerProxy(Handler handler) {
            this.mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                mHandler.handleMessage(msg);
            } catch (WindowManager.BadTokenException e) {
                //ignore
            }
        }
    }


}
