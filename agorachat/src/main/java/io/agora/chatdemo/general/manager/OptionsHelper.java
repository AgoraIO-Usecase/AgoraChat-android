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
     * 设置是否只使用Https
     * @param usingHttpsOnly
     */
    public void setUsingHttpsOnly(boolean usingHttpsOnly) {
        PreferenceManager.getInstance().setUsingHttpsOnly(usingHttpsOnly);
    }

    /**
     * 获取是否只使用Https
     * @return
     */
    public boolean getUsingHttpsOnly() {
        return PreferenceManager.getInstance().getUsingHttpsOnly();
    }

    /**
     * 设置是否允许聊天室owner离开并删除会话记录，意味着owner再不会受到任何消息
     * @param value
     */
    public void allowChatroomOwnerLeave(boolean value){
        PreferenceManager.getInstance().setSettingAllowChatroomOwnerLeave(value);
    }

    /**
     * 获取聊天室owner离开时的设置
     * @return
     */
    public boolean isChatroomOwnerLeaveAllowed(){
        return PreferenceManager.getInstance().getSettingAllowChatroomOwnerLeave();
    }

    /**
     * 设置退出(主动和被动退出)群组时是否删除聊天消息
     * @param value
     */
    public void setDeleteMessagesAsExitGroup(boolean value) {
        PreferenceManager.getInstance().setDeleteMessagesAsExitGroup(value);
    }

    /**
     * 获取退出(主动和被动退出)群组时是否删除聊天消息
     * @return
     */
    public boolean isDeleteMessagesAsExitGroup() {
        return PreferenceManager.getInstance().isDeleteMessagesAsExitGroup();
    }


    public void setDeleteMessagesAsExitChatRoom(boolean value){
        PreferenceManager.getInstance().setDeleteMessagesAsExitChatRoom(value);
    }

    public boolean isDeleteMessagesAsExitChatRoom() {
        return PreferenceManager.getInstance().isDeleteMessagesAsExitChatRoom();
    }

    /**
     * 设置是否自动接受加群邀请
     * @param value
     */
    public void setAutoAcceptGroupInvitation(boolean value) {
        PreferenceManager.getInstance().setAutoAcceptGroupInvitation(value);
    }

    /**
     * 获取是否自动接受加群邀请
     * @return
     */
    public boolean isAutoAcceptGroupInvitation() {
        return PreferenceManager.getInstance().isAutoAcceptGroupInvitation();
    }

    /**
     * 设置是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @param value
     */
    public void setTransfeFileByUser(boolean value) {
        PreferenceManager.getInstance().setTransferFileByUser(value);
    }

    /**
     * 获取是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载
     * @return
     */
    public boolean isSetTransferFileByUser() {
        return PreferenceManager.getInstance().isSetTransferFileByUser();
    }

    /**
     * 是否自动下载缩略图，默认是true为自动下载
     * @param autodownload
     */
    public void setAutodownloadThumbnail(boolean autodownload) {
        PreferenceManager.getInstance().setAudodownloadThumbnail(autodownload);
    }

    /**
     * 获取是否自动下载缩略图
     * @return
     */
    public boolean isSetAutodownloadThumbnail() {
        return PreferenceManager.getInstance().isSetAutodownloadThumbnail();
    }

    public void setSortMessageByServerTime(boolean sortByServerTime) {
        PreferenceManager.getInstance().setSortMessageByServerTime(sortByServerTime);
    }

    public boolean isSortMessageByServerTime() {
        return PreferenceManager.getInstance().isSortMessageByServerTime();
    }
}
