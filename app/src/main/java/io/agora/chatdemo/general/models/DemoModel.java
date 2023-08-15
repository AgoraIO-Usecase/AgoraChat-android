package io.agora.chatdemo.general.models;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatRoom;
import io.agora.chat.uikit.manager.EasePreferenceManager;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.dao.EmUserDao;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.manager.OptionsHelper;
import io.agora.chatdemo.general.manager.PreferenceManager;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;

/**
 * DemoModel is mainly used for SP access and access to some databases
 */
public class DemoModel {
    private final EMContactManagerRepository mRepository;
    EmUserDao dao = null;
    protected Context context = null;
    protected Map<Key,Object> valueCache = new HashMap<Key,Object>();
    public List<ChatRoom> chatRooms;

    //User attribute data expiration time setting
    public static long userInfoTimeOut =  7 * 24 * 60 * 60 * 1000;
    
    public DemoModel(Context ctx){
        context = ctx;
        PreferenceManager.init(context);
        mRepository = new EMContactManagerRepository();
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

    /**
     * Get all contacts, include friends and blacklist
     * @return
     */
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

    /**
     * Get all users
     * @return
     */
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

    /**
     * Get contacts who are friends
     * @return
     */
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
     * Determine whether it is a contact
     * @param userId
     * @return
     */
    public boolean isContact(String userId) {
        Map<String, EaseUser> contactList = getFriendContactList();
        return contactList.keySet().contains(userId);
    }

    /**
     * Save contact to the database
     * @param user
     */
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
     * Insert data into the database
     * @param object
     */
    public void insert(Object object) {
        mRepository.insert(object);
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
     * Find out the user ID about the user's user attribute expired
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
     * Save whether to delete the status of the contact
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
     * Check if the contact is deleted
     * @param username
     * @return
     */
    public boolean isDeleteUsername(String username) {
        SharedPreferences sp = context.getSharedPreferences("save_delete_username_status", Context.MODE_PRIVATE);
        return sp.getBoolean(username, false);
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
     * Set whether to allow the chat room owner to leave and delete the conversation record,
     * which means that the owner will never receive any messages
     * @param value
     */
    public void allowChatroomOwnerLeave(boolean value){
        OptionsHelper.getInstance().allowChatroomOwnerLeave(value);
    }

    /**
     * Get the settings when the chat room owner leaves
     * @return
     */
    public boolean isChatroomOwnerLeaveAllowed(){
        return OptionsHelper.getInstance().isChatroomOwnerLeaveAllowed();
    }

    /**
     * Set whether to delete chat messages when exiting (active and passive exit) groups
     * @param value
     */
    public void setDeleteMessagesAsExitGroup(boolean value) {
        OptionsHelper.getInstance().setDeleteMessagesAsExitGroup(value);
    }

    /**
     * Get whether to delete chat messages when exiting (active and passive exit) groups
     * @return
     */
    public boolean isDeleteMessagesAsExitGroup() {
        return OptionsHelper.getInstance().isDeleteMessagesAsExitGroup();
    }

    /**
     * Set whether to delete chat messages when exiting (active and passive exit) chatroom
     * @param value
     */
    public void setDeleteMessagesAsExitChatRoom(boolean value) {
        OptionsHelper.getInstance().setDeleteMessagesAsExitChatRoom(value);
    }

    /**
     * Get whether to delete chat messages when exiting (active and passive exit) chatroom
     * @return
     */
    public boolean isDeleteMessagesAsExitChatRoom() {
        return OptionsHelper.getInstance().isDeleteMessagesAsExitChatRoom();
    }

    /**
     * Set whether to automatically accept group invitations
     * @param value
     */
    public void setAutoAcceptGroupInvitation(boolean value) {
        OptionsHelper.getInstance().setAutoAcceptGroupInvitation(value);
    }

    /**
     * Get whether to automatically accept group invitation
     * @return
     */
    public boolean isAutoAcceptGroupInvitation() {
        return OptionsHelper.getInstance().isAutoAcceptGroupInvitation();
    }

    /**
     * Set whether to automatically upload message attachments to the ring letter server,
     * the default is True is to use the ring letter server to upload and download
     * @param value
     */
    public void setTransferFileByUser(boolean value) {
        OptionsHelper.getInstance().setTransferFileByUser(value);
    }

    /**
     * Get whether to automatically upload the message attachment to the ring letter server,
     * the default is True is to use the ring letter server to upload and download
     * @return
     */
    public boolean isSetTransferFileByUser() {
        return OptionsHelper.getInstance().isSetTransferFileByUser();
    }

    /**
     * Set whether to download thumbnails automatically, the default is true for automatic download
     * @param autodownload
     */
    public void setAutodownloadThumbnail(boolean autodownload) {
        OptionsHelper.getInstance().setAutodownloadThumbnail(autodownload);
    }

    /**
     * Get whether to download thumbnails automatically
     * @return
     */
    public boolean isSetAutodownloadThumbnail() {
        return OptionsHelper.getInstance().isSetAutodownloadThumbnail();
    }


    /**
     * Set whether to use Https only
     * @param usingHttpsOnly
     */
    public void setUsingHttpsOnly(boolean usingHttpsOnly) {
        OptionsHelper.getInstance().setUsingHttpsOnly(usingHttpsOnly);
    }

    /**
     * Get whether to use Https only
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
     * Save unsent text message content
     * @param toChatUsername
     * @param content
     */
    public void saveUnSendMsg(String toChatUsername, String content) {
        EasePreferenceManager.getInstance().saveUnSendMsgInfo(toChatUsername, content);
    }

    public String getUnSendMsg(String toChatUsername) {
        return EasePreferenceManager.getInstance().getUnSendMsgInfo(toChatUsername);
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

    /**
     *  get target translation language
     */
    public String getTargetLanguage() {
        return PreferenceManager.getInstance().getTargetLanguage();
    }

    /**
     *  set target translation language
     */
    public void setTargetLanguage(String languageCode) {
        PreferenceManager.getInstance().setTargetLanguage(languageCode);
    }

    public void clearTargetLanguage(){
        PreferenceManager.getInstance().clearTargetLanguage();
    }

    public void setPushLanguage(String languageCode){
        PreferenceManager.getInstance().setPushLanguage(languageCode);
    }

    public String getPushLanguage(){
        return PreferenceManager.getInstance().getPushLanguage();
    }

    public void clearPushLanguage(){
        PreferenceManager.getInstance().clearPushLanguage();
    }

    public void setEnableAutoTranslation(String isEnable){
        PreferenceManager.getInstance().setEnableAutoTranslation(isEnable);
    }

    public String getEnableAutoTranslation(){
        return PreferenceManager.getInstance().getEnableAutoTranslation();
    }

    public void clearAutoTranslation(){
        PreferenceManager.getInstance().clearAutoTranslation();
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
