package io.agora.chatdemo.chat;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.databinding.FragmentPickAtLayoutBinding;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.fragments.GroupAllMembersFragment;


public class PickAtUserDialogFragment extends BaseBottomSheetFragment {

    private FragmentPickAtLayoutBinding binding;
    private String conversationId;
    private int role;
    private Group group;

    private FrameLayout frameLayout;
    private TextView tvTitle;
    private TextView tvCancel;
    private onPickAtSelectListener listener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPickAtLayoutBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            conversationId = bundle.getString(DemoConstant.EXTRA_CONVERSATION_ID);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        frameLayout = findViewById(R.id.frame_layout);
        tvTitle = findViewById(R.id.tv_title);
        tvCancel = findViewById(R.id.tv_cancel);

        group = ChatClient.getInstance().groupManager().getGroup(conversationId);
        if(GroupHelper.isOwner(group)) {
            role = DemoConstant.GROUP_ROLE_OWNER;
        }else if(GroupHelper.isAdmin(group)) {
            role = DemoConstant.GROUP_ROLE_ADMIN;
        }else {
            role = DemoConstant.GROUP_ROLE_MEMBER;
        }

        GroupAllMembersFragment fragment = new GroupAllMembersFragment();
        fragment.setPickAtSelectListener(username -> {
            if (listener != null){
                listener.getPickAtItem(username);
            }
            hide();
        });
        FragmentManager fragmentManager = getChildFragmentManager();

        Bundle bundle = new Bundle();
        bundle.putString("group_id", conversationId);
        bundle.putInt("group_role", role);
        bundle.putBoolean("group_at", true);

        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frame_layout, fragment);
        fragmentTransaction.commit();


    }

    @Override
    protected void initListener() {
        super.initListener();
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public interface onPickAtSelectListener{
        void getPickAtItem(String username);
    }

    public void setPickAtSelectListener(onPickAtSelectListener listener){
        this.listener = listener;
    }
}
