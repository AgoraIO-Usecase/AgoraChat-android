package io.agora.chatdemo.group;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.base.BottomSheetChildHelper;

/**
 * Created by 许成谱 on 2021/10/29 0029 20:16.
 * qq:1550540124
 */
public class NewGroupFragment extends BaseInitFragment implements BottomSheetChildHelper {
    private EditText edtDesc;
    private TextView tvCount;
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        edtDesc=findViewById(R.id.edt_desc);
        tvCount=findViewById(R.id.tv_count);
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



    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_group;
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
        checkGroupInfo();
        Bundle bundle=new Bundle();
        //ToDo
        startFrament(new NewGroupSettingFragment(),null);
        return true;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }

    @Override
    public Fragment getBottomSheetContainerFragment() {
        return getParentFragment();
    }

    private void checkGroupInfo() {
//        String groupName = itemGroupName.getTvContent().getText().toString().trim();
//        if(TextUtils.isEmpty(groupName)) {
//            new SimpleDialogFragment.Builder(mContext)
//                    .setTitle(R.string.em_group_new_name_cannot_be_empty)
//                    .show();
//            return;
//        }
//        if(maxUsers < MIN_GROUP_USERS || maxUsers > MAX_GROUP_USERS) {
//            showToast(R.string.em_group_new_member_limit);
//            return;
//        }
//        String desc = itemGroupProfile.getTvContent().getText().toString();
//        EMGroupOptions option = new EMGroupOptions();
//        option.maxUsers = maxUsers;
//        option.inviteNeedConfirm = true;
//        String reason = getString(R.string.em_group_new_invite_join_group, DemoHelper.getInstance().getCurrentUser(), groupName);
//        if(itemSwitchPublic.getSwitch().isChecked()){
//            option.style = itemSwitchInvite.getSwitch().isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
//        }else{
//            option.style = itemSwitchInvite.getSwitch().isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
//        }
//        if(newmembers == null) {
//            newmembers = new String[]{};
//        }
//        viewModel.createGroup(groupName, desc, newmembers, reason, option);
    }
}
