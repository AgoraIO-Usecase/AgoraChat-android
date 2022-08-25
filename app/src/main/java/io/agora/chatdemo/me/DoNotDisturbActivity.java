package io.agora.chatdemo.me;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ActivityDoNotDisturbBinding;

import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_ID;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_CHAT;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_GROUP;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_THREAD;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_USER;
import static io.agora.chatdemo.general.constant.DemoConstant.SILENT_DURATION;

public class DoNotDisturbActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, EaseTitleBar.OnRightClickListener, AdapterView.OnItemClickListener{

    private ActivityDoNotDisturbBinding mBinding;
    private List<DoNotDisturbAdapter.SelectItem> itemList = new ArrayList<>();
    private DoNotDisturbAdapter adapter;
    private int detailType;
    private String detailId;

    @Override
    protected View getContentView() {
        mBinding = ActivityDoNotDisturbBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setTextStyle(mBinding.titleBar.getTitle(),Typeface.BOLD);
        setTextStyle(mBinding.titleBar.getRightText(),Typeface.BOLD);
        itemList.add(new DoNotDisturbAdapter.SelectItem(this.getResources().getString(R.string.not_disturb_15_min), 15));
        itemList.add(new DoNotDisturbAdapter.SelectItem(this.getResources().getString(R.string.not_disturb_1_hour), 60));
        itemList.add(new DoNotDisturbAdapter.SelectItem(this.getResources().getString(R.string.not_disturb_8_hours), 480));
        itemList.add(new DoNotDisturbAdapter.SelectItem(this.getResources().getString(R.string.not_disturb_24_hours), 1440));
        itemList.add(new DoNotDisturbAdapter.SelectItem(this.getResources().getString(R.string.not_disturb_8_tomorrow), -1));
        adapter = new DoNotDisturbAdapter(mContext, itemList);
        mBinding.listView.setAdapter(adapter);

        switch (detailType){
            case DETAIL_TYPE_USER:
                mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_do_not_disturb));
                break;
            case DETAIL_TYPE_THREAD:
                mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_mute_thread));
                break;
            case DETAIL_TYPE_GROUP:
                mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_mute_group));
                break;
            case DETAIL_TYPE_CHAT:
                mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_mute_contact));
                break;
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.titleBar.setOnBackPressListener(this);
        mBinding.titleBar.setOnRightClickListener(this);
        mBinding.listView.setOnItemClickListener(this);

    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        detailType = intent.getIntExtra(DETAIL_TYPE, 0);
        detailId = intent.getStringExtra(DETAIL_ID);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        adapter.setSelectedIndex(id);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRightClick(View view) {
        long index = adapter.getSelectedIndex();
        int duration = itemList.get((int)index).duration;
        Intent intent = new Intent();
        if(duration < 0){
            intent.putExtra(SILENT_DURATION,parseSilentDurationUntilAM());
        } else {
            intent.putExtra(SILENT_DURATION,duration);
        }
        setResult(RESULT_OK, intent);
        onBackPressed();
    }

    private int parseSilentDurationUntilAM(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH/mm");
        String dateStr = dateFormat.format(date);
        String[] time = dateStr.split("/");
        return 60 * 32 - Integer.parseInt(time[0]) * 60 - Integer.parseInt(time[1]);
    }
}