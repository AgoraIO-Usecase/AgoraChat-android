package io.agora.chatdemo.general.manager;

public class OptionsHelper {
    private String DEF_APPKEY = "";
    private static final String DEF_IM_SERVER = "msync-im1.sandbox.easemob.com";
    private static final int DEF_IM_PORT = 6717;
    private static final String DEF_REST_SERVER = "a1.sdb.easemob.com";

    private static OptionsHelper instance;

    private OptionsHelper(){}

    public static OptionsHelper getInstance() {
        if(instance == null) {
            synchronized (OptionsHelper.class) {
                if(instance == null) {
                    instance = new OptionsHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Set whether to use Https only
     * @param usingHttpsOnly
     */
    public void setUsingHttpsOnly(boolean usingHttpsOnly) {
        PreferenceManager.getInstance().setUsingHttpsOnly(usingHttpsOnly);
    }

    /**
     * Get whether to use Https only
     * @return
     */
    public boolean getUsingHttpsOnly() {
        return PreferenceManager.getInstance().getUsingHttpsOnly();
    }

    /**
     * Set whether to allow the chat room owner to leave and delete the conversation record,
     * which means that the owner will never receive any messages
     * @param value
     */
    public void allowChatroomOwnerLeave(boolean value){
        PreferenceManager.getInstance().setSettingAllowChatroomOwnerLeave(value);
    }

    /**
     * Get the settings when the chat room owner leaves
     * @return
     */
    public boolean isChatroomOwnerLeaveAllowed(){
        return PreferenceManager.getInstance().getSettingAllowChatroomOwnerLeave();
    }

    /**
     * Set whether to delete chat messages when exiting (active and passive exit) groups
     * @param value
     */
    public void setDeleteMessagesAsExitGroup(boolean value) {
        PreferenceManager.getInstance().setDeleteMessagesAsExitGroup(value);
    }

    /**
     * Get whether to delete chat messages when exiting (active and passive exit) groups
     * @return
     */
    public boolean isDeleteMessagesAsExitGroup() {
        return PreferenceManager.getInstance().isDeleteMessagesAsExitGroup();
    }

    /**
     * Set whether to delete chat messages when exiting (active and passive exit) chatroom
     * @param value
     */
    public void setDeleteMessagesAsExitChatRoom(boolean value){
        PreferenceManager.getInstance().setDeleteMessagesAsExitChatRoom(value);
    }

    /**
     * Get whether to delete chat messages when exiting (active and passive exit) chatroom
     * @return
     */
    public boolean isDeleteMessagesAsExitChatRoom() {
        return PreferenceManager.getInstance().isDeleteMessagesAsExitChatRoom();
    }

    /**
     * Set whether to automatically accept group invitations
     * @param value
     */
    public void setAutoAcceptGroupInvitation(boolean value) {
        PreferenceManager.getInstance().setAutoAcceptGroupInvitation(value);
    }

    /**
     * Get whether to automatically accept group invitation
     * @return
     */
    public boolean isAutoAcceptGroupInvitation() {
        return PreferenceManager.getInstance().isAutoAcceptGroupInvitation();
    }

    /**
     * Set whether to automatically upload message attachments to the ring letter server,
     * the default is True is to use the ring letter server to upload and download
     * @param value
     */
    public void setTransferFileByUser(boolean value) {
        PreferenceManager.getInstance().setTransferFileByUser(value);
    }

    /**
     * Get whether to automatically upload the message attachment to the ring letter server,
     * the default is True is to use the ring letter server to upload and download
     * @return
     */
    public boolean isSetTransferFileByUser() {
        return PreferenceManager.getInstance().isSetTransferFileByUser();
    }

    /**
     * Set whether to download thumbnails automatically, the default is true for automatic download
     * @param autodownload
     */
    public void setAutodownloadThumbnail(boolean autodownload) {
        PreferenceManager.getInstance().setAudodownloadThumbnail(autodownload);
    }

    /**
     * Get whether to download thumbnails automatically
     * @return
     */
    public boolean isSetAutodownloadThumbnail() {
        return PreferenceManager.getInstance().isSetAutodownloadThumbnail();
    }

    /**
     * Set whether sort messages by server time
     * @param sortByServerTime
     */
    public void setSortMessageByServerTime(boolean sortByServerTime) {
        PreferenceManager.getInstance().setSortMessageByServerTime(sortByServerTime);
    }

    /**
     * Get whether sort messages by server time
     * @return
     */
    public boolean isSortMessageByServerTime() {
        return PreferenceManager.getInstance().isSortMessageByServerTime();
    }
}
