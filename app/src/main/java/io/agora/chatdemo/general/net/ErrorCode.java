package io.agora.chatdemo.general.net;


import io.agora.Error;
import io.agora.chatdemo.R;

/**
 * Define some local error codes
 */
public class ErrorCode extends Error {
    /**
     * Network is unavailable
     */
    public static final int NETWORK_ERROR = -2;

    /**
     * Haven't logged in to Agora Chat SDK
     */
    public static final int NOT_LOGIN = -8;

    /**
     * result parsing error
     */
    public static final int PARSE_ERROR = -10;

    /**
     * Network problem, please try again later
     */
    public static final int ERR_UNKNOWN = -20;

    /**
     * Android version issue, only supports above 4.4
     */
    public static final int ERR_IMAGE_ANDROID_MIN_VERSION = -50;

    /**
     * file does not exist
     */
    public static final int ERR_FILE_NOT_EXIST = -55;

    /**
     * Add self as a friend
     */
    public static final int ADD_SELF_ERROR = -100;

    /**
     * Already friends
     */
    public static final int FRIEND_ERROR = -101;

    /**
     * Has been added to the blacklist
     */
    public static final int FRIEND_BLACK_ERROR = -102;

    /**
     * No group members
     */
    public static final int ERR_GROUP_NO_MEMBERS = -105;

    /**
     * Failed to delete conversation
     */
    public static final int DELETE_CONVERSATION_ERROR = -110;

    public static final int DELETE_SYS_MSG_ERROR = -115;

    public enum Error {
        NETWORK_ERROR(ErrorCode.NETWORK_ERROR, R.string.error_network_error),
        NOT_LOGIN(ErrorCode.NOT_LOGIN, R.string.error_not_login),
        PARSE_ERROR(ErrorCode.PARSE_ERROR, R.string.error_parse_error),
        ERR_UNKNOWN(ErrorCode.ERR_UNKNOWN, R.string.error_err_unknown),
        ERR_IMAGE_ANDROID_MIN_VERSION(ErrorCode.ERR_IMAGE_ANDROID_MIN_VERSION, R.string.err_image_android_min_version),
        ERR_FILE_NOT_EXIST(ErrorCode.ERR_FILE_NOT_EXIST, R.string.err_file_not_exist),
        ADD_SELF_ERROR(ErrorCode.ADD_SELF_ERROR, R.string.error_add_self),
        FRIEND_ERROR(ErrorCode.FRIEND_ERROR, R.string.error_already_friend),
        FRIEND_BLACK_ERROR(ErrorCode.FRIEND_BLACK_ERROR, R.string.error_already_friend_but_in_black),
        ERR_GROUP_NO_MEMBERS(ErrorCode.ERR_GROUP_NO_MEMBERS, R.string.error_group_no_members),
        DELETE_CONVERSATION_ERROR(ErrorCode.DELETE_CONVERSATION_ERROR, R.string.error_delete_conversation),
        DELETE_SYS_MSG_ERROR(ErrorCode.DELETE_SYS_MSG_ERROR, R.string.error_delete_not_exist_msg),
        USER_ALREADY_EXIST(ErrorCode.USER_ALREADY_EXIST, R.string.error_user_already_exist),
        UNKNOWN_ERROR(-9999, 0);


        private int code;
        private int messageId;

        private Error(int code, int messageId) {
            this.code = code;
            this.messageId = messageId;
        }

        public static Error parseMessage(int errorCode) {
            for (Error error: Error.values()) {
                if(error.code == errorCode) {
                    return error;
                }
            }
            return UNKNOWN_ERROR;
        }


        public int getMessageId() {
            return messageId;
        }


    }
}
