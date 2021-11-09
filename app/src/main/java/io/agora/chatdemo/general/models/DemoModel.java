package io.agora.chatdemo.general.models;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatRoom;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.dao.EmUserDao;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.manager.OptionsHelper;
import io.agora.chatdemo.general.manager.PreferenceManager;

/**
 * DemoModel主要用于SP存取及一些数据库的存取
 */
public class DemoModel {
    EmUserDao dao = null;
    protected Context context = null;
    protected Map<Key,Object> valueCache = new HashMap<Key,Object>();
    public List<ChatRoom> chatRooms;

    //用户属性数据过期时间设置
    public static long userInfoTimeOut =  7 * 24 * 60 * 60 * 1000;
    
    public DemoModel(Context ctx){
        context = ctx;
        PreferenceManager.init(context);
    }

    public long getUserInfoTimeOut() {
        return userInfoTimeOut;
    }

    public void setUserInfoTimeOut(long userInfoTimeOut) {
        if(userInfoTimeOut > 0){
            this.userInfoTimeOut = userInfoTimeOut;
        }
    }


    public boolean updateContactList(List<EaseUser> contactList) {
        List<EmUserEntity> userEntities = EmUserEntity.parseList(contactList);
        EmUserDao dao = DemoDbHelper.getInstance(context).getUserDao();
        if(dao != null) {
            dao.insert(userEntities);
            return true;
        }
        return false;
    }

    public Map<String, EaseUser> getContactList() {
        EmUserDao dao = DemoDbHelper.getInstance(context).getUserDao();
        if(dao == null) {
            return new HashMap<>();
        }
        Map<String, EaseUser> map = new HashMap<>();
        List<EaseUser> users = dao.loadAllContactUsers();
        if(users != null && !users.isEmpty()) {
            for (EaseUser user : users) {
                map.put(user.getUsername(), user);
            }
        }
        return map;
    }


    public Map<String, EaseUser> getAllUserList() {
        EmUserDao dao = DemoDbHelper.getInstance(context).getUserDao();
        if(dao == null) {
            return new HashMap<>();
        }
        Map<String, EaseUser> map = new HashMap<>();
        List<EaseUser> users = dao.loadAllEaseUsers();
        if(users != null && !users.isEmpty()) {
            for (EaseUser user : users) {
                map.put(user.getUsername(), user);
            }
        }
        return map;
    }


    public Map<String, EaseUser> getFriendContactList() {
        EmUserDao dao = DemoDbHelper.getInstance(context).getUserDao();
        if(dao == null) {
            return new HashMap<>();
        }
        Map<String, EaseUser> map = new HashMap<>();
        List<EaseUser> users = dao.loadContacts();
        if(users != null && !users.isEmpty()) {
            for (EaseUser user : users) {
                map.put(user.getUsername(), user);
            }
        }
        return map;
    }

    /**
     * 判断是否是联系人
     * @param userId
     * @return
     */
    public boolean isContact(String userId) {
        Map<String, EaseUser> contactList = getFriendContactList();
        return contactList.keySet().contains(userId);
    }
    
    public void saveContact(EaseUser user){
        EmUserDao dao = DemoDbHelper.getInstance(context).getUserDao();
        if(dao == null) {
            return;
        }
        dao.insert(EmUserEntity.parseParent(user));
    }

    /**
     * get DemoDbHelper
     * @return
     */
    public DemoDbHelper getDbHelper() {
        return DemoDbHelper.getInstance(DemoApplication.getInstance());
    }

    /**
     * 向数据库中插入数据
     * @param object
     */
    public void insert(Object object) {
        DemoDbHelper dbHelper = getDbHelper();
        if(object instanceof EmUserEntity) {
            if(dbHelper.getUserDao() != null) {
                dbHelper.getUserDao().insert((EmUserEntity) object);
            }
        }
    }

    /**
     * update
     * @param object
     */
    public void update(Object object) {
        DemoDbHelper dbHelper = getDbHelper();
        if(object instanceof EmUserEntity) {
            if(dbHelper.getUserDao() != null) {
                dbHelper.getUserDao().insert((EmUserEntity) object);
            }
        }
    }


    /**
     * 查找有关用户用户属性过期的用户ID
     *
     */
    public List<String> selectTimeOutUsers() {
        DemoDbHelper dbHelper = getDbHelper();
        List<String> users = null;
        if(dbHelper.getUserDao() != null) {
            users = dbHelper.getUserDao().loadTimeOutEaseUsers(userInfoTimeOut,System.currentTimeMillis());
        }
        return users;
    }

    /**
     * 保存是否删除联系人的状态
     * @param username
     * @param isDelete
     */
    public void deleteUsername(String username, boolean isDelete) {
        SharedPreferences sp = context.getSharedPreferences("save_delete_username_status", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(username, isDelete);
        edit.commit();
    }

    /**
     * 查看联系人是否删除
     * @param username
     * @return
     */
    public boolean isDeleteUsername(String username) {
        SharedPreferences sp = context.getSharedPreferences("save_delete_username_status", Context.MODE_PRIVATE);
        return sp.getBoolean(username, false);
    }

    /**
     * 设置昵称
     * @param nickname
     */
    public void setCurrentUserNick(String nickname) {
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    public String getCurrentUserNick() {
        return PreferenceManager.getInstance().getCurrentUserNick();
    }

    /**
     * 设置头像
     * @param avatar
     */
    private void setCurrentUserAvatar(String avatar) {
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private String getCurrentUserAvatar() {
        return PreferenceManager.getInstance().getCurrentUserAvatar();
    }
    
    public void setSettingMsgNotification(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgNotification(paramBoolean);
        valueCache.put(Key.VibrateAndPlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgNotification() {
        Object val = valueCache.get(Key.VibrateAndPlayToneOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgNotification();
            valueCache.put(Key.VibrateAndPlayToneOn, val);
        }
       
        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgSound(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSound(paramBoolean);
        valueCache.put(Key.PlayToneOn, paramBoolean);
    }

    public boolean getSettingMsgSound() {
        Object val = valueCache.get(Key.PlayToneOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgSound();
            valueCache.put(Key.PlayToneOn, val);
        }
       
        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgVibrate(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgVibrate(paramBoolean);
        valueCache.put(Key.VibrateOn, paramBoolean);
    }

    public boolean getSettingMsgVibrate() {
        Object val = valueCache.get(Key.VibrateOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgVibrate();
            valueCache.put(Key.VibrateOn, val);
        }
       
        return (Boolean) (val != null?val:true);
    }

    public void setSettingMsgSpeaker(boolean paramBoolean) {
        PreferenceManager.getInstance().setSettingMsgSpeaker(paramBoolean);
        valueCache.put(Key.SpakerOn, paramBoolean);
    }

    public boolean getSettingMsgSpeaker() {        
        Object val = valueCache.get(Key.SpakerOn);

        if(val == null){
            val = PreferenceManager.getInstance().getSettingMsgSpeaker();
            valueCache.put(Key.SpakerOn, val);
        }
       
        return (Boolean) (val != null?val:true);
    }

    public boolean isMsgRoaming() {
        return PreferenceManager.getInstance().isMsgRoaming();
    }

    public void setMsgRoaming(boolean roaming) {
        PreferenceManager.getInstance().setMsgRoaming(roaming);
    }

    public boolean isShowMsgTyping() {
        return PreferenceManager.getInstance().isShowMsgTyping();
    }

    public void showMsgTyping(boolean show) {
        PreferenceManager.getInstance().showMsgTyping(show);
    }


    /**
     * 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
     * @param value
     */
    public void allowChatroomOwnerLeave(boolean value){
        OptionsHelper.getInstance().allowChatroomOwnerLeave(value);
    }

    /**
     * 获取聊天室owner离开时的设置
     * @return
     */
    public boolean isChatroomOwnerLeaveAllowed(){
        return OptionsHelper.getInstance().isChatroomOwnerLeaveAllowed();
    }

    /**
     * 设置退出(主动和被动退出)群组时是否删除聊天消息
     * @param value
     */
    public void setDeleteMessagesAsExitGroup(boolean value) {
        OptionsHelper.getInstance().setDeleteMessagesAsExitGroup(value);
    }

    /**
     * 获取退出(主动和被动退出)群组时是否删除聊天消息
     * @return
     */
    public boolean isDeleteMessagesAsExitGroup() {
        return OptionsHelper.getInstance().isDeleteMessagesAsExitGroup();
    }

    /**
     * 设置退出（主动和被动）聊天室时是否删除聊天信息
     * @param value
     */
    public void setDeleteMessagesAsExitChatRoom(boolean value) {
        OptionsHelper.getInstance().setDeleteMessagesAsExitChatRoom(value);
    }

    /**
     * 获取退出(主动和被动退出)聊天室时是否删除聊天消息
     * @return
     */
    public boolean isDeleteMessagesAsExitChatRoom() {
        return OptionsHelper.getInstance().isDeleteMessagesAsExitChatRoom();
    }

    /**
     * 设置是否自动接受加群邀请
     * @param value
     */
    public void setAutoAcceptGroupInvitation(boolean value) {
        OptionsHelper.getInstance().setAutoAcceptGroupInvitation(value);
    }

    /**
     * 获取是否自动接受加群邀请
     * @return
     */
    public boolean isAutoAcceptGroupInvitation() {
        return OptionsHelper.getInstance().isAutoAcceptGroupInvitation();
    }

    /**
     * 设置是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @param value
     */
    public void setTransfeFileByUser(boolean value) {
        OptionsHelper.getInstance().setTransfeFileByUser(value);
    }

    /**
     * 获取是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @return
     */
    public boolean isSetTransferFileByUser() {
        return OptionsHelper.getInstance().isSetTransferFileByUser();
    }

    /**
     * 是否自动下载缩略图，默认是true为自动下载
     * @param autodownload
     */
    public void setAutodownloadThumbnail(boolean autodownload) {
        OptionsHelper.getInstance().setAutodownloadThumbnail(autodownload);
    }

    /**
     * 获取是否自动下载缩略图
     * @return
     */
    public boolean isSetAutodownloadThumbnail() {
        return OptionsHelper.getInstance().isSetAutodownloadThumbnail();
    }


    /**
     * 设置是否只使用Https
     * @param usingHttpsOnly
     */
    public void setUsingHttpsOnly(boolean usingHttpsOnly) {
        OptionsHelper.getInstance().setUsingHttpsOnly(usingHttpsOnly);
    }

    /**
     * 获取是否只使用Https
     * @return
     */
    public boolean getUsingHttpsOnly() {
        return OptionsHelper.getInstance().getUsingHttpsOnly();
    }

    public void setSortMessageByServerTime(boolean sortByServerTime) {
        OptionsHelper.getInstance().setSortMessageByServerTime(sortByServerTime);
    }

    public boolean isSortMessageByServerTime() {
        return OptionsHelper.getInstance().isSortMessageByServerTime();
    }

    /**
     * 保存未发送的文本消息内容
     * @param toChatUsername
     * @param content
     */
    public void saveUnSendMsg(String toChatUsername, String content) {
        //EasePreferenceManager.getInstance().saveUnSendMsgInfo(toChatUsername, content);
    }

    public String getUnSendMsg(String toChatUsername) {
        //return EasePreferenceManager.getInstance().getUnSendMsgInfo(toChatUsername);
        return "";
    }


    public void setDisabledGroups(List<String> groups){
//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        List<String> list = new ArrayList<String>();
//        list.addAll(groups);
//        for(int i = 0; i < list.size(); i++){
//            if(EaseAtMessageHelper.get().getAtMeGroups().contains(list.get(i))){
//                list.remove(i);
//                i--;
//            }
//        }
//
//        dao.setDisabledGroups(list);
//        valueCache.put(Key.DisabledGroups, list);
    }

    public List<String> getDisabledGroups(){
        Object val = valueCache.get(Key.DisabledGroups);

//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        if(val == null){
//            val = dao.getDisabledGroups();
//            valueCache.put(Key.DisabledGroups, val);
//        }

        //noinspection unchecked
        return (List<String>) val;
    }

    public void setDisabledIds(List<String> ids){
//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        dao.setDisabledIds(ids);
//        valueCache.put(Key.DisabledIds, ids);
    }

    public List<String> getDisabledIds(){
        Object val = valueCache.get(Key.DisabledIds);

//        if(dao == null){
//            dao = new UserDao(context);
//        }
//
//        if(val == null){
//            val = dao.getDisabledIds();
//            valueCache.put(Key.DisabledIds, val);
//        }

        //noinspection unchecked
        return (List<String>) val;
    }


    enum Key{
        VibrateAndPlayToneOn,
        VibrateOn,
        PlayToneOn,
        SpakerOn,
        DisabledGroups,
        DisabledIds
    }
}
