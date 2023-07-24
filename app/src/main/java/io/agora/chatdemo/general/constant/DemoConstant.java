package io.agora.chatdemo.general.constant;


import io.agora.chat.uikit.constants.EaseConstant;

public interface DemoConstant extends EaseConstant {
    String ACCOUNT_CHANGE = "account_change";
    String ACCOUNT_REMOVED = "account_removed";
    String ACCOUNT_CONFLICT = "conflict";
    String ACCOUNT_FORBIDDEN = "user_forbidden";
    String ACCOUNT_KICKED_BY_CHANGE_PASSWORD = "kicked_by_change_password";
    String ACCOUNT_KICKED_BY_OTHER_DEVICE = "kicked_by_another_device";

    String CONFERENCE_ID = "confId";
    String CONFERENCE_PASS = "password";
    String CONFERENCE_INVITER = "inviter";
    String CONFERENCE_IS_CREATOR = "is_creator";
    String CONFERENCE_GROUP_ID = "groupId";
    String CONFERENCE_GROUP_EXIST_MEMBERS = "exist_members";

    String OP_INVITE = "invite";
    String OP_REQUEST_TOBE_SPEAKER = "request_tobe_speaker";
    String OP_REQUEST_TOBE_AUDIENCE = "request_tobe_audience";

    String EM_CONFERENCE_OP = "em_conference_op";
    String EM_CONFERENCE_ID = "em_conference_id";
    String EM_CONFERENCE_PASSWORD = "em_conference_password";
    String EM_CONFERENCE_TYPE = "em_conference_type";
    String EM_MEMBER_NAME = "em_member_name";
    String EM_NOTIFICATION_TYPE = "em_notification_type";

    String MSG_ATTR_CONF_ID = "conferenceId";
    String MSG_ATTR_CONF_PASS = CONFERENCE_PASS;
    String MSG_ATTR_EXTENSION = "msg_extension";

    String NEW_FRIENDS_USERNAME = "item_new_friends";
    String GROUP_USERNAME = "item_groups";
    String CHAT_ROOM = "item_chatroom";
    String CHAT_ROBOT = "item_robots";

    String NOTIFY_GROUP_INVITE_RECEIVE = "invite_receive";
    String NOTIFY_GROUP_INVITE_ACCEPTED = "invite_accepted";
    String NOTIFY_GROUP_INVITE_DECLINED = "invite_declined";
    String NOTIFY_GROUP_JOIN_RECEIVE = "invite_join_receive";
    String NOTIFY_CHANGE = "notify_change";

    String MESSAGE_GROUP_JOIN_ACCEPTED = "message_join_accepted";
    String MESSAGE_GROUP_AUTO_ACCEPT = "message_auto_accept";

    String CONTACT_REMOVE = "contact_remove";
    String CONTACT_ACCEPT = "contact_accept";
    String CONTACT_DECLINE = "contact_decline";
    String CONTACT_BAN = "contact_ban";
    String CONTACT_ALLOW = "contact_allow";

    String CONTACT_CHANGE = "contact_change";
    String CONTACT_UNREAD_CHANGE = "contact_unread_change";
    String CONTACT_ADD = "contact_add";
    String CONTACT_DELETE = "contact_delete";
    String CONTACT_UPDATE = "contact_update";
    String CURRENT_USER_INFO_CHANGE = "current_user_info_change";
    String REMOVE_BLACK = "remove_black";

    String GROUP_CHANGE = "group_change";
    String GROUP_OWNER_TRANSFER = "group_owner_transfer";
    String GROUP_SHARE_FILE_CHANGE = "group_share_file_change";

    String CHAT_ROOM_CHANGE = "chat_room_change";
    String CHAT_ROOM_DESTROY = "chat_room_destroy";

    String THREAD_CHANGE = "thread_change";
    String THREAD_LEAVE = "thread_leave";
    String THREAD_DESTROY = "thread_destroy";

    String REFRESH_NICKNAME = "refresh_nickname";

    String CONVERSATION_DELETE = "conversation_delete";
    String CONVERSATION_READ = "conversation_read";

    String MESSAGE_NOT_SEND = "message_not_send";

    String SYSTEM_MESSAGE_FROM = "from";
    String SYSTEM_MESSAGE_REASON = "reason";
    String SYSTEM_MESSAGE_STATUS = "status";
    String SYSTEM_MESSAGE_GROUP_ID = "groupId";
    String SYSTEM_MESSAGE_NAME = "name";
    String SYSTEM_MESSAGE_INVITER = "inviter";
    String SYSTEM_MESSAGE_EXPIRED = "expired";

    String USER_CARD_EVENT = "userCard";
    String USER_CARD_ID = "uid";
    String USER_CARD_NICK = "nickname";
    String USER_CARD_AVATAR = "avatar";
    String ID_OR_NICKNAME = "idOrNickname";

    String USER_INFO = "userinfo";

    String GROUP_NAME = "group_name";
    String GROUP_DESC = "group_desc";
    String GROUP_REASON = "group_reason";
    String GROUP_PUBLIC = "group_public";
    String GROUP_ALLOW_INVITE = "group_allow_invite";
    String GROUP_MAX_USERS = "group_max_users";

    String GROUP_MEMBER_USER = "group_member_user";

    String PRESENCES_CHANGED = "presences_changed";
    String PRESENCE_CUSTOM = "PRESENCE_CUSTOM";
    int PRESENCE_CUSTOM_REQUESTCODE_FORM_SETPRESENCEFRAGAGMENT = 3;
    int PRESENCE_CUSTOM_REQUESTCODE_FORM_CONVERSATIONFRAGMENT = 4;
    int PRESENCE_RESULTCODE = 5;

    int GROUP_ROLE_OWNER = 1;
    int GROUP_ROLE_ADMIN = 2;
    int GROUP_ROLE_MEMBER = 0;


    int VIEW_TYPE_MESSAGE_CALL_ME = 18;//must larger than 17,the lagest number which defined in uikit#EaseChatType
    int VIEW_TYPE_MESSAGE_CALL_OTHER = 19;

    int VIEW_TYPE_MESSAGE_URL_PREVIEW_ME = 20;
    int VIEW_TYPE_MESSAGE_URL_PREVIEW_OTHER = 21;

    String CALL_TYPE = "easeCallType";

     String DETAIL_TYPE = "detail_type";
     int DETAIL_TYPE_USER = 0;
     int DETAIL_TYPE_CHAT = 1;
     int DETAIL_TYPE_GROUP = 2;
     int DETAIL_TYPE_THREAD = 3;
     String DETAIL_ID = "detail_id";

    String SILENT_DURATION = "silent_duration";

    String EASE_SYSTEM_NOTIFICATION_TYPE = "em_system_notification_type";

    String SYSTEM_NOTIFICATION_TYPE = "system_notification_type";
    String SYSTEM_CREATE_GROUP = "system_createdGroup";
    String SYSTEM_JOINED_GROUP = "system_joinedGroup";
    String SYSTEM_ADD_CONTACT = "system_add_contact";
    String SYSTEM_INVITATION_CONTACT = "system_invitation_contact";
    String SYSTEM_GROUP_INVITE_ACCEPT = "system_group_invite_accept";
    String SYSTEM_NOTIFICATION_NICKNAME = "system_notification_nickname";
    String SYSTEM_CHANGE_OWNER = "system_change_owner";
    String SYSTEM_CHANGE_GROUP_NAME = "system_change_group_name";

    String REPORT_MESSAGE_ID = "report_message_id";

}
