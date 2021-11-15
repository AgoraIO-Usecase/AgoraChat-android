package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseListFragment;
import io.agora.chatdemo.general.widget.EaseSearchEditText;

public abstract class SearchFragment<T> extends BaseListFragment<T> {
    protected SmartRefreshLayout srlContactRefresh;
    protected EaseSearchEditText etSearch;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        srlContactRefresh = findViewById(R.id.srl_contact_refresh);
        etSearch = findViewById(R.id.et_search);
    }

    @Override
    protected RecyclerView initRecyclerView() {
        return findViewById(R.id.recycleview);
    }

    @Override
    protected void initListener() {
        super.initListener();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchContent = s.toString().trim();
                searchText(searchContent);
            }
        });
    }

    protected abstract void searchText(String content) ;

}
