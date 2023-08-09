package io.agora.chatdemo.chat.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chat.uikit.widget.chatrow.EaseChatRow;
import io.agora.chatdemo.chat.chatrow.ChatRowTextTranslation;

public class ChatTextTranslationViewHolder extends EaseMessageAdapter.ViewHolder<ChatMessage> implements EaseChatRow.EaseChatRowActionCallback, ChatRowTextTranslation.translationClickListener {
    private Context context;
    private MessageListItemClickListener mItemClickListener;
    private EaseChatRow chatRow;
    private ChatMessage message;

    public ChatTextTranslationViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(params);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
        ((ChatRowTextTranslation)itemView).setTranslationOnClickListener(this);
    }

    @Override
    public void initView(View itemView) {
        this.chatRow = (EaseChatRow) itemView;
    }


    @Override
    public void setData(ChatMessage item, int position) {
        message = item;
        chatRow.setUpView(item, position, mItemClickListener, this);
        handleMessage();
    }

    @Override
    public void setDataList(List<ChatMessage> data, int position) {
        super.setDataList(data, position);
        chatRow.setTimestamp(position == 0 ? null : data.get(position - 1));
    }

    @Override
    public void onResendClick(ChatMessage message) {

    }

    @Override
    public void onBubbleClick(ChatMessage message) {

    }

    @Override
    public void onDetachedFromWindow() {

    }

    private void handleMessage() {
        if (message.direct() == ChatMessage.Direct.SEND) {
            handleSendMessage(message);
        } else if (message.direct() == ChatMessage.Direct.RECEIVE) {
            handleReceiveMessage(message);
        }
    }

    /**
     * send message
     * @param message
     */
    protected void handleSendMessage(final ChatMessage message) {
        // Update the view according to the message current status.
        getChatRow().updateView(message);
    }

    /**
     * receive message
     * @param message
     */
    protected void handleReceiveMessage(ChatMessage message) {

    }

    public Context getContext() {
        return context;
    }

    public EaseChatRow getChatRow() {
        return chatRow;
    }


    @Override
    public void onTranslationClick(View view,ChatMessage message) {
        mItemClickListener.onTranslationClick(view,message);
    }

    @Override
    public void onTranslationLongClick(View view,ChatMessage message) {

    }
}
