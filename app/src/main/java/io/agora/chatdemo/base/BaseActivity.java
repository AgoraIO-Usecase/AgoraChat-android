package io.agora.chatdemo.base;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.chat.uikit.utils.StatusBarCompat;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.UserActivityLifecycleCallbacks;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.general.widget.EaseProgressDialog;
import io.agora.chatdemo.sign.SignInActivity;
import io.agora.util.EMLog;

/**
 * As a basic activity, place some public methods
 */
public class BaseActivity extends AppCompatActivity {
    public BaseActivity mContext;
    private EaseProgressDialog dialog;
    private AlertDialog logoutDialog;
    //Dialog generation time, used to determine the display time of the dialog
    private long dialogCreateTime;
    // Used for the dialog delay to disappear
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        clearFragmentsBeforeCreate();
        registerAccountObservable();
    }

    /**
     * Add account exception monitoring
     */
    protected void registerAccountObservable() {
        LiveDataBus.get().with(DemoConstant.ACCOUNT_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(!event.isAccountChange()) {
                return;
            }
            int errorCode = 0;
            try {
                errorCode = Integer.parseInt(event.event);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if(errorCode == 0) {
                return;
            }
            String accountEvent = event.event;
            if(errorCode == Error.USER_REMOVED
                    || errorCode == Error.USER_KICKED_BY_CHANGE_PASSWORD
                    || errorCode == Error.TOKEN_EXPIRED
                    || errorCode == Error.USER_KICKED_BY_OTHER_DEVICE) {
                DemoHelper.getInstance().logout(false, new CallBack() {
                    @Override
                    public void onSuccess() {
                        finishOtherActivities();
                        startActivity(new Intent(mContext, SignInActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(int code, String error) {
                        EMLog.e("logout", "logout error: error code = "+code + " error message = "+error);
                        showToast("logout error: error code = "+code + " error message = "+error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }else if(errorCode == Error.USER_LOGIN_ANOTHER_DEVICE 
                    || errorCode == Error.SERVER_SERVICE_RESTRICTED) {
                DemoHelper.getInstance().logout(false, null);
                showExceptionDialog(accountEvent);
            }
        });
    }

    private void showExceptionDialog(String accountEvent) {
        if(logoutDialog != null && logoutDialog.isShowing() && !mContext.isFinishing()) {
            logoutDialog.dismiss();
        }
        logoutDialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.account_logoff_notification)
                .setMessage(getExceptionMessageId(accountEvent))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishOtherActivities();
                        startActivity(new Intent(mContext, SignInActivity.class));
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        logoutDialog.show();
    }

    private int getExceptionMessageId(String exceptionType) {
        if(exceptionType.equals(DemoConstant.ACCOUNT_CONFLICT)) {
            return R.string.account_connect_conflict;
        } else if (exceptionType.equals(DemoConstant.ACCOUNT_REMOVED)) {
            return R.string.account_user_remove;
        } else if (exceptionType.equals(DemoConstant.ACCOUNT_FORBIDDEN)) {
            return R.string.account_user_forbidden;
        }
        return R.string.Network_error;
    }

    /**
     * Finish other activities except the current Activity
     */
    protected void finishOtherActivities() {
        UserActivityLifecycleCallbacks lifecycleCallbacks = DemoApplication.getInstance().getLifecycleCallbacks();
        if(lifecycleCallbacks == null) {
            finish();
            return;
        }
        List<Activity> activities = lifecycleCallbacks.getActivityList();
        if(activities == null || activities.isEmpty()) {
            finish();
            return;
        }
        for(Activity activity : activities) {
            if(activity != lifecycleCallbacks.current()) {
                activity.finish();
            }
        }
    }


    /**
     * Initialize toolbar
     * @param toolbar
     */
    public void initToolBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);//not show title
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    /**
     * Set the color of the return icon
     * @param mContext
     * @param colorId
     */
    public static void setToolbarCustomColor(AppCompatActivity mContext, int colorId) {
        Drawable leftArrow = ContextCompat.getDrawable(mContext, R.drawable.abc_ic_ab_back_material);
        if(leftArrow != null) {
            leftArrow.setColorFilter(ContextCompat.getColor(mContext, colorId), PorterDuff.Mode.SRC_ATOP);
            if(mContext.getSupportActionBar() != null) {
                mContext.getSupportActionBar().setHomeAsUpIndicator(leftArrow);
            }
        }
    }

    @Override
    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null&&getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null){
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                super.onBackPressed();
            }else {
                super.onBackPressed();
            }
        }else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissLoading();
    }

    /**
     * hide keyboard
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null&&getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null){
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * toast by string
     * @param message
     */
    public void showToast(String message) {
        ToastUtils.showToast(message);
    }

    /**
     * toast by string res
     * @param messageId
     */
    public void showToast(@StringRes int messageId) {
        ToastUtils.showToast(messageId);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){

        if(null != this.getCurrentFocus()){
            /**
             * Click on the blank position to hide the soft keyboard
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }

        return super.onTouchEvent(event);
    }


    /**
     * General page settings
     */
    public void setFitSystemForTheme() {
        setFitSystemForTheme(true, R.color.white);
        setStatusBarTextColor(true);
    }

    /**
     * General page settings
     * @param fitSystemForTheme
     */
    public void setFitSystemForTheme(boolean fitSystemForTheme) {
        setFitSystemForTheme(fitSystemForTheme, "#FeFFFFFF");
        setStatusBarTextColor(false);
    }

    /**
     * General page settings
     * @param fitSystemForTheme
     */
    public void setFitSystemForTheme2(boolean fitSystemForTheme) {
        setFitSystemForTheme(fitSystemForTheme, "#ffffffff");
        setStatusBarTextColor(true);
    }

    /**
     * Set whether it is immersive, and set the status bar color
     * @param fitSystemForTheme
     * @param colorId Color resource id
     */
    public void setFitSystemForTheme(boolean fitSystemForTheme, @ColorRes int colorId) {
        setFitSystem(fitSystemForTheme);
        StatusBarCompat.compat(this, ContextCompat.getColor(mContext, colorId));
    }

    /**
     * Modify the text color of the status bar
     * @param isLight Is it a light font
     */
    public void setStatusBarTextColor(boolean isLight) {
        StatusBarCompat.setLightStatusBar(mContext, !isLight);
    }


    /**
     * Set whether it is immersive, and set the status bar color
     * @param fitSystemForTheme true is not immersive
     * @param color Status bar color
     */
    public void setFitSystemForTheme(boolean fitSystemForTheme, String color) {
        setFitSystem(fitSystemForTheme);
        StatusBarCompat.compat(mContext, Color.parseColor(color));
    }

    /**
     * Whether the setting is immersive
     * @param fitSystemForTheme
     */
    public void setFitSystem(boolean fitSystemForTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if(fitSystemForTheme) {
            ViewGroup contentFrameLayout = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
            View parentView = contentFrameLayout.getChildAt(0);
            if (parentView != null && Build.VERSION.SDK_INT >= 14) {
                parentView.setFitsSystemWindows(true);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }

    /**
     * Parse Resource<T>
     * @param response
     * @param callback
     * @param <T>
     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(response == null) {
            return;
        }
        if(response.status == Status.SUCCESS) {
            callback.onHideLoading();
            callback.onSuccess(response.data);
        }else if(response.status == Status.ERROR) {
            callback.onHideLoading();
            if(!callback.hideErrorMsg) {
                showToast(response.getMessage());
            }
            callback.onError(response.errorCode, response.getMessage());
        }else if(response.status == Status.LOADING) {
            callback.onLoading(response.data);
        }
    }

    public boolean isMessageChange(String message) {
        if(TextUtils.isEmpty(message)) {
            return false;
        }
        if(message.contains("message")) {
            return true;
        }
        return false;
    }

    public boolean isContactChange(String message) {
        if(TextUtils.isEmpty(message)) {
            return false;
        }
        if(message.contains("contact")) {
            return true;
        }
        return false;
    }

    public boolean isGroupInviteChange(String message) {
        if(TextUtils.isEmpty(message)) {
            return false;
        }
        if(message.contains("invite")) {
            return true;
        }
        return false;
    }

    public boolean isNotify(String message) {
        if(TextUtils.isEmpty(message)) {
            return false;
        }
        if(message.contains("invite")) {
            return true;
        }
        return false;
    }

    public void showLoading() {
        showLoading(getString(R.string.loading));
    }

    public void showLoading(String message) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if(mContext.isFinishing()) {
            return;
        }
        dialogCreateTime = System.currentTimeMillis();
        dialog = new EaseProgressDialog.Builder(mContext)
                .setLoadingMessage(message)
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .show();
    }

    public void dismissLoading() {
        if(dialog != null && dialog.isShowing()) {
            if(System.currentTimeMillis() - dialogCreateTime < 500 && !isFinishing()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }
                }, 1000);
            }else {
                dialog.dismiss();
                dialog = null;
            }

        }
    }

    /**
     * Deal with the fragment overlay problem caused by Activity reconstruction
     */
    public void clearFragmentsBeforeCreate() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() == 0){
            return;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : fragments) {
            fragmentTransaction.remove(fragment);
        }
        fragmentTransaction.commitNow();
    }
}
