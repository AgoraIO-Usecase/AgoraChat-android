package io.agora.chatdemo.main;

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

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.contact.ContactFragment;
import io.agora.chatdemo.conversation.ConversationListFragment;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.me.AboutMeFragment;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navView;
    private Fragment mConversationListFragment, mFriendsFragment, mAboutMeFragment;
    private Fragment mCurrentFragment;
    private TextView mTvMainHomeMsg, mTvMainContactsMsg;
    private int[] badgeIds = {R.layout.badge_home, R.layout.badge_contacts};
    private int[] msgIds = {R.id.tv_main_home_msg, R.id.tv_main_contacts_msg};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(savedInstanceState);
        initListener();
        initData();
    }

    private void initView(Bundle savedInstanceState) {
        navView = findViewById(R.id.nav_view);
        //navView.setItemIconTintList(null);
        switchToHome();
        checkIfShowSavedFragment(savedInstanceState);
        addTabBadge();
    }

    private void initListener() {
        navView.setOnNavigationItemSelectedListener(this);
    }

    private void initData() {
        DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(ChatClient.getInstance().getCurrentUser());
    }

    private void switchToHome() {
        if(mConversationListFragment == null) {
            mConversationListFragment = new ConversationListFragment();
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
            mAboutMeFragment = new AboutMeFragment();
        }
        replace(mAboutMeFragment, "me");
    }

    private void replace(Fragment fragment, String tag) {
        if(mCurrentFragment != fragment) {
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

    private void showContactUnReadIcon(boolean isShow) {
        mTvMainContactsMsg.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}