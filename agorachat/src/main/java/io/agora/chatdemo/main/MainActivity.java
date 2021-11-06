package io.agora.chatdemo.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.model.EaseConversationSetStyle;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.contact.ContactFragment;
import io.agora.chatdemo.conversation.ConversationListFragment;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.permission.PermissionsManager;
import io.agora.chatdemo.me.MeFragment;

public class MainActivity extends BaseInitActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navView;
    private Fragment mConversationListFragment, mFriendsFragment, mAboutMeFragment;
    private Fragment mCurrentFragment;
    private TextView mTvMainHomeMsg, mTvMainContactsMsg;
    private int[] badgeIds = {R.layout.badge_home, R.layout.badge_contacts};
    private int[] msgIds = {R.id.tv_main_home_msg, R.id.tv_main_contacts_msg};
    private MainViewModel mainViewModel;

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
            initRedDot(conversation);
        });

        LiveDataBus messageChange =  LiveDataBus.get();
        messageChange.with(DemoConstant.NOTIFY_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(this, this::loadData);
        messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, this::loadData);

    }

    private void loadData(EaseEvent easeEvent) {
        mainViewModel.getMsgConversation();
    }

    private void initRedDot(Conversation conversation) {
        int visiable;
        int unreadMsgCount = conversation.getUnreadMsgCount();
        if(unreadMsgCount>0) {
            visiable=View.VISIBLE;
        }else{
            visiable=View.GONE;
        }
        showContactUnReadIcon(visiable);
    }

    public void initData() {

        mainViewModel.getMsgConversation();
        DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(ChatClient.getInstance().getCurrentUser());
        checkNeedPermission();

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
                                            .setUseHeader(true)
                                            .setHeaderTitle(getString(R.string.main_title_home))
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

    private void showMainUnReadMsg(String unReadCount) {
        if(!TextUtils.isEmpty(unReadCount)) {
            mTvMainHomeMsg.setVisibility(View.VISIBLE);
            mTvMainHomeMsg.setText(unReadCount);
        }else {
            mTvMainHomeMsg.setVisibility(View.GONE);
        }
    }

    public void showContactUnReadIcon(int visibility) {
        mTvMainContactsMsg.setVisibility(visibility);
    }
}