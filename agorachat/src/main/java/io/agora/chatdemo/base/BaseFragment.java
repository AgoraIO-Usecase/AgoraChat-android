package io.agora.chatdemo.base;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.DialogCallBack;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.general.utils.ToastUtils;


public class BaseFragment extends Fragment {
    public BaseActivity mContext;
    protected boolean isVisiableToUser;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (BaseActivity) context;
    }

    /**
     * 通过id获取当前view控件，需要在onViewCreated()之后的生命周期调用
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T findViewById(@IdRes int id) {
        return requireView().findViewById(id);
    }

    /**
     * hide keyboard
     */
    protected void hideKeyboard() {
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputManager == null) {
                    return;
                }
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 判断当前activity是否可用
     * @return
     */
    public boolean isActivityDisable() {
        return mContext == null || mContext.isFinishing();
    }


    /**
     * 切换到UI线程
     * @param runnable
     */
    public void runOnUiThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    public void setVisiableToUser(boolean isVisiableToUser){
        this.isVisiableToUser =isVisiableToUser;
    }

    public boolean getVisiableToUser(){
        return isVisiableToUser;
    }


    /**
     * 设置返回按钮的颜色
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

    public void showDialog(@StringRes int message, DialogCallBack callBack) {
        showDialog(getResources().getString(R.string.dialog_default_title), getResources().getString(message), callBack);
    }

    public void showDialog(String message, DialogCallBack callBack) {
        showDialog(getResources().getString(R.string.dialog_default_title), message, callBack);
    }

    public void showDialog(@StringRes int title, @StringRes int message, DialogCallBack callBack) {
        showDialog(getResources().getString(title), getResources().getString(message), callBack);
    }

    public void showDialog(String title, String message, DialogCallBack callBack) {
        new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(callBack != null) {
                                callBack.onClick(dialog, which);
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
    }

    /**
     * 解析Resource<T>
     * @param response
     * @param callback
     * @param <T>
     */
    public <T> void parseResource(Resource<T> response, @NonNull OnResourceParseCallback<T> callback) {
        if(mContext != null) {
            mContext.parseResource(response, callback);
        }
    }

    public void showLoading() {
        if(mContext != null) {
            mContext.showLoading();
        }
    }

    public void showLoading(String message) {
        if(mContext != null) {
            mContext.showLoading(message);
        }
    }

    public void dismissLoading() {
        if(mContext != null) {
            mContext.dismissLoading();
        }
    }
}
