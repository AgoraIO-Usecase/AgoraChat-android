package io.agora.chatdemo.general.models;

public class LoginBean {
    private String accessToken;
    private String userNickname;
    private int agoraUid;

    public int getAgoraUid() {
        return agoraUid;
    }

    public void setAgoraUid(int agoraUid) {
        this.agoraUid = agoraUid;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }
}
