package io.agora.chatdemo.general.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
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
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.util.EMLog;

public class UsersManager {
	private static final String TAG = UsersManager.class.getSimpleName();
	private EaseUser currentUser;
	private boolean isGroupsSyncedWithServer = false;
	private boolean isContactsSyncedWithServer = false;
	private boolean isBlackListSyncedWithServer = false;
	private boolean isPushConfigsWithServer = false;

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

	/**
	 * After the account is changed, call this method after the successful login to ensure that the information is correct
	 */
	public synchronized void reload() {
		String username = ChatClient.getInstance().getCurrentUser();
		currentUser = new EaseUser(username);
		String nick = getCurrentUserNick();
		currentUser.setNickname((nick != null) ? nick : username);
		currentUser.setAvatar(getCurrentUserAvatar());
	}

	public String getCurrentUserID() {
		return ChatClient.getInstance().getCurrentUser();
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
		DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(ChatClient.getInstance().getCurrentUser());
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
	public void setUserInfo(Context context, String username, TextView tvName, ImageView avatar) {
		setUserInfo(context, username, R.drawable.ease_default_avatar, tvName, avatar);
	}

	public void setUserInfo(Context context, String username, @DrawableRes int defaultAvatar, TextView tvName, ImageView avatar) {
		String name = username;
		String userAvatar= "";
		EaseUserProfileProvider userProvider = EaseUIKit.getInstance().getUserProvider();
		if(userProvider != null) {
			EaseUser user = userProvider.getUser(username);
			if(user != null) {
				if(!TextUtils.isEmpty(user.getNickname())) {
					name = user.getNickname();
				}
				userAvatar = user.getAvatar();
			}
		}else {
			if(TextUtils.equals(name, getCurrentUserID())) {
				EaseUser user = getCurrentUserInfo();
				userAvatar = user.getAvatar();
				name = user.getNickname();
			}
		}
		if(tvName != null && !TextUtils.isEmpty(name)) {
			tvName.setText(name);
		}
		if(avatar != null) {
			try {
				int resourceId = Integer.parseInt(userAvatar);
				Glide.with(context)
						.load(resourceId)
						.placeholder(defaultAvatar)
						.error(defaultAvatar)
						.into(avatar);
			} catch (NumberFormatException e) {
				Glide.with(context)
						.load(userAvatar)
						.placeholder(defaultAvatar)
						.error(defaultAvatar)
						.into(avatar);
			}
		}
	}

	public EaseUser getUserInfo(String username) {
		if(TextUtils.isEmpty(username)) {
			return null;
		}
		// To get instance of EaseUser, here we get it from the user list in memory
		// You'd better cache it if you get it from your server
		EaseUser user = null;
		if(username.equalsIgnoreCase(ChatClient.getInstance().getCurrentUser()))
			return getCurrentUserInfo();
		// If do not contains the key, will return null
		user = DemoHelper.getInstance().getContactList().get(username);
		if(user == null) {
			getUserInfoFromServer(username);
			user = new EaseUser(username);
		}
		return user;
	}
	private void getUserInfoFromServer(String username) {
		new EMContactManagerRepository().fetchUserInfoFromServer(username, new ResultCallBack<EaseUser>() {
			@Override
			public void onSuccess(EaseUser value) {
				Log.e(TAG,UIUtils.getString(R.string.fetch_userinfo_success));
				Log.e(TAG,value.toString());
				LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE)
						.postValue(EaseEvent.create(DemoConstant.CONTACT_UPDATE, EaseEvent.TYPE.CONTACT));
			}

			@Override
			public void onError(int error, String errorMsg) {
				Log.e(TAG,UIUtils.getString(R.string.fetch_userinfo_fail));
			}
		});
	}
	/**
	 * Determine if it is from the current user account of another device
	 * @param username
	 * @return
	 */
	public boolean isCurrentUserFromOtherDevice(String username) {
		if(TextUtils.isEmpty(username)) {
			return false;
		}
		if(username.contains("/") && username.contains(ChatClient.getInstance().getCurrentUser())) {
			return true;
		}
		return false;
	}
}
