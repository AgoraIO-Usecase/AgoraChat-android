package io.agora.chatdemo.contact;

import static io.agora.chatdemo.general.constant.DemoConstant.GROUP_MEMBER_USER;
import static io.agora.chatdemo.general.utils.ToastUtils.showToast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.contact.viewmodels.ContactDetailViewModel;
import io.agora.chatdemo.databinding.FragmentGroupMemberDetailBottomSheetBinding;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.utils.CommonUtils;
import io.agora.chatdemo.general.utils.UIUtils;


public class GroupMemberDetailBottomSheetFragment extends BaseBottomSheetFragment {

    private FragmentGroupMemberDetailBottomSheetBinding mBinding;
    private ContactDetailViewModel mViewModel;
    private EaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentGroupMemberDetailBottomSheetBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        user = (EaseUser) getArguments().getSerializable(GROUP_MEMBER_USER);
    }

    @Override
    protected void initView() {
        super.initView();
        //strange
        if(user!=null&&user.getContact()==3) {
            mBinding.itemChat.setAvatar(R.drawable.group_member_add);
            mBinding.itemChat.setTitle(CommonUtils.getString(getContext(),R.string.contact_detail_add_contact));
            mBinding.itemChat.getRightTitle().setVisibility(View.VISIBLE);
            mBinding.itemChat.setArrow(View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if(user!=null) {
            Glide.with(getContext())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ease_default_avatar)
                    .error(R.drawable.ease_default_avatar)
                    .into(mBinding.ivAvatar);
        }
        mBinding.tvNickname.setText(user.getNickname());
        mBinding.tvId.setText(getString(R.string.show_agora_chat_id, user.getUsername()));
        setTopOffset((int) (EaseUtils.getScreenInfo(getContext())[1]-UIUtils.dp2px(getContext(),425)));
    }

    @Override
    protected void initListener() {
        super.initListener();
        mViewModel = new ViewModelProvider(this).get(ContactDetailViewModel.class);
        mViewModel.getAddContact().observe(this, response -> {
            if(response.status== Status.SUCCESS) {
                showToast(getResources().getString(R.string.em_add_contact_send_successful));
                hide();
            }

        });
        mBinding.itemChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null) {
                    if (user.getContact()==0) {
                        ChatActivity.actionStart(getContext(), user.getUsername(), EaseConstant.CHATTYPE_SINGLE);
                        hide();
                    } else if (user.getContact()==3) {
                        mViewModel.addContact(user.getUsername(), getResources().getString(R.string.em_add_contact_add_a_friend));
                    }
                }
            }
        });

    }
}
