package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.general.widget.SwitchItemView;

public class NewGroupSettingFragment extends BaseInitFragment implements BottomSheetChildHelper, SwitchItemView.OnCheckedChangeListener {
    private static final int DEFAULT_GROUP_MAX_MEMBERS = 200;
    private EditText edtDesc;
    private EditText edtGroupName;
    private EditText edtGroupNumber;
    private TextView tvCount;
    private SwitchItemView swToPublic;
    private SwitchItemView swInvite;
    private int maxUsers = 200;
    private static final int MAX_GROUP_USERS = 3000;
    private static final int MIN_GROUP_USERS = 3;
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        edtGroupName = findViewById(R.id.edt_group_name);
        edtGroupNumber = findViewById(R.id.edt_mumber_num);
        edtDesc=findViewById(R.id.edt_desc);
        tvCount=findViewById(R.id.tv_count);
        swToPublic=findViewById(R.id.swi_to_public);
        swInvite=findViewById(R.id.swi_allow_invite);
    }

    @Override
    protected void initListener() {
        super.initListener();
        edtDesc.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                tvCount.setText(s.length()+"/500");
                if(s.length()==0) {
                    tvCount.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_cccccc));
                }
            }
        });
        swToPublic.setOnCheckedChangeListener(this);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_group_setting;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.group_new_group;
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.group_next;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.color_light_gray_999999;
    }

    @Override
    public boolean onTitlebarRightTextViewClick() {

        String groupName = edtGroupName.getText().toString().trim();
        if(TextUtils.isEmpty(groupName)) {
            ToastUtils.showToast(R.string.group_new_name_cannot_be_empty);
            return true;
        }
        String memberNumber = edtGroupNumber.getText().toString().trim();
        if(TextUtils.isEmpty(memberNumber)) {
            maxUsers = DEFAULT_GROUP_MAX_MEMBERS;
        }else {
            try {
                maxUsers = Integer.parseInt(memberNumber);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if(maxUsers < MIN_GROUP_USERS || maxUsers > MAX_GROUP_USERS) {
                showToast(R.string.group_new_member_limit);
                return true;
            }
        }
        String desc = edtDesc.getText().toString();
        String reason = getString(R.string.group_new_invite_join_group, DemoHelper.getInstance().getUsersManager().getCurrentUserID(), groupName);
        NewGroupSelectContactsFragment fragment = new NewGroupSelectContactsFragment();
        Bundle bundle=new Bundle();
        bundle.putString(DemoConstant.GROUP_NAME,groupName);
        bundle.putString(DemoConstant.GROUP_DESC,desc);
        bundle.putString(DemoConstant.GROUP_REASON,reason);
        bundle.putBoolean(DemoConstant.GROUP_PUBLIC,swToPublic.getSwitch().isChecked());
        bundle.putBoolean(DemoConstant.GROUP_ALLOW_INVITE,swInvite.getSwitch().isChecked());
        bundle.putInt(DemoConstant.GROUP_MAX_USERS,maxUsers);
        fragment.setArguments(bundle);
        startFragment(fragment,null);
        return true;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }

    private void checkGroupInfo() {

    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        if(buttonView==swToPublic) {
            if(isChecked) {
                swInvite.getTvTitle().setText(R.string.group_authoried_to_join);
            }else{
                swInvite.getTvTitle().setText(R.string.group_allow_members_to_invite);
            }
        }
    }
}
