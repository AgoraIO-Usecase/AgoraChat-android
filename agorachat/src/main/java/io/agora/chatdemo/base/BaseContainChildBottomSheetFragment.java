package io.agora.chatdemo.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Stack;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.FragmentContainTitleBaseBinding;
import io.agora.chatdemo.general.utils.CommonUtils;

/**
 * Base bottom fragment which has child fragment and title
 */
public abstract class BaseContainChildBottomSheetFragment extends BaseBottomSheetFragment implements BottomSheetContainerHelper{
    protected FragmentContainTitleBaseBinding baseBinding;
    protected BottomSheetChildHelper currentChild;
    protected Stack<BottomSheetChildHelper> childStack = new Stack<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        baseBinding = FragmentContainTitleBaseBinding.inflate(inflater);
        return baseBinding.getRoot();
    }

    @Override
    public void startFragment(@NonNull Fragment fragment, @Nullable String tag) {
        if (!(fragment instanceof BottomSheetChildHelper)) {
            throw new IllegalArgumentException("only ButtomSheetChildFragment can be started here ");
        }
        if (TextUtils.isEmpty(tag)) {
            tag = fragment.getClass().getSimpleName();
        }
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, fragment, tag)
                .addToBackStack(null)
                .commit();
        childStack.add((BottomSheetChildHelper) fragment);
        currentChild = (BottomSheetChildHelper) fragment;
        initTileBar();
    }

    private void initTileBar() {
        if(!showTitle()) {
            return;
        }
        if (currentChild != null) {
            baseBinding.titlebar.getTitle().setText(currentChild.getTitlebarTitle());
            baseBinding.titlebar.getRightText().setText(currentChild.getTitleBarRightText());
            baseBinding.titlebar.getRightText().setTextColor(ContextCompat.getColor(requireContext(), currentChild.getTitlebarRightTextColor()));
            if (currentChild.isShowTitlebarLeftLayout()) {
                baseBinding.titlebar.setLeftImageResource(R.drawable.titlebar_back);
            } else {
                baseBinding.titlebar.setLeftLayoutVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();
        setTitleBar();
        
        // set child fragment
        Fragment childFragment = getChildFragment();
        startFragment(childFragment, childFragment.getClass().getSimpleName());
    }

    protected void setTitleBar() {
        if(showTitle()) {
            baseBinding.titlebar.setRightLayoutVisibility(View.VISIBLE);
            baseBinding.titlebar.getRightText().setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big));
            baseBinding.titlebar.getTitle().setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big_18));
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        baseBinding.titlebar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                if (getChildFragmentManager().getBackStackEntryCount() > 1) {
                    getChildFragmentManager().popBackStack();
                    childStack.pop();
                    currentChild = childStack.peek();
                    initTileBar();
                } else {
                    hide();
                }
            }
        });
    }

    /**
     * Provider child fragment, should not be null.
     * @return
     */
    protected abstract @NonNull Fragment getChildFragment();

    /**
     * Whether to show titleBar
     * @return
     */
    protected boolean showTitle() {
        return true;
    }
}
