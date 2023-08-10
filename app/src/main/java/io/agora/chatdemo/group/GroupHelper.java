package io.agora.chatdemo.group;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatRoom;
import io.agora.chat.Group;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.models.EaseGroupInfo;
import io.agora.chat.uikit.provider.EaseGroupInfoProvider;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.group.model.MemberAttributeBean;

public class GroupHelper {

    private static final Map<String,Map<String,MemberAttributeBean>> groupMemberAttribute = new HashMap<>();
    private static final Map<String,MemberAttributeBean> attributeMap = new HashMap<>();

    /**
     * Whether is group owner
     * @return
     */
    public static boolean isOwner(Group group) {
        if(group == null || 
                TextUtils.isEmpty(group.getOwner())) {
            return false;
        }
        return TextUtils.equals(group.getOwner(), DemoHelper.getInstance().getUsersManager().getCurrentUserID());
    }

    /**
     * Whether is group owner
     * @return
     */
    public static boolean isOwner(Group group, String username) {
        if(group == null ||
                TextUtils.isEmpty(group.getOwner()) || TextUtils.isEmpty(username)) {
            return false;
        }
        return TextUtils.equals(group.getOwner(), username);
    }

    /**
     * Whether is chatRoom owner
     * @return
     */
    public static boolean isOwner(ChatRoom room) {
        if(room == null ||
                TextUtils.isEmpty(room.getOwner())) {
            return false;
        }
        return TextUtils.equals(room.getOwner(), DemoHelper.getInstance().getUsersManager().getCurrentUserID());
    }

    /**
     * Whether is chatRoom owner
     * @return
     */
    public synchronized static boolean isAdmin(Group group) {
        if (null == group) {
            return false;
        }
        List<String> adminList = group.getAdminList();
        if(adminList != null && !adminList.isEmpty()) {
            return adminList.contains(DemoHelper.getInstance().getUsersManager().getCurrentUserID());
        }
        return false;
    }

    /**
     * Whether is admin
     * @return
     */
    public synchronized static boolean isAdmin(ChatRoom group) {
        List<String> adminList = group.getAdminList();
        if(adminList != null && !adminList.isEmpty()) {
            return adminList.contains(DemoHelper.getInstance().getUsersManager().getCurrentUserID());
        }
        return false;
    }

    /**
     * Whether have invitation permission
     * @return
     */
    public static boolean isCanInvite(Group group) {
        return group != null && (group.isMemberAllowToInvite() || isOwner(group) || isAdmin(group));
    }

    /**
     * in blacklist
     * @param username
     * @return
     */
    public static boolean isInAdminList(String username, List<String> adminList) {
        return isInList(username, adminList);
    }

    /**
     * Whether in blacklist
     * @param username
     * @return
     */
    public static boolean isInBlackList(String username, List<String> blackMembers) {
        return isInList(username, blackMembers);
    }

    /**
     * Whether in muteList
     * @param username
     * @return
     */
    public static boolean isInMuteList(String username, List<String> muteMembers) {
        return isInList(username, muteMembers);
    }

    /**
     * Whether in muteList
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
     * get GroupName
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
     * Whether joined group
     * @param allJoinGroups All joined groups
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

    /**
     * Stores group member properties
     * @param groupId
     * @param userName
     * @param bean
     */
    public static void saveMemberAttribute(String groupId,String userName,MemberAttributeBean bean){
        attributeMap.put(userName,bean);
        groupMemberAttribute.put(groupId,attributeMap);
    }

    /**
     * Gets group membership properties
     * @param groupId
     * @param userId
     * @return
     */
    public static MemberAttributeBean getMemberAttribute(String groupId,String userId){
        MemberAttributeBean attributeBean = null;
        if (!TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(userId)){
            if (groupMemberAttribute.containsKey(groupId)){
                Map<String,MemberAttributeBean> map = groupMemberAttribute.get(groupId);
                if (map != null ){
                    if (map.containsKey(userId)){
                        attributeBean = map.get(userId);
                    }
                }
            }
        }
        return attributeBean;
    }

    /**
     * Remove the specified group member property to exit a group yourself
     * @param groupId
     */
    public static void clearGroupMemberAttribute(String groupId){
        groupMemberAttribute.remove(groupId);
        attributeMap.clear();
    }

    /**
     * Remove all group member properties to log out
     */
    public static void clearAllGroupMemberAttribute(){
        groupMemberAttribute.clear();
        attributeMap.clear();
    }

    /**
     * Remove a Specified group The property of a specified member is used when a member is kicked out of the group or when a member leaves the group
     * @param groupId
     * @param userId
     */
    public static void clearGroupMemberAttributeByUserId(String groupId,String userId){
        if (groupMemberAttribute.containsKey(groupId)){
            Map<String, MemberAttributeBean> map = groupMemberAttribute.get(groupId);
            if (map != null){
                map.remove(userId);
                attributeMap.remove(userId);
            }
        }
    }

    public static boolean setGroupInfo(Context context, String groupId, @DrawableRes int defaultAvatar, TextView tvName, ImageView avatar) {
        String name = groupId;
        boolean isProvide = false;
        EaseGroupInfoProvider userProvider = EaseUIKit.getInstance().getGroupInfoProvider();
        if(userProvider != null) {
            EaseGroupInfo info = userProvider.getGroupInfo(groupId, 1);
            if(info != null) {
                if(!TextUtils.isEmpty(info.getName())) {
                    name = info.getName();
                }
                String iconUrl = info.getIconUrl();
                if(avatar != null) {
                    if(!TextUtils.isEmpty(iconUrl)) {
                        try {
                            int resourceId = Integer.parseInt(iconUrl);
                            Glide.with(context).load(resourceId).error(defaultAvatar).into(avatar);
                        } catch (NumberFormatException e) {
                            Glide.with(context).load(iconUrl).error(defaultAvatar).into(avatar);
                        }
                    }else {
                        Glide.with(context).load(info.getIcon()).error(defaultAvatar).into(avatar);
                    }
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
                }
                if(!TextUtils.isEmpty(info.getName())) {
                    isProvide = true;
                }
            }
        }
        if(tvName != null && !TextUtils.isEmpty(name)) {
            tvName.setText(name);
        }
        return isProvide;
    }
}
