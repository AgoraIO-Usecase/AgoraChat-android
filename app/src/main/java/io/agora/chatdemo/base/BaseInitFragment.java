package io.agora.chatdemo.base;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class BaseInitFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getLayoutId();
        View view = null;
        if(layoutId != 0) {
            view = inflater.inflate(layoutId, container, false);
        }else {
            view = getContentView(inflater,container,savedInstanceState);
        }
        initArgument();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        initViewModel();
        initListener();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    /**
     * Return the layout ID
     * @return
     */
    protected int getLayoutId(){
        return 0;
    };

    /**
     * Return the layout view
     * @return
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    /**
     * Initialize the params
     */
    protected void initArgument() {}

    /**
     * Initialize the views
     * @param savedInstanceState
     */
    protected void initView(Bundle savedInstanceState) {
        Log.e("TAG", "fragment = "+this.getClass().getSimpleName());
    }


    /**
     * Initialize the viewmodels
     */
    protected void initViewModel() {}

    /**
     * Initialize the listeners
     */
    protected void initListener() {}

    /**
     * Initialize the data
     */
    protected void initData() {}

    /**
     * Call it after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T findViewById(@IdRes int id) {
        return getView().findViewById(id);
    }
}
