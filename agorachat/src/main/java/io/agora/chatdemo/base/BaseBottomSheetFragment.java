package io.agora.chatdemo.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.net.Resource;

public class BaseBottomSheetFragment extends BottomSheetDialogFragment {
    private int topOffset;
    private BottomSheetBehavior mBehavior;
    public BaseActivity mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (BaseActivity) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog;
        dialog = new BottomSheetDialog(requireContext(), R.style.transparentBottomSheetStyle);
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initArgument();
        initView();
        initListener();
        initData();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(getCanceledOnTouchOutside());
        }
        ViewGroup.LayoutParams layoutParams = requireView().getLayoutParams();
        layoutParams.height = getHeight();
        mBehavior = BottomSheetBehavior.from((View) requireView().getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return requireView().findViewById(id);
    }

    /**
     * Get fragment's height
     */
    private int getHeight() {
        return getResources().getDisplayMetrics().heightPixels - getTopOffset();
    }

    protected int getTopOffset() {
        return topOffset;
    }

    protected void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    protected BottomSheetBehavior<FrameLayout> getBehavior() {
        return mBehavior;
    }

    public void hide() {
        if (mBehavior != null) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    protected void initArgument() {
    }

    protected void initData() {
    }

    protected void initListener() {
    }

    protected void initView() {
        // Set default top offset
        topOffset = (int) EaseUtils.dip2px(requireActivity(), 56);
    }

    protected boolean getCanceledOnTouchOutside() {
        return true;
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