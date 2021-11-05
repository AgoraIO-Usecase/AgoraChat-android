package io.agora.chatdemo.group.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.databinding.ActivityGroupMembersBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.fragments.GroupAdminsFragment;
import io.agora.chatdemo.group.fragments.GroupAllMembersFragment;
import io.agora.chatdemo.group.fragments.GroupBlockListFragment;
import io.agora.chatdemo.group.fragments.GroupMuteListFragment;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupMembersActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener {
    private ActivityGroupMembersBinding binding;
    private int[] titles;
    private ArrayList<BaseInitFragment> fragments=new ArrayList();
    private String groupId;
    private GroupMemberAuthorityViewModel viewModel;
    private Group group;
    private int role;

    public static void actionStart(Context context, String groupId) {
        Intent starter = new Intent(context, GroupMembersActivity.class);
        starter.putExtra("group_id", groupId);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        binding = ActivityGroupMembersBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("group_id");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.titleBar.setTitle(getString(R.string.group_members_title));
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        if(GroupHelper.isOwner(group)) {
            role = DemoConstant.GROUP_ROLE_OWNER;
        }else if(GroupHelper.isAdmin(group)) {
            role = DemoConstant.GROUP_ROLE_ADMIN;
        }else {
            role = DemoConstant.GROUP_ROLE_MEMBER;
        }
        if(role == DemoConstant.GROUP_ROLE_OWNER || role == DemoConstant.GROUP_ROLE_ADMIN) {
            titles = new int[] {R.string.group_members_tab_title_all, R.string.group_members_tab_title_admin
                    , R.string.group_members_tab_title_mute, R.string.group_members_tab_title_block};
            GroupAllMembersFragment allMembersFragment = new GroupAllMembersFragment();
            Bundle bundle = new Bundle();
            bundle.putString("group_id", groupId);
            bundle.putInt("group_role", role);
            allMembersFragment.setArguments(bundle);

            GroupAdminsFragment adminsFragment = new GroupAdminsFragment();
            adminsFragment.setArguments(bundle);

            GroupMuteListFragment muteListFragment = new GroupMuteListFragment();
            muteListFragment.setArguments(bundle);

            GroupBlockListFragment blockListFragment = new GroupBlockListFragment();
            blockListFragment.setArguments(bundle);

            fragments.add(allMembersFragment);
            fragments.add(adminsFragment);
            fragments.add(muteListFragment);
            fragments.add(blockListFragment);
        }else {
            titles = new int[] {R.string.group_members_tab_title_all, R.string.group_members_tab_title_admin};
            GroupAllMembersFragment allMembersFragment = new GroupAllMembersFragment();
            Bundle bundle = new Bundle();
            bundle.putString("group_id", groupId);
            bundle.putInt("group_role", role);
            allMembersFragment.setArguments(bundle);

            GroupAdminsFragment adminsFragment = new GroupAdminsFragment();
            adminsFragment.setArguments(bundle);

            fragments.add(allMembersFragment);
            fragments.add(adminsFragment);
        }


        setupWithViewPager();
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.titleBar.setOnRightClickListener(this);
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

    private void setupWithViewPager() {
        binding.vpFragment.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        // set adapter
        binding.vpFragment.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
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
                title.setBackgroundResource(R.drawable.contact_tab_bg);
                title.setText(titles[position]);
            }
        });
        // setup with viewpager2
        mediator.attach();
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        viewModel.getGroupManagersObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    group = ChatClient.getInstance().groupManager().getGroup(groupId);
                    if(group == null) {
                        return;
                    }
                    binding.titleBar.setTitle(getString(R.string.group_members_title)+" (" + group.getMemberCount() +")");
                    if(GroupHelper.isAdmin(group) || GroupHelper.isOwner(group)) {
                        viewModel.getMuteMembers(groupId);
                    }
                }
            });
        });
        viewModel.getGroupManagers(groupId);
    }

    @Override
    public void onRightClick(View view) {

    }
}
