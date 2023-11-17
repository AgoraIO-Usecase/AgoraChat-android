package io.agora.chatdemo.contact.fragment;

import android.os.Bundle;
import android.view.View;


import org.json.JSONException;
import org.json.JSONObject;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.contact.ContactListFragment;
import io.agora.chatdemo.contact.adapter.ForwardContactsAdapter;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;

public class ForwardContactsFragment extends ContactListFragment {
    private boolean isFromChatThread = false;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            isFromChatThread = bundle.getBoolean("isFromChatThread");
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.GONE);
        sideBarContact.setVisibility(View.GONE);
    }

    @Override
    protected void initPresenceViewModel() {

    }

    @Override
    protected EaseBaseRecyclerViewAdapter<EaseUser> initAdapter() {
        return new ForwardContactsAdapter();
    }

    @Override
    protected void checkView(String content) {

    }

    @Override
    protected void initListener() {
        super.initListener();
        if(mListAdapter instanceof ForwardContactsAdapter) {
            ((ForwardContactsAdapter) mListAdapter).setOnForwardSendClickListener((view, to) -> {
                LiveDataBus.get().with(DemoConstant.EVENT_SEND_COMBINE).postValue(EaseEvent.create(DemoConstant.EVENT_SEND_COMBINE, EaseEvent.TYPE.MESSAGE, createJsonObject(to)));
            });
        }
    }

    private String createJsonObject(String to) {
        JSONObject object = new JSONObject();
        try {
            object.put("to", to);
            object.put("chatType", ChatMessage.ChatType.Chat.name());
            object.put("isFromChatThread", isFromChatThread);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
