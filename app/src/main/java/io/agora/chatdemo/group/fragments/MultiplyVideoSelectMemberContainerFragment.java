package io.agora.chatdemo.group.fragments;

import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import io.agora.chatdemo.av.CallInviteUsersActivity;
import io.agora.chatdemo.global.BottomSheetContainerFragment;

public class MultiplyVideoSelectMemberContainerFragment extends BottomSheetContainerFragment {
    @NonNull
    @Override
    protected Fragment getChildFragment() {
        MultiplyVideoSelectMemberChildFragment fragment = new MultiplyVideoSelectMemberChildFragment();
        fragment.setArguments(getArguments());
        return fragment;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        FragmentActivity activity = getActivity();
        if(activity!=null&&activity instanceof CallInviteUsersActivity) {
            activity.finish();
        }
    }
}
