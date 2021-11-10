package io.agora.chatdemo.general.manager;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.dao.EmUserDao;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.repositories.EMContactManagerRepository;
import io.agora.chatdemo.general.repositories.EMGroupManagerRepository;
import io.agora.chatdemo.general.repositories.EMPushManagerRepository;
import io.agora.util.EMLog;

public class UserProfileManager {
	private static final String TAG = UserProfileManager.class.getSimpleName();
	/**
	 * application context
	 */
	protected Context appContext = null;

	/**
	 * init flag: test if the sdk has been inited before, we don't need to init
	 * again
	 */
	private boolean sdkInited = false;

	private boolean isSyncingContactInfosWithServer = false;

	private EaseUser currentUser;

	private boolean isGroupsSyncedWithServer = false;
	private boolean isContactsSyncedWithServer = false;
	private boolean isBlackListSyncedWithServer = false;
	private boolean isPushConfigsWithServer = false;

	public UserProfileManager() {
	}

	public synchronized boolean init(Context context) {
		if (sdkInited) {
			return true;
		}
		sdkInited = true;
		return true;
	}

	public synchronized EaseUser getCurrentUserInfo() {
		if (currentUser == null) {
			String username = ChatClient.getInstance().getCurrentUser();
			currentUser = new EaseUser(username);
			String nick = getCurrentUserNick();
			currentUser.setNickname((nick != null) ? nick : username);
			currentUser.setAvatar(getCurrentUserAvatar());
		}
		return currentUser;
	}

	public String updateUserAvatar(String avatarUrl) {
		if (avatarUrl != null) {
			setCurrentUserAvatar(avatarUrl);
		}
		return avatarUrl;
	}

	public String updateUserNickname(String nickname) {
		if (!TextUtils.isEmpty(nickname)) {
			setCurrentUserNick(nickname);
		}
		return nickname;
	}

	private void setCurrentUserNick(String nickname) {
		getCurrentUserInfo().setNickname(nickname);
		PreferenceManager.getInstance().setCurrentUserNick(nickname);
	}

	private void setCurrentUserAvatar(String avatar) {
		getCurrentUserInfo().setAvatar(avatar);
		PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
	}

	private String getCurrentUserNick() {
		return PreferenceManager.getInstance().getCurrentUserNick();
	}

	private String getCurrentUserAvatar() {
		return PreferenceManager.getInstance().getCurrentUserAvatar();
	}
	
	public void initUserInfo() {
		if(!DemoHelper.getInstance().isLoggedIn()) {
			return;
		}
		if(!isGroupsSyncedWithServer) {
			EMLog.i(TAG, "isGroupsSyncedWithServer");
			new EMGroupManagerRepository().getAllGroups(new ResultCallBack<List<Group>>() {
				@Override
				public void onSuccess(List<Group> value) {
					EMLog.i(TAG, "isGroupsSyncedWithServer success");
					EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
					LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(event);
				}

				@Override
				public void onError(int error, String errorMsg) {

				}
			});
			isGroupsSyncedWithServer = true;
		}
		if(!isContactsSyncedWithServer) {
			EMLog.i(TAG, "isContactsSyncedWithServer");
			new EMContactManagerRepository().getContactList(new ResultCallBack<List<EaseUser>>() {
				@Override
				public void onSuccess(List<EaseUser> value) {
					EmUserDao userDao = DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao();
					if(userDao != null) {
						userDao.clearUsers();
						userDao.insert(EmUserEntity.parseList(value));
					}
				}

				@Override
				public void onError(int error, String errorMsg) {

				}
			});
			isContactsSyncedWithServer = true;
		}
		if(!isBlackListSyncedWithServer) {
			EMLog.i(TAG, "isBlackListSyncedWithServer");
			new EMContactManagerRepository().getBlackContactList(null);
			isBlackListSyncedWithServer = true;
		}
		if(!isPushConfigsWithServer) {
			EMLog.i(TAG, "isPushConfigsWithServer");
			new EMPushManagerRepository().fetchPushConfigsFromServer();
			isPushConfigsWithServer = true;
		}
	}

}
