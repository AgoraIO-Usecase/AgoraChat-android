package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.group.GroupContactManageFragment;
import io.agora.chatdemo.group.GroupContainerFragment;
import io.agora.chatdemo.notification.NotificationMsgFragment;

public class ContactFragment extends BaseInitFragment implements EaseTitleBar.OnRightClickListener {
    private EaseTitleBar toolbar_contact;
    private TabLayout tab_layout;
    private ViewPager2 vp_fragment;
    private int[] titles = {R.string.contact_tab_title_friends, R.string.contact_tab_title_groups, R.string.contact_tab_title_requests};
    private ArrayList<BaseInitFragment> fragments=new ArrayList();

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
        fragments.add(new GroupContactManageFragment());
        fragments.add(new NotificationMsgFragment());
        setupWithViewPager();
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
                }
                title.setText(titles[position]);
            }
        });
        // setup with viewpager2
        mediator.attach();
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
    }

    @Override
    public void onRightClick(View view) {
        switch (tab_layout.getSelectedTabPosition()) {
            case  0:
                break;
            case  1:
                new GroupContainerFragment().show(getChildFragmentManager(),"GroupContainerFragment");
                break;
            case  2:
                break;
        }
    }
}
