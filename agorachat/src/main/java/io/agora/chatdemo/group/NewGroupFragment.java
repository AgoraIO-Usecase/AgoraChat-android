package io.agora.chatdemo.group;

import android.widget.TextView;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.base.CustomTitleBarFragment;

/**
 * Created by 许成谱 on 2021/10/29 0029 20:16.
 * qq:1550540124
 * 你有什么不开心的事，说出来让我开心一下
 */
public class NewGroupFragment extends BaseInitFragment implements CustomTitleBarFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_group;
    }

    @Override
    public void initTitle(TextView titleView) {
        titleView.setText(R.string.group_new_group);
    }

    @Override
    public void initRightText(TextView rightTextView) {
        rightTextView.setText(R.string.group_next);
    }

    @Override
    public boolean onRightTextViewClick() {
        return false;
    }
}
