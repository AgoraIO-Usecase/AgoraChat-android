package io.agora.chatdemo.main;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Stack;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.base.BottomSheetChildHelper;
import io.agora.chatdemo.base.BottomSheetContainerHelper;
import io.agora.chatdemo.general.utils.CommonUtils;
import io.agora.chatdemo.group.GroupCreateFragment;

public class BottomSheetContainerFragment extends BaseBottomSheetFragment implements BottomSheetContainerHelper {

    private BottomSheetChildHelper currentChild;
    protected TextView titlebarRightText;
    private Stack<BottomSheetChildHelper> childStack = new Stack<>();
    private EaseTitleBar titlebar;

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
            titlebar.getTitle().setText(currentChild.getTitlebarTitle());
            titlebarRightText.setText(currentChild.getTitleBarRightText());
            titlebarRightText.setTextColor(ContextCompat.getColor(requireContext(), currentChild.getTitlebarRightTextColor()));
            if (currentChild.isShowTitlebarLeftLayout()) {
                titlebar.setLeftImageResource(R.drawable.titlebar_back);
            } else {
                titlebar.setLeftLayoutVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();

        titlebar= findViewById(R.id.titlebar);

        titlebarRightText = titlebar.getRightText();
        titlebar.setRightLayoutVisibility(View.VISIBLE);
        titlebarRightText.setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big));
        titlebar.getTitle().setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big_18));

        GroupCreateFragment groupCreateFragment = new GroupCreateFragment();
        startFragment(groupCreateFragment, groupCreateFragment.getClass().getSimpleName());

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_bottom_sheet_group_container;
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
        titlebar.setOnBackPressListener(v -> {
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
