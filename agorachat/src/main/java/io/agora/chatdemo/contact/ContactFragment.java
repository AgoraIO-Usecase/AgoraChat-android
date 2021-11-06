package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import io.agora.chat.Conversation;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.contact.viewmodels.ContactsViewModel;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.fragments.JoinedGroupFragment;
import io.agora.chatdemo.global.BottomSheetContainerFragment;
import io.agora.chatdemo.notification.NotificationMsgFragment;

public class ContactFragment extends BaseInitFragment implements EaseTitleBar.OnRightClickListener {
    private EaseTitleBar toolbar_contact;
    private TabLayout tab_layout;
    private ViewPager2 vp_fragment;
    private int[] titles = {R.string.contact_tab_title_friends, R.string.contact_tab_title_groups, R.string.contact_tab_title_requests};
    private ArrayList<BaseInitFragment> fragments=new ArrayList();
    private ContactsViewModel contactsViewModel;
    private View redDot;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contact;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        toolbar_contact = findViewById(R.id.toolbar_contact);
        tab_layout = findViewById(R.id.tab_layout);
        vp_fragment = findViewById(R.id.vp_fragment);
        toolbar_contact.setRightImageResource(R.drawable.add);

    }

    @Override
    protected void initData() {
        super.initData();
        fragments.add(new ContactListFragment());
        fragments.add(new JoinedGroupFragment());
        fragments.add(new NotificationMsgFragment());
        setupWithViewPager();
        contactsViewModel.getMsgConversation();
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        contactsViewModel=new ViewModelProvider(this).get(ContactsViewModel.class);
        contactsViewModel.getConversationObservable().observe(this,conversation->{
            initRedDot(conversation);
        });

    }

    private void setupWithViewPager() {
        vp_fragment.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        // set adapter
        vp_fragment.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return titles.length;
            }
        });
        // set TabLayoutMediator
        TabLayoutMediator mediator = new TabLayoutMediator(tab_layout, vp_fragment, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(R.layout.layout_custom_tab_contact);
                TextView title = tab.getCustomView().findViewById(R.id.tv_tab_title);
                if(position == 0) {
                    title.setBackgroundResource(R.drawable.contact_tab_bg);
                }else if(position==2) {
                    redDot =tab.getCustomView().findViewById(R.id.tv_tab_red);
                }
                title.setText(titles[position]);
            }
        });
        // setup with viewpager2
        mediator.attach();
    }

    private void loadData(EaseEvent change) {
        if(change == null) {
            return;
        }
        contactsViewModel.getMsgConversation();
    }
    public boolean isNofificationMsgFragmentVisiable(){
        return tab_layout.getSelectedTabPosition()==2;
    }

    @Override
    protected void initListener() {
        super.initListener();
        toolbar_contact.setOnRightClickListener(this);
        //new GroupContainerFragment().show(getChildFragmentManager(),"GroupContainerFragment");
        tab_layout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getCustomView() != null) {
                    TextView title = tab.getCustomView().findViewById(R.id.tv_tab_title);
                    title.setBackgroundResource(R.drawable.contact_tab_bg);
                    ViewGroup.LayoutParams layoutParams = title.getLayoutParams();
                    layoutParams.height = (int) EaseUtils.dip2px(mContext, 28);
                    title.setGravity(Gravity.CENTER);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        vp_fragment.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                toolbar_contact.setVisibility(View.VISIBLE);
            }
        });

        LiveDataBus messageChange =  LiveDataBus.get();
        messageChange.with(DemoConstant.NOTIFY_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadData);
        messageChange.with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadData);
        messageChange.with(DemoConstant.CHAT_ROOM_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadData);
        messageChange.with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), this::loadData);
    }

    private void initRedDot(Conversation conversation) {
        int visiable;
        int unreadMsgCount = conversation.getUnreadMsgCount();
        if(!isNofificationMsgFragmentVisiable()&&unreadMsgCount>0) {
            visiable=View.VISIBLE;
        }else{
            visiable=View.GONE;
        }
        redDot.setVisibility(visiable);
    }

    @Override
    public void onRightClick(View view) {
        switch (tab_layout.getSelectedTabPosition()) {
            case  0:
            case  1:
            case  2:
                new BottomSheetContainerFragment().show(getChildFragmentManager(),"ContainerFragment");
                break;
        }
    }
}
