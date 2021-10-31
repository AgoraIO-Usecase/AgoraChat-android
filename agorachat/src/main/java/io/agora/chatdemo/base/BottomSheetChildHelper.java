package io.agora.chatdemo.base;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.R;

/**
 * Created by 许成谱 on 2021/10/29 0029 19:39.
 * qq:1550540124
 */
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

    Fragment getBottomSheetContainerFragment();

    default void startFrament(Fragment fragment, String tag) {
        Fragment parentFragment = getBottomSheetContainerFragment();
        if (parentFragment != null && parentFragment instanceof BottomSheetContainerHelper) {
            ((BottomSheetContainerHelper) parentFragment).startFragment(fragment, tag);
        }
    }
}
