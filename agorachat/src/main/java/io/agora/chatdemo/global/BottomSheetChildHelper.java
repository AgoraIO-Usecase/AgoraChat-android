package io.agora.chatdemo.global;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.R;

public interface BottomSheetChildHelper {

    //more details by user choose
    default @StringRes
    int getTitlebarTitle() {
        return R.string.group_create_title;
    }

    default @StringRes
    int getTitleBarRightText() {
        return R.string.cancel;
    }

    default @ColorRes
    int getTitlebarRightTextColor() {
        return R.color.group_blue_154dfe;
    }

    default boolean onTitlebarRightTextViewClick() {
        return false;
    }

    default boolean isShowTitlebarLeftLayout() {
        return false;
    }

    Fragment getParentFragment();

    default void startFragment(Fragment fragment, String tag) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).startFragment(fragment, tag);
        }
    }
    default void  hide(){
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).hide();
        }
    }
}
