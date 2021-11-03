package io.agora.chatdemo.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.agora.chatdemo.R;

/**
 * 底部弹出fragment基类，封装弹出、隐藏逻辑
 */
public abstract class BaseBottomSheetFragment extends BottomSheetDialogFragment {

    private int topOffset = 300;
    private BottomSheetBehavior mBehavior;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog;
        dialog = new BottomSheetDialog(requireContext(), R.style.transparentBottomSheetStyle);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        initArgument();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initListener();
        initData();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setCanceledOnTouchOutside(getCanceledOnTouchOutside());
        ViewGroup.LayoutParams layoutParams = requireView().getLayoutParams();
        layoutParams.height = getHeight();
        mBehavior = BottomSheetBehavior.from((View) requireView().getParent());
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    protected <T extends View> T findViewById(@IdRes int id) {
        return requireView().findViewById(id);
    }

    /**
     * 获取屏幕高度
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

    protected abstract int getLayoutId();

    protected void initArgument() {
    }

    protected void initData() {
    }

    protected void initListener() {
    }

    protected void initView() {
    }

    protected boolean getCanceledOnTouchOutside() {
        return true;
    }

}