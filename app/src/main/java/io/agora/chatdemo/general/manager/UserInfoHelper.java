package io.agora.chatdemo.general.manager;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chatdemo.DemoHelper;

public class UserInfoHelper {

    public static void setUserInfo(Context context, String username, @DrawableRes int defaultAvatar, TextView tvName, ImageView avatar) {
        String name = username;
        String userAvatar= "";
        EaseUserProfileProvider userProvider = EaseUIKit.getInstance().getUserProvider();
        if(userProvider != null) {
            EaseUser user = userProvider.getUser(username);
            if(user != null) {
                if(!TextUtils.isEmpty(user.getNickname())) {
                    name = user.getNickname();
                }
                userAvatar = user.getAvatar();
            }
        }
        if(tvName != null && !TextUtils.isEmpty(name)) {
            tvName.setText(name);
        }
        if(avatar != null) {
            Glide.with(context)
                    .load(userAvatar)
                    .placeholder(defaultAvatar)
                    .error(defaultAvatar)
                    .into(avatar);
        }
    }

    public static EaseUser getUserInfo(String username) {
        return DemoHelper.getInstance().getUserInfo(username);
    }
    
}
