package io.agora.chatdemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatManager;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatOptions;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.ContactManager;
import io.agora.chat.Conversation;
import io.agora.chat.GroupManager;
import io.agora.chat.PushManager;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.manager.EaseNotifier;
import io.agora.chat.uikit.models.EaseGroupInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.options.EaseAvatarOptions;
import io.agora.chat.uikit.provider.EaseFileIconProvider;
import io.agora.chat.uikit.provider.EaseGroupInfoProvider;
import io.agora.chat.uikit.provider.EaseSettingsProvider;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chat.uikit.utils.EaseCompat;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.manager.UsersManager;
import io.agora.chatdemo.general.models.DemoModel;
import io.agora.chatdemo.global.GlobalEventsMonitor;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.push.PushConfig;
import io.agora.push.PushHelper;
import io.agora.push.PushListener;
import io.agora.push.PushType;
import io.agora.util.EMLog;


/**
 * Demo's helper class, such as initialize Agora Chat SDK
 */
public class DemoHelper {
    private static final String TAG = DemoHelper.class.getSimpleName();

    public boolean isSDKInit;
    private static DemoHelper mInstance;
    private DemoModel demoModel = null;
    private Map<String, EaseUser> contactList;
    private UsersManager usersManager;

    private DemoHelper() {}

    public static DemoHelper getInstance() {
        if(mInstance == null) {
            synchronized (DemoHelper.class) {
                if(mInstance == null) {
                    mInstance = new DemoHelper();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        demoModel = new DemoModel(context);
        //Initialize Agora Chat SDK
        if(initSDK(context)) {
            // debug mode, you'd better set it to false, if you want release your App officially.
            ChatClient.getInstance().setDebugMode(true);
            // Initialize Push
            initPush(context);
            // Initialize UIKit
            initEaseUIKit(context);
        }

    }

    /**
     * Initialize Agora Chat SDK
     * @param context
     * @return
     */
    private boolean initSDK(Context context) {
        // Set Chat Options
        ChatOptions options = initChatOptions(context);
        if(options == null) {
            return false;
        }
        // Configure custom rest server and im server
        //options.setRestServer(BuildConfig.APP_SERVER_DOMAIN);
        //options.setIMServer("106.75.100.247");
        //options.setImPort(6717);
        options.setUsingHttpsOnly(true);
        // Call UIKit to initialize Agora Chat SDK
        isSDKInit = EaseUIKit.getInstance().init(context, options);
        return isSDKInit();
    }


    /**
     * Determine if you have logged in before
     * @return
     */
    public boolean isLoggedIn() {
        return getChatClient().isLoggedInBefore();
    }

    /**
     * Get ChatClient's entity
     * @return
     */
    public ChatClient getChatClient() {
        return ChatClient.getInstance();
    }

    /**
     * Get the entity of contact manager
     * @return
     */
    public ContactManager getContactManager() {
        return getChatClient().contactManager();
    }

    /**
     * Get the entity of group manager
     * @return
     */
    public GroupManager getGroupManager() {
        return getChatClient().groupManager();
    }

    /**
     * Get the entity of chatroom manager
     * @return
     */
    public ChatRoomManager getChatroomManager() {
        return getChatClient().chatroomManager();
    }


    /**
     * Get the entity of EMChatManager
     * @return
     */
    public ChatManager getChatManager() {
        return getChatClient().chatManager();
    }

    /**
     * Get the entity of push manager
     * @return
     */
    public PushManager getPushManager() {
        return getChatClient().pushManager();
    }

    /**
     * Initialize UIKit
     * @param context
     */
    private void initEaseUIKit(Context context) {
        //Set custom chat presenter
        EaseUIKit.getInstance().addChatPresenter(GlobalEventsMonitor.getInstance());
        EaseUIKit.getInstance()
                .setSettingsProvider(new EaseSettingsProvider() {
                    @Override
                    public boolean isMsgNotifyAllowed(ChatMessage message) {
                        if(message == null){
                            return demoModel.getSettingMsgNotification();
                        }
                        if(!demoModel.getSettingMsgNotification()){
                            return false;
                        }else{
                            String chatUsename = null;
                            List<String> notNotifyIds = null;
                            // get user or group id which was blocked to show message notifications
                            if (message.getChatType() == ChatMessage.ChatType.Chat) {
                                chatUsename = message.getFrom();
                                notNotifyIds = demoModel.getDisabledIds();
                            } else {
                                chatUsename = message.getTo();
                                notNotifyIds = demoModel.getDisabledGroups();
                            }

                            if (notNotifyIds == null || !notNotifyIds.contains(chatUsename)) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }

                    @Override
                    public boolean isMsgSoundAllowed(ChatMessage message) {
                        return demoModel.getSettingMsgSound();
                    }

                    @Override
                    public boolean isMsgVibrateAllowed(ChatMessage message) {
                        return demoModel.getSettingMsgVibrate();
                    }

                    @Override
                    public boolean isSpeakerOpened() {
                        return demoModel.getSettingMsgSpeaker();
                    }
                })
                .setUserProvider(new EaseUserProfileProvider() {
                    @Override
                    public EaseUser getUser(String userID) {
                        return getUsersManager().getUserInfo(userID);
                    }

                })
                .setGroupInfoProvider(new EaseGroupInfoProvider() {
                    @Override
                    public EaseGroupInfo getGroupInfo(String groupId, int type) {
                        if(type == Conversation.ConversationType.GroupChat.ordinal()) {
                            EaseGroupInfo info = new EaseGroupInfo();
                            info.setIcon(ContextCompat.getDrawable(context, R.drawable.group_avatar));
                            EaseGroupInfo.AvatarSettings settings = new EaseGroupInfo.AvatarSettings();
                            settings.setAvatarShapeType(2);
                            settings.setAvatarRadius(1);
                            info.setAvatarSettings(settings);
                            return info;
                        }
                        return null;
                    }
                })
                .setAvatarOptions(getAvatarOptions())
                .setFileIconProvider(new EaseFileIconProvider() {
                    @Override
                    public Drawable getFileIcon(String filename) {
                        return getFileDrawable(filename);
                    }
                });
    }

    private Drawable getFileDrawable(String filename) {
        if(!TextUtils.isEmpty(filename)) {
            Drawable drawable = null;
            Context context = DemoApplication.getInstance();
            Resources resources = context.getResources();
            if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_image_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_voice);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_video_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_voice);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_audio_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_voice);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_word_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_doc);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_excel_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_exl);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_pdf_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_pdf);
            }else if(EaseCompat.checkSuffix(filename, resources.getStringArray(io.agora.chat.uikit.R.array.ease_ppt_file_suffix))) {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_ppt);
            }else {
                drawable = ContextCompat.getDrawable(context, R.drawable.file_type_unknown);
            }
            return drawable;
        }
        return null;
    }

    /**
     * Unified Profile Picture Configuration
     * @return
     */
    private EaseAvatarOptions getAvatarOptions() {
        EaseAvatarOptions avatarOptions = new EaseAvatarOptions();
        avatarOptions.setAvatarShape(1);
        return avatarOptions;
    }

    public UsersManager getUsersManager() {
        if (usersManager == null) {
            usersManager = new UsersManager();
        }
        return usersManager;
    }


    /**
     * Custom settings
     * @param context
     * @return
     */
    private ChatOptions initChatOptions(Context context){
        Log.d(TAG, "init Agora Chat Options");

        ChatOptions options = new ChatOptions();
        boolean hasAppkey = checkAgoraChatAppKey(context);
        // You can set your AppKey by options.setAppKey(appkey)
        if(!hasAppkey) {
            String error = context.getString(R.string.please_check);
            EMLog.e(TAG, error);
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            return null;
        }
        // Sets whether to automatically accept friend invitations. Default is true
        options.setAcceptInvitationAlways(false);
        // Set whether read confirmation is required by the recipient
        options.setRequireAck(true);
        // Set whether confirmation of delivery is required by the recipient. Default: false
        options.setRequireDeliveryAck(true);
        // Set whether to delete chat messages when exiting (actively and passively) a group
        options.setDeleteMessagesAsExitGroup(demoModel.isDeleteMessagesAsExitGroup());
        // Set whether to automatically accept group invitations
        options.setAutoAcceptGroupInvitation(demoModel.isAutoAcceptGroupInvitation());


        /**
         * NOTE:You need to set up your own account to use the three-way push function, see the integration documentation
         */
        PushConfig.Builder builder = new PushConfig.Builder(context);

        // The FCM sender id should equals with the project_number in google-services.json
        builder.enableFCM("142290967082");
        options.setPushConfig(builder.build());

        return options;
    }

    private boolean checkAgoraChatAppKey(Context context) {
        String appPackageName = context.getPackageName();
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(appPackageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        if(ai != null) {
            Bundle metaData = ai.metaData;
            if(metaData == null){
                return false;
            }
            // read appkey
            String appKeyFromConfig = metaData.getString("EASEMOB_APPKEY");

            if (TextUtils.isEmpty(appKeyFromConfig) || !appKeyFromConfig.contains("#")) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void initPush(Context context) {
        if(EaseUIKit.getInstance().isMainProcess(context)) {
            PushHelper.getInstance().setPushListener(new PushListener() {
                @Override
                public void onError(PushType pushType, long errorCode) {
                    EMLog.e("PushClient", "Push client occur a error: " + pushType + " - " + errorCode);
                }

                @Override
                public boolean isSupportPush(PushType pushType, PushConfig pushConfig) {
                    if(pushType == PushType.FCM){
                        EMLog.d("FCM", "GooglePlayServiceCode:"+ GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context));
                        return GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
                    }
                    return super.isSupportPush(pushType, pushConfig);
                }
            });
        }
    }

    /**
     * logout
     *
     * @param unbindDeviceToken
     *            whether you need unbind your device token
     * @param callback
     *            callback
     */
    public void logout(boolean unbindDeviceToken, final CallBack callback) {
        Log.d(TAG, "logout: " + unbindDeviceToken);
        ChatClient.getInstance().logout(unbindDeviceToken, new CallBack() {

            @Override
            public void onSuccess() {
                logoutSuccess();
                //reset();
                if (callback != null) {
                    callback.onSuccess();
                }

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                Log.d(TAG, "logout: onSuccess");
                //reset();
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    /**
     * Kill current process
     */
    public void killApp() {
        List<Activity> activities = DemoApplication.getInstance().getLifecycleCallbacks().getActivityList();
        if(activities != null && !activities.isEmpty()) {
            for(Activity activity : activities) {
                activity.finish();
            }
        }
        Process.killProcess(Process.myPid());
        System.exit(0);
    }



    /**
     * Set custom logic that needs to be processed after logout
     */
    public void logoutSuccess() {
        Log.d(TAG, "logout: onSuccess");
        DemoDbHelper.getInstance(DemoApplication.getInstance()).closeDb();
    }

    /**
     * Get avatar's options
     * @return
     */
    public EaseAvatarOptions getEaseAvatarOptions() {
        return EaseUIKit.getInstance().getAvatarOptions();
    }

    public DemoModel getModel(){
        if(demoModel == null) {
            demoModel = new DemoModel(DemoApplication.getInstance());
        }
        return demoModel;
    }

    /**
     * get instance of EaseNotifier
     * @return
     */
    public EaseNotifier getNotifier(){
        return EaseUIKit.getInstance().getNotifier();
    }

    /**
     * Whether to log in automatically
     * @return
     */
    public boolean getAutoLogin() {
        return ChatClient.getInstance().getOptions().getAutoLogin();
    }

    public boolean isSDKInit() {
        return ChatClient.getInstance().isSdkInited();
    }

    /**
     * Insert data into the database
     * @param object
     */
    public void insert(Object object) {
        demoModel.insert(object);
    }

    /**
     * Update the data int the database
     * @param object
     */
    public void update(Object object) {
        demoModel.update(object);
    }

    /**
     * Get contact list
     *
     * @return
     */
    public Map<String, EaseUser> getContactList() {
        // Fetching data directly from the local database without considering too many complex scenarios
        if (isLoggedIn()) {
            contactList = demoModel.getAllUserList();
        }

        // return a empty non-null object to avoid app crash
        if(contactList == null){
            return new Hashtable<String, EaseUser>();
        }
        return contactList;
    }

    /**
     * Update contact list
     */
    public void updateContactList() {
        if(isLoggedIn()) {
            contactList = demoModel.getContactList();
        }
    }

    /**
     * Delete contact
     * @param username
     * @return
     */
    public synchronized int deleteContact(String username) {
        if(TextUtils.isEmpty(username)) {
            return 0;
        }
        DemoDbHelper helper = DemoDbHelper.getInstance(DemoApplication.getInstance());
        if(helper.getUserDao() == null) {
            return 0;
        }
        int num = helper.getUserDao().deleteUser(username);
        ChatClient.getInstance().chatManager().deleteConversation(username, false);
        getModel().deleteUsername(username, false);
        Log.e(TAG, "delete num = "+num);
        return num;
    }



    public boolean setGroupInfo(Context context, String groupId, TextView tvName, ImageView avatar) {
        return setGroupInfo(context, groupId, R.drawable.group_avatar, tvName, avatar);
    }
    public boolean setGroupInfo(Context context, String groupId, @DrawableRes int defaultAvatar, TextView tvName, ImageView avatar) {
        return GroupHelper.setGroupInfo(context, groupId, defaultAvatar, tvName, avatar);
    }

    public  String getAppVersionName(Context context) {
        String versionName=null;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

}
