package io.agora.chatdemo.common

import io.agora.uikit.common.ChatError


/**
 * 定义一些本地的错误code
 */
object ErrorCode : ChatError() {
    /**
     * 当前网络不可用
     */
    const val EM_NETWORK_ERROR = -2

    /**
     * 未登录过环信
     */
    const val EM_NOT_LOGIN = -8

    /**
     * result解析错误
     */
    const val EM_PARSE_ERROR = -10

    /**
     * 网络问题请稍后重试
     */
    const val EM_ERR_UNKNOWN = -20

    /**
     * 安卓版本问题,只支持4.4以上
     */
    const val EM_ERR_IMAGE_ANDROID_MIN_VERSION = -50

    /**
     * 文件不存在
     */
    const val EM_ERR_FILE_NOT_EXIST = -55

    /**
     * 添加自己为好友
     */
    const val EM_ADD_SELF_ERROR = -100

    /**
     * 已经是好友
     */
    const val EM_FRIEND_ERROR = -101

    /**
     * 已经添加到黑名单中
     */
    const val EM_FRIEND_BLACK_ERROR = -102

    /**
     * 没有群组成员
     */
    const val EM_ERR_GROUP_NO_MEMBERS = -105

    /**
     * 删除对话失败
     */
    const val EM_DELETE_CONVERSATION_ERROR = -110
    const val EM_DELETE_SYS_MSG_ERROR = -115

}