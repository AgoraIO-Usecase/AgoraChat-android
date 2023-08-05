package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import java.util.List;

import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.contact.fragment.ForwardContactsFragment;
import io.agora.chatdemo.contact.fragment.ForwardGroupsFragment;
import io.agora.chatdemo.databinding.FragmentForwardContactListBinding;
import io.agora.chatdemo.global.BottomSheetChildHelper;

public class ForwardContactsListFragment extends BaseInitFragment implements BottomSheetChildHelper {
    FragmentForwardContactListBinding binding;
    private int[] titles = {R.string.main_title_contacts, R.string.contact_tab_title_groups};
    private List<BaseInitFragment> fragments = new ArrayList();

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentForwardContactListBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
    }

    @Override
    protected void initData() {
        super.initData();
        ForwardContactsFragment contactsFragment = new ForwardContactsFragment();
        ForwardGroupsFragment forwardGroupsFragment = new ForwardGroupsFragment();
        contactsFragment.setArguments(getArguments());
        forwardGroupsFragment.setArguments(getArguments());
        fragments.add(contactsFragment);
        fragments.add(forwardGroupsFragment);
        setupWithViewPager();
    }

    private void setupWithViewPager() {
        binding.vpFragment.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        // set adapter
        binding.vpFragment.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), getLifecycle()) {
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
        TabLayoutMediator mediator = new TabLayoutMediator(binding.tabLayout, binding.vpFragment, new TabLayoutMediator.TabConfigurationStrategy() {
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
    public int getTitleBarRightText() {
        return R.string.dialog_action_close;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.color_action_text;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.dialog_forward_title;
    }
}
