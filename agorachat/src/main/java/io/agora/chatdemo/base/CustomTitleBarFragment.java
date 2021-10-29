package io.agora.chatdemo.base;

import android.widget.TextView;

/**
 * Created by 许成谱 on 2021/10/29 0029 19:39.
 * qq:1550540124
 * 你有什么不开心的事，说出来让我开心一下
 */
public interface CustomTitleBarFragment {
    default void initTitle(TextView titleView) {}
    default void initRightText(TextView rightTextView){}

    /**
     * 若重写点击事件，返回true即可
     * @return
     */
    default boolean onRightTextViewClick(){return false;}
}
