package io.agora.chatdemo.contact;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.chat.uikit.domain.EaseUser;
import io.agora.chatdemo.contact.viewmodels.ContactsViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;

public class ContactListFragment extends BaseContactListFragment<EaseUser> {
    private ContactsViewModel mViewModel;

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        mViewModel.getContactObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    srlContactRefresh.setRefreshing(false);
                    mListAdapter.setData(data);
                }

                @Override
                public void onLoading(@Nullable List<EaseUser> data) {
                    super.onLoading(data);
                    mListAdapter.setData(data);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> srlContactRefresh.setRefreshing(false));
                }
            });
        });
        mViewModel.resultObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    mViewModel.loadContactList(false);
                }
            });
        });

        mViewModel.deleteObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    mViewModel.loadContactList(false);
                }
            });
        });

        mViewModel.messageChangeObservable().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                mViewModel.loadContactList(false);
            }
        });

        mViewModel.messageChangeObservable().with(DemoConstant.REMOVE_BLACK, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                mViewModel.loadContactList(true);
            }
        });


        mViewModel.messageChangeObservable().with(DemoConstant.CONTACT_ADD, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                mViewModel.loadContactList(false);
            }
        });


        mViewModel.messageChangeObservable().with(DemoConstant.CONTACT_DELETE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                mViewModel.loadContactList(false);
            }
        });

        mViewModel.messageChangeObservable().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
                mViewModel.loadContactList(false);
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        mViewModel.loadContactList(true);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        mViewModel.loadContactList(true);
    }

    @Override
    public void onItemClick(View view, int position) {
        ContactDetailActivity.actionStart(mContext, mListAdapter.getData().get(position).getUsername());
    }

}
