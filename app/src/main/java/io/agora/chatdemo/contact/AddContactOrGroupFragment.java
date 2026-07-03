package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.databinding.FragmentAddContactOrGroupBinding;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.group.fragments.PublicGroupFragment;
import io.agora.chatdemo.group.fragments.SearchGroupFragment;

public class AddContactOrGroupFragment extends BaseInitFragment implements BottomSheetChildHelper {
    private FragmentAddContactOrGroupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddContactOrGroupBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.itemAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(new SearchContactFragment(),null);
            }
        });
        binding.itemJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(new SearchGroupFragment(),null);
            }
        });
        binding.itemPublicGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFragment(new PublicGroupFragment(),null);
            }
        });
    }

    @Override
    public int getTitleBarRightText() {
        return io.agora.chat.uikit.R.string.ease_cancel;
    }

    @Override
    public int getTitlebarRightTextColor() {
        return R.color.color_action_text;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.contact_add;
    }
}
