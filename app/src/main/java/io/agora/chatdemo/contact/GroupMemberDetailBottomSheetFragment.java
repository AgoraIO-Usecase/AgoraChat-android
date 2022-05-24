package io.agora.chatdemo.contact;

import static io.agora.chatdemo.general.constant.DemoConstant.GROUP_MEMBER_USER;
import static io.agora.chatdemo.general.utils.ToastUtils.showToast;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.Presence;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EasePresenceUtil;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.contact.viewmodels.ContactDetailViewModel;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.databinding.FragmentGroupMemberDetailBottomSheetBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.UIUtils;


public class GroupMemberDetailBottomSheetFragment extends BaseBottomSheetFragment {

    private FragmentGroupMemberDetailBottomSheetBinding mBinding;
    private ContactDetailViewModel mViewModel;
    private EaseUser user;
    private PresenceViewModel presenceViewModel;

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
            mBinding.itemChat.setTitle(UIUtils.getString(getContext(),R.string.contact_detail_add_contact));
            mBinding.itemChat.getRightTitle().setVisibility(View.VISIBLE);
            mBinding.itemChat.getRightTitle().setText(UIUtils.getString(getContext(),R.string.contact_add));
            mBinding.itemChat.setArrowVisiable(View.GONE);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if(user!=null) {
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, TextUtils.isEmpty(user.getNickname())?
                    user.getNickname():user.getUsername(), mBinding.tvNickname, mBinding.ivUserAvatar);
        }
        mBinding.tvId.setText(getString(R.string.show_agora_chat_id, user.getUsername()));
        setTopOffset((int) (EaseUtils.getScreenInfo(getContext())[1]-UIUtils.dp2px(getContext(),387)));
    }

    @Override
    protected void initListener() {
        super.initListener();
        mViewModel = new ViewModelProvider(this).get(ContactDetailViewModel.class);
        presenceViewModel=new ViewModelProvider(this).get(PresenceViewModel.class);
        presenceViewModel.presencesObservable().observe(this,response->{
            parseResource(response, new OnResourceParseCallback<List<Presence>>() {
                @Override
                public void onSuccess(@Nullable List<Presence> data) {
                    updatePresence();
                }
            });
        });
        mViewModel.getAddContact().observe(this, response -> {
            if(response.status== Status.SUCCESS) {
                showToast(getResources().getString(R.string.add_contact_send_successful));
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
                        mViewModel.addContact(user.getUsername(), getResources().getString(R.string.add_contact_add_a_friend));
                    }
                }
            }
        });

        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(((BaseActivity)mContext), event -> {
            updatePresence();
        });

    }

    private void updatePresence() {
        Presence presence = DemoHelper.getInstance().getPresences().get(user.getUsername());
        mBinding.ivPresence.setImageResource(EasePresenceUtil.getPresenceIcon(mContext,presence));
    }
}
