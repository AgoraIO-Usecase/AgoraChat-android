package io.agora.chatdemo.group;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.base.BottomSheetChildHelper;

/**
 * Created by 许成谱 on 2021/10/30 0030 19:57
 * qq:1550540124
 * 热爱生活每一天
 */
public class NewGroupSettingFragment extends BaseInitFragment implements BottomSheetChildHelper {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_group_setting;
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
