package io.agora.chatdemo.group;

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
import io.agora.chatdemo.base.BottomSheetChildFragment;
import io.agora.chatdemo.base.BottomSheetContainerFragment;
import io.agora.chatdemo.general.utils.CommonUtils;

public class GroupContainerFragment extends BaseBottomSheetFragment implements BottomSheetContainerFragment {

    private BottomSheetChildFragment currentFragment;
    protected TextView titlebarRightText;
    private Stack<BottomSheetChildFragment> fragmentStack = new Stack<>();


    @Override
    public void startFragment(@NonNull Fragment fragment, @Nullable String tag) {
        if (!(fragment instanceof BottomSheetChildFragment)) {
            throw new IllegalArgumentException("only ButtomSheetChildFragment can be started here ");
        }
        if (TextUtils.isEmpty(tag)) {
            tag = fragment.getClass().getSimpleName();
        }
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, fragment,  tag)
                .addToBackStack(null)
                .commit();
        fragmentStack.add((BottomSheetChildFragment) fragment);
        currentFragment = (BottomSheetChildFragment) fragment;
        initTileBar();
    }

    private void initTileBar() {
        //对标题的设置下沉到子fragment中
        if (currentFragment != null) {
            baseBinding.titlebar.getTitle().setText(currentFragment.getTitlebarTitle());
            titlebarRightText.setText(currentFragment.getTitleBarRightText());
            titlebarRightText.setTextColor(ContextCompat.getColor(requireContext(), currentFragment.getTitlebarRightTextColor()));
            if (currentFragment.isShowTitlebarLeftLayout()) {
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
        baseBinding.titlebar.getTitle().setTextSize(R.dimen.text_size_big_18);

        GroupCreateFragment groupCreateFragment = new GroupCreateFragment();
        startFragment(groupCreateFragment, groupCreateFragment.getClass().getSimpleName());

        setTopOffset(300);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titlebarRightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentFragment.onTitlebarRightTextViewClick()) {
                    hide();
                }
            }
        });
        baseBinding.titlebar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                if (getChildFragmentManager().getBackStackEntryCount() > 1) {
                    getChildFragmentManager().popBackStack();
                    fragmentStack.pop();
                    currentFragment = fragmentStack.peek();
                    initTileBar();
                } else {
                    hide();
                }
            }
        });
    }
}
