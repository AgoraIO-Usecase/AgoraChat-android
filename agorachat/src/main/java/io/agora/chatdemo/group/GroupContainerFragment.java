package io.agora.chatdemo.group;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Stack;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.base.BottomSheetChildHelper;
import io.agora.chatdemo.base.BottomSheetContainerHelper;
import io.agora.chatdemo.general.utils.CommonUtils;

public class GroupContainerFragment extends BaseBottomSheetFragment implements BottomSheetContainerHelper {

    private BottomSheetChildHelper currentChild;
    protected TextView titlebarRightText;
    private Stack<BottomSheetChildHelper> childStack = new Stack<>();


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
        //对标题的设置下沉到子fragment中
        if (currentChild != null) {
            baseBinding.titlebar.getTitle().setText(currentChild.getTitlebarTitle());
            titlebarRightText.setText(currentChild.getTitleBarRightText());
            titlebarRightText.setTextColor(ContextCompat.getColor(requireContext(), currentChild.getTitlebarRightTextColor()));
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

        titlebarRightText = baseBinding.titlebar.getRightText();
        baseBinding.titlebar.setRightLayoutVisibility(View.VISIBLE);
        titlebarRightText.setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big));
        baseBinding.titlebar.setTitleSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big_18));

        GroupCreateFragment groupCreateFragment = new GroupCreateFragment();
        startFragment(groupCreateFragment, groupCreateFragment.getClass().getSimpleName());

        setTopOffset(300);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titlebarRightText.setOnClickListener(v -> {
                    if (!currentChild.onTitlebarRightTextViewClick()) {
                        hide();
                    }
                }
        );
        baseBinding.titlebar.setOnBackPressListener(v -> {
            if (getChildFragmentManager().getBackStackEntryCount() > 1) {
                getChildFragmentManager().popBackStack();
                childStack.pop();
                currentChild = childStack.peek();
                initTileBar();
            } else {
                hide();
            }
        });
    }
}
