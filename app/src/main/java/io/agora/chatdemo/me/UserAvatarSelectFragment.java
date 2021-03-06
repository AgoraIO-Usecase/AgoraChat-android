package io.agora.chatdemo.me;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.base.BaseListFragment;
import io.agora.chatdemo.databinding.FragmentUserAvaterSelectBinding;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.PreferenceManager;
import io.agora.chatdemo.general.repositories.TestAvatarRepository;
import io.agora.chatdemo.general.utils.UIUtils;


public class UserAvatarSelectFragment extends BaseListFragment<Integer> implements SwipeRefreshLayout.OnRefreshListener {

    private UserAvatarSelectViewModel mViewModel;
    private FragmentUserAvaterSelectBinding mBinding;
    private GridLayoutManager gridLayoutManager;

    @Override
    protected void initArgument() {
        super.initArgument();
        gridLayoutManager = new GridLayoutManager(mContext, 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mRecyclerView.addItemDecoration(new AvatarSelectItemDecoration(UIUtils.dp2px(mContext,7)));
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(this).get(UserAvatarSelectViewModel.class);
    }

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentUserAvaterSelectBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView initRecyclerView() {
        return mBinding.avatarList;
    }

    @Override
    protected void initData() {
        super.initData();
        mListAdapter.setData(new TestAvatarRepository().getAvatarList());
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.srlContactRefresh.setOnRefreshListener(this);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return gridLayoutManager;
    }

    @Override
    protected EaseBaseRecyclerViewAdapter<Integer> initAdapter() {
        return new AvatarSelectAdapter();
    }

    @Override
    public void onItemClick(View view, int position) {
        Integer headImg = mListAdapter.getData().get(position);
        PreferenceManager.getInstance().setCurrentUserAvatar(headImg.toString());
        DemoHelper.getInstance().getUsersManager().updateUserAvatar(headImg.toString());
        EaseEvent event = EaseEvent.create(DemoConstant.CURRENT_USER_INFO_CHANGE, EaseEvent.TYPE.CONTACT);
        event.message = headImg.toString();
        LiveDataBus.get().with(DemoConstant.CURRENT_USER_INFO_CHANGE).postValue(event);
        Intent intent = getActivity().getIntent();
        intent.putExtra("headImage", headImg);
        getActivity().setResult(RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onRefresh() {
        //just it temp
        mListAdapter.setData(new TestAvatarRepository().getAvatarList());
        mBinding.srlContactRefresh.setEnabled(false);
    }
}