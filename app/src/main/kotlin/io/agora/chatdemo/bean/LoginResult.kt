package io.agora.chatdemo.bean

class LoginResult {
    var code = 0
    var agoraUid: Int = 0
    var chatUserName: String? = null
    var avatarUrl: String? = null
    var token: String? = null
    var accessToken: String? = null
    var expireTimestamp: Long? = null
}