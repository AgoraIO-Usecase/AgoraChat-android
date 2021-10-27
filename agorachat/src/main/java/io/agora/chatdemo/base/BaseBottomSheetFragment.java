package io.agora.chatdemo.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.agora.chatdemo.R;

/**
 * 底部弹出fragment基类，封装弹出、隐藏逻辑
 */
public class BaseBottomSheetFragment extends BottomSheetDialogFragment {
    /**
     * 顶部向下偏移量
     */
    private int topOffset = 0;
    private BottomSheetBehavior mBehavior;
    private View dialogView;

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
        dialogView = inflater.inflate(R.layout.fragment_group_base, container, false);
        return dialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (dialogView != null) {
            getDialog().setCanceledOnTouchOutside(true);
            ViewGroup.LayoutParams layoutParams = dialogView.getLayoutParams();
            layoutParams.height = getHeight();
            mBehavior = BottomSheetBehavior.from((View) dialogView.getParent());
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    /**
     * 获取屏幕高度
     */
    private int getHeight() {
        return getResources().getDisplayMetrics().heightPixels - getTopOffset();
    }

    public int getTopOffset() {
        return topOffset;
    }

    public void setTopOffset(int topOffset) {
        this.topOffset = topOffset;
    }

    public BottomSheetBehavior<FrameLayout> getBehavior() {
        return mBehavior;
    }

    public void hide() {
        if (mBehavior != null) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}