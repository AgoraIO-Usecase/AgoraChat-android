package io.agora.chatdemo.group;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.Group;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.models.EaseGroupInfo;
import io.agora.chat.uikit.provider.EaseGroupInfoProvider;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;

public class GroupHelper {

    /**
     * 是否是群主
     * @return
     */
    public static boolean isOwner(Group group) {
        if(group == null || 
                TextUtils.isEmpty(group.getOwner())) {
            return false;
        }
        return TextUtils.equals(group.getOwner(), DemoHelper.getInstance().getCurrentUser());
    }

    /**
     * 是否是聊天室创建者
     * @return
     */
    public static boolean isOwner(ChatRoom room) {
        if(room == null ||
                TextUtils.isEmpty(room.getOwner())) {
            return false;
        }
        return TextUtils.equals(room.getOwner(), DemoHelper.getInstance().getCurrentUser());
    }

    /**
     * 是否是管理员
     * @return
     */
    public synchronized static boolean isAdmin(Group group) {
        List<String> adminList = group.getAdminList();
        if(adminList != null && !adminList.isEmpty()) {
            return adminList.contains(DemoHelper.getInstance().getCurrentUser());
        }
        return false;
    }

    /**
     * 是否是管理员
     * @return
     */
    public synchronized static boolean isAdmin(ChatRoom group) {
        List<String> adminList = group.getAdminList();
        if(adminList != null && !adminList.isEmpty()) {
            return adminList.contains(DemoHelper.getInstance().getCurrentUser());
        }
        return false;
    }

    /**
     * 是否有邀请权限
     * @return
     */
    public static boolean isCanInvite(Group group) {
        return group != null && (group.isMemberAllowToInvite() || isOwner(group) || isAdmin(group));
    }

    /**
     * 在黑名单中
     * @param username
     * @return
     */
    public static boolean isInAdminList(String username, List<String> adminList) {
        return isInList(username, adminList);
    }

    /**
     * 在黑名单中
     * @param username
     * @return
     */
    public static boolean isInBlackList(String username, List<String> blackMembers) {
        return isInList(username, blackMembers);
    }

    /**
     * 在禁言名单中
     * @param username
     * @return
     */
    public static boolean isInMuteList(String username, List<String> muteMembers) {
        return isInList(username, muteMembers);
    }

    /**
     * 是否在列表中
     * @param name
     * @return
     */
    public static boolean isInList(String name, List<String> list) {
        if(list == null) {
            return false;
        }
        synchronized (GroupHelper.class) {
            for (String item : list) {
                if (TextUtils.equals(name, item)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取群名称
     * @param groupId
     * @return
     */
    public static String getGroupName(String groupId) {
        Group group = ChatClient.getInstance().groupManager().getGroup(groupId);
        if(group == null) {
            return groupId;
        }
        return TextUtils.isEmpty(group.getGroupName()) ? groupId : group.getGroupName();
    }

    /**
     * 判断是否加入了群组
     * @param allJoinGroups 所有加入的群组
     * @param groupId
     * @return
     */
    public static boolean isJoinedGroup(List<Group> allJoinGroups, String groupId) {
        if(allJoinGroups == null || allJoinGroups.isEmpty()) {
            return false;
        }
        for (Group group : allJoinGroups) {
            if(TextUtils.equals(group.getGroupId(), groupId)) {
                return true;
            }
        }
        return false;
    }

    public static boolean setGroupInfo(Context context, String groupId, @DrawableRes int defaultAvatar, TextView tvName, ImageView avatar) {
        String name = groupId;
        String userAvatar= "";
        boolean isProvide = false;
        EaseGroupInfoProvider userProvider = EaseUIKit.getInstance().getGroupInfoProvider();
        if(userProvider != null) {
            EaseGroupInfo info = userProvider.getGroupInfo(groupId, 1);
            if(info != null) {
                if(!TextUtils.isEmpty(info.getName())) {
                    name = info.getName();
                }
                userAvatar = info.getIconUrl();
                EaseGroupInfo.AvatarSettings settings = info.getAvatarSettings();
                if(settings != null && avatar != null && avatar instanceof EaseImageView) {
                    if(settings.getAvatarShapeType() != 0)
                        ((EaseImageView)avatar).setShapeType(settings.getAvatarShapeType());
                    if(settings.getAvatarBorderWidth() != 0)
                        ((EaseImageView)avatar).setBorderWidth(settings.getAvatarBorderWidth());
                    if(settings.getAvatarBorderColor() != 0)
                        ((EaseImageView)avatar).setBorderColor(settings.getAvatarBorderColor());
                    if(settings.getAvatarRadius() != 0)
                        ((EaseImageView)avatar).setRadius(settings.getAvatarRadius());
                }
                if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(userAvatar)) {
                    isProvide = true;
                }
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
        return isProvide;
    }
}
