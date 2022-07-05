package io.agora.chatdemo.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.model.EaseConversationSetStyle;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.contact.ContactFragment;
import io.agora.chatdemo.conversation.ChatConversationChangeListener;
import io.agora.chatdemo.conversation.ConversationListFragment;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.permission.PermissionsManager;
import io.agora.chatdemo.me.MeFragment;
import io.agora.util.EMLog;

public class MainActivity extends BaseInitActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navView;
    private Fragment mConversationListFragment, mFriendsFragment, mAboutMeFragment;
    private Fragment mCurrentFragment;
    private TextView mTvMainHomeMsg, mTvMainContactsMsg;
    private int[] badgeIds = {R.layout.badge_home, R.layout.badge_contacts};
    private int[] msgIds = {R.id.tv_main_home_msg, R.id.tv_main_contacts_msg};
    private MainViewModel mainViewModel;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    public void initView(Bundle savedInstanceState) {
        navView = findViewById(R.id.nav_view);
        //navView.setItemIconTintList(null);
        switchToHome();
        checkIfShowSavedFragment(savedInstanceState);
        addTabBadge();
    }

    public void initListener() {
        navView.setOnNavigationItemSelectedListener(this);
        mainViewModel= new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getConversationObservable().observe(this,conversation->{
            initContactsRedDot(conversation);
        });
        mainViewModel.homeUnReadObservable().observe(this,unReadCount->{
            initChatRedDot(unReadCount);
        });
        LiveDataBus messageChange =  LiveDataBus.get();
        messageChange.with(DemoConstant.NOTIFY_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.class).observe(this,this::loadData);

        messageChange.with(DemoConstant.CONVERSATION_DELETE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.CONVERSATION_READ, EaseEvent.class).observe(this, this::loadData);
    }

    private void loadData(EaseEvent easeEvent) {
        if(easeEvent==null) {
            return;
        }
        if(!easeEvent.isMessageChange()) {
            mainViewModel.getMsgConversation();
        }
        refreshConversation();
        mainViewModel.checkUnreadMsg();
    }

    private void refreshConversation() {
        if(mConversationListFragment != null && mConversationListFragment instanceof EaseConversationListFragment
                && mConversationListFragment.isAdded()) {
            ((EaseConversationListFragment) mConversationListFragment).refreshList();
        }
    }

    private void initContactsRedDot(Conversation conversation) {
        int visible;
        int unreadMsgCount = conversation.getUnreadMsgCount();
        if(unreadMsgCount>0) {
            visible=View.VISIBLE;
        }else{
            visible=View.GONE;
        }
        showContactUnReadIcon(visible);
    }

    private void initChatRedDot(String unReadCount) {
        if(!TextUtils.isEmpty(unReadCount)) {
            mTvMainHomeMsg.setVisibility(View.VISIBLE);
            mTvMainHomeMsg.setText(unReadCount);
        }else {
            mTvMainHomeMsg.setVisibility(View.GONE);
        }
    }

    public void initData() {
        mainViewModel.getMsgConversation();
        mainViewModel.checkUnreadMsg();
        DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(ChatClient.getInstance().getCurrentUser());
        checkNeedPermission();

        if(GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS){
            // set enable FCM automatic initialization
            if(!FirebaseMessaging.getInstance().isAutoInitEnabled()){
                FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
            }
            // get FCM token upload
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        EMLog.d("FCM", "Fetching FCM registration token failed:"+task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();
                    EMLog.d("FCM", token);
                    ChatClient.getInstance().sendFCMTokenToServer(token);
                }
            });
        }

    }

    private void checkNeedPermission() {
        if(!PermissionsManager.getInstance().hasPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mContext
                    , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, null);
        }
    }

    private void switchToHome() {
        if(mConversationListFragment == null) {
            mConversationListFragment = new EaseConversationListFragment.Builder()
                                            .setCustomFragment(new ConversationListFragment())
                                            .setConversationChangeListener(new ChatConversationChangeListener())
                                            .useHeader(false)
                                            .setUnreadPosition(EaseConversationSetStyle.UnreadDotPosition.RIGHT)
                                            .setUnreadStyle(EaseConversationSetStyle.UnreadStyle.NUM)
                                            .build();
        }
        replace(mConversationListFragment, "conversation");
    }

    private void switchToContacts() {
        if(mFriendsFragment == null) {
            mFriendsFragment = new ContactFragment();
        }
        replace(mFriendsFragment, "contact");
    }

    private void switchToAboutMe() {
        if(mAboutMeFragment == null) {
            mAboutMeFragment = new MeFragment();
        }
        replace(mAboutMeFragment, "me");
    }

    private void replace(Fragment fragment, String tag) {
        if(fragment != null && mCurrentFragment != fragment) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if(mCurrentFragment != null) {
                t.hide(mCurrentFragment);
            }
            mCurrentFragment = fragment;
            if(!fragment.isAdded()) {
                t.add(R.id.fl_main_fragment, fragment, tag).show(fragment).commit();
            }else {
                t.show(fragment).commit();
            }
        }
    }

    /**
     * Add custom view for Tab
     */
    private void addTabBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navView.getChildAt(0);
        BottomNavigationItemView itemTab;
        for(int i = 0; i < 2; i++) {
            itemTab = (BottomNavigationItemView) menuView.getChildAt(i);
            View badge = LayoutInflater.from(this).inflate(badgeIds[i], menuView, false);
            switch (i) {
                case 0 :
                    mTvMainHomeMsg = badge.findViewById(msgIds[0]);
                    break;
                case 1 :
                    mTvMainContactsMsg = badge.findViewById(msgIds[1]);
                    break;
            }
            itemTab.addView(badge);
        }
    }

    /**
     * Check if have fragment exited
     * @param savedInstanceState
     */
    private void checkIfShowSavedFragment(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            String tag = savedInstanceState.getString("tag");
            if(!TextUtils.isEmpty(tag)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                replace(fragment, tag);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mCurrentFragment != null) {
            outState.putString("tag", mCurrentFragment.getTag());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        boolean showNavigation = false;
        switch (menuItem.getItemId()) {
            case R.id.main_nav_home:
                switchToHome();
                showNavigation = true;
                break;
            case R.id.main_nav_contacts:
                switchToContacts();
                showNavigation = true;
                invalidateOptionsMenu();
                break;
            case R.id.main_nav_me:
                switchToAboutMe();
                showNavigation = true;
                break;
        }
        invalidateOptionsMenu();
        return showNavigation;
    }

    public void showContactUnReadIcon(int visibility) {
        mTvMainContactsMsg.setVisibility(visibility);
    }
}