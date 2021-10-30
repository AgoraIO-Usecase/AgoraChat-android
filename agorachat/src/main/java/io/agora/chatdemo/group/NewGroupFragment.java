package io.agora.chatdemo.group;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.base.BottomSheetChildFragment;

/**
 * Created by 许成谱 on 2021/10/29 0029 20:16.
 * qq:1550540124
 */
public class NewGroupFragment extends BaseInitFragment implements BottomSheetChildFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_group;
    }
    @Override
    public int getTitlebarTitle() {
        return R.string.group_new_group;
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.group_next;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.group_blue_154dfe;
    }

    @Override
    public boolean onTitlebarRightTextViewClick() {
        return false;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }
}
