package io.agora.chatdemo.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.general.widget.EaseProgressDialog;

/**
 * Created by wei on 2016/9/27.
 */
public class BaseActivity extends AppCompatActivity {
    //private FirebaseAnalytics mFirebaseAnalytics;
    private Toolbar mActionBarToolbar;
    private EaseProgressDialog dialog;
    private long dialogCreateTime;//To judge the showing time for dialog
    private Handler handler = new Handler();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
        // should be in launcher activity, but all app use this can avoid the problem
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //getActionBarToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //DemoHelper.getInstance().getNotifier().reset();
    }

    /**
     * set overflow icon's color
     * @param toolbar
     * @param colorId
     */
    public static void setToolbarMoreIconCustomColor(Toolbar toolbar, int colorId) {
        if(toolbar == null) {
            return;
        }
        Drawable moreIcon = ContextCompat.getDrawable(toolbar.getContext(), R.drawable.abc_ic_menu_overflow_material);
        if(moreIcon != null) {
            moreIcon.setColorFilter(ContextCompat.getColor(toolbar.getContext(), colorId), PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(moreIcon);
        }
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
     * Uses to parse Resource<T>
     * @param response
     * @param callback
     * @param <T>
     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(response == null) {
            return;
        }
        if(response.status == Status.SUCCESS) {
            callback.hideLoading();
            callback.onSuccess(response.data);
        }else if(response.status == Status.ERROR) {
            callback.hideLoading();
            if(!callback.hideErrorMsg) {
                showToast(response.getMessage());
            }
            callback.onError(response.errorCode, response.getMessage());
        }else if(response.status == Status.LOADING) {
            callback.onLoading(response.data);
        }
    }

    public void showLoading() {
        showLoading(getString(R.string.loading));
    }

    public void showLoading(String message) {
        if(dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        if(this.isFinishing()) {
            return;
        }
        dialogCreateTime = System.currentTimeMillis();
        dialog = new EaseProgressDialog.Builder(this)
                .setLoadingMessage(message)
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .show();
    }

    public void dismissLoading() {
        if(dialog != null && dialog.isShowing()) {
            //如果dialog的展示时间过短，则延迟1s再消失
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

}
