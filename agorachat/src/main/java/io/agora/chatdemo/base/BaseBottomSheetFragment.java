package io.agora.chatdemo.base;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.FragmentGroupBaseBinding;
import io.agora.chatdemo.general.utils.CommonUtils;

/**
 * 底部弹出fragment基类，封装弹出、隐藏逻辑
 */
public abstract class BaseBottomSheetFragment extends BottomSheetDialogFragment  {
    /**
     * 顶部向下偏移量
     */
    private int topOffset = 0;
    private BottomSheetBehavior mBehavior;
    protected FragmentGroupBaseBinding baseBinding;
    protected TextView titlebarRightText;

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
        baseBinding = FragmentGroupBaseBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        return baseBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        titlebarRightText.setText(R.string.cancel);
        titlebarRightText.setTextColor(ContextCompat.getColor(requireContext(), R.color.group_blue_154dfe));
        baseBinding.titlebar.setTitle(getTitle());
    }

    /**
     * 设置标题时子类重写这个方法
     * @return
     */
    protected abstract String getTitle();

    private void initListener() {
        titlebarRightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    private void initView() {
        titlebarRightText = baseBinding.titlebar.getRightText();
        baseBinding.titlebar.setRightLayoutVisibility(View.VISIBLE);
        baseBinding.titlebar.setLeftLayoutVisibility(View.GONE);
        titlebarRightText.setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big));
        baseBinding.titlebar.setTitleSize(CommonUtils.getSpDimen(requireContext(),R.dimen.text_size_big_18));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (baseBinding.getRoot() != null) {
            getDialog().setCanceledOnTouchOutside(true);
            ViewGroup.LayoutParams layoutParams = baseBinding.getRoot().getLayoutParams();
            layoutParams.height = getHeight();
            mBehavior = BottomSheetBehavior.from((View) baseBinding.getRoot().getParent());
            mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
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

    protected void hide() {
        if (mBehavior != null) {
            mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}