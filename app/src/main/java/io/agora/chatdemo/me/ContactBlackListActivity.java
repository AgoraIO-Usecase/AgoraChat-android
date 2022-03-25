package io.agora.chatdemo.me;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.contact.viewmodels.ContactsListViewModel;
import io.agora.chatdemo.databinding.ActivityContactBlackListBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.dialog.SelectDialog;
import io.agora.chatdemo.general.models.SelectDialogItemBean;

public class ContactBlackListActivity extends BaseInitActivity implements OnRefreshListener, EaseTitleBar.OnBackPressListener, OnItemClickListener {

    private ActivityContactBlackListBinding mBinding;

    private ContactListAdapter mContactListAdapter;
    private ContactBlackListModel mBlackListViewModel;
    private ContactsListViewModel mContactViewModel;

    private List<SelectDialogItemBean> mSelectDialogItemBeans;
    private final static int MENU_UNBLOCK_CONTACT = 1;
    private final static int MENU_DELETE_CONTACT = 2;
    private EaseUser mSelectUser;
    private boolean mDeleteUser;
    private List<EaseUser> mLastData;
    private String mSearchContent;

    @Override
    protected View getContentView() {
        mBinding = ActivityContactBlackListBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);

        mBinding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mContactListAdapter = new ContactListAdapter();
        mContactListAdapter.hideEmptyView(true);
        mBinding.rvList.setAdapter(mContactListAdapter);

        mBinding.searchBlack.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchBlackList(s.toString().trim());
            }
        });
    }

    private void searchBlackList(String content) {
        mSearchContent = content;
        if (TextUtils.isEmpty(content)) {
            mContactListAdapter.setData(mLastData);
        } else {
            List<EaseUser> userList = new ArrayList<>(mLastData);
            Iterator<EaseUser> iterator = userList.iterator();
            EaseUser user;
            while (iterator.hasNext()) {
                user = iterator.next();
                if (!user.getUsername().contains(content)) {
                    iterator.remove();
                }
            }

            mContactListAdapter.setData(userList);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.titleBar.setOnBackPressListener(this);
        mBinding.srlRefresh.setOnRefreshListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mDeleteUser = false;
        mLastData = new ArrayList<>();

        mContactViewModel = new ViewModelProvider(this).get(ContactsListViewModel.class);
        mBlackListViewModel = new ViewModelProvider(this).get(ContactBlackListModel.class);
        mBlackListViewModel.blackObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    mLastData.clear();
                    mLastData.addAll(data);
                    searchBlackList(mSearchContent);
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    finishRefresh();
                }
            });
        });
        mBlackListViewModel.resultObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    if (!mDeleteUser) {
                        // delete user don't tip
                        showToast(R.string.contact_move_out_blacklist_success);
                    }
                    LiveDataBus.get().with(DemoConstant.REMOVE_BLACK).postValue(EaseEvent.create(DemoConstant.REMOVE_BLACK, EaseEvent.TYPE.CONTACT));
                }
            });
        });

        mContactViewModel.deleteObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    mBlackListViewModel.getBlackList();
                }
            });
        });

        LiveDataBus.get().with(DemoConstant.REMOVE_BLACK, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mBlackListViewModel.getBlackList();
            }
        });

        LiveDataBus.get().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                mBlackListViewModel.getBlackList();
            }
        });

        mBlackListViewModel.getBlackList();

        mContactListAdapter.setOnItemClickListener(this);
        initSelectDialogData();
    }

    private void initSelectDialogData() {
        mSelectDialogItemBeans = new ArrayList<>(2);

        SelectDialogItemBean bean;
        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.contact_unblock));
        bean.setAlert(false);
        bean.setIcon(R.drawable.contact_unblock);
        bean.setId(MENU_UNBLOCK_CONTACT);
        mSelectDialogItemBeans.add(bean);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.contact_detail_delete_contact));
        bean.setAlert(true);
        bean.setId(MENU_DELETE_CONTACT);
        bean.setIcon(R.drawable.contact_delete);
        mSelectDialogItemBeans.add(bean);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onItemClick(View view, int position) {
        mSelectUser = mContactListAdapter.getItem(position);
        SelectDialog dialog = new SelectDialog(mContext);
        dialog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SelectDialogItemBean bean = mSelectDialogItemBeans.get(position);
                executeSelectDialogAction(bean);
            }
        });
        dialog.setData(mSelectDialogItemBeans);
        dialog.setTitle(mSelectUser.getUsername());
        dialog.init();
        new EaseAlertDialog.Builder<SelectDialog>(mContext)
                .setCustomDialog(dialog)
                .setFullWidth()
                .setGravity(Gravity.BOTTOM)
                .setFromBottomAnimation()
                .show();
    }

    private void executeSelectDialogAction(SelectDialogItemBean bean) {
        mDeleteUser = false;
        switch (bean.getId()) {
            case MENU_UNBLOCK_CONTACT:
                new SimpleDialog.Builder(mContext)
                        .setContent(R.string.contact_unblock_tip)
                        .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                unBlock(mSelectUser);
                            }
                        })
                        .showCancelButton(true)
                        .show();
                break;
            case MENU_DELETE_CONTACT:
                new SimpleDialog.Builder(mContext)
                        .setContent(R.string.contact_detail_delete_title)
                        .setOnConfirmClickListener(R.string.group_file_delete_confirm_text, new SimpleDialog.OnConfirmClickListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                deleteUser(mSelectUser);
                            }
                        })
                        .setConfirmColor(R.color.contact_color_block)
                        .showCancelButton(true)
                        .show();
                break;
        }
    }

    private void unBlock(EaseUser user) {
        if (null == user) {
            return;
        }

        mBlackListViewModel.removeUserFromBlackList(user.getUsername());
    }

    private void deleteUser(EaseUser user) {
        if (null == user) {
            return;
        }
        mDeleteUser = true;
        mBlackListViewModel.removeUserFromBlackList(user.getUsername());
        mContactViewModel.deleteContact(user.getUsername());
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        mBlackListViewModel.getBlackList();
    }

    private void finishRefresh() {
        mBinding.srlRefresh.finishRefresh();
    }
}