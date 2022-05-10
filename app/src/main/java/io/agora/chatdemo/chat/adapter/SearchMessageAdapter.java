package io.agora.chatdemo.chat.adapter;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.util.Date;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chat.uikit.utils.EaseDateUtils;
import io.agora.chat.uikit.utils.EaseEditTextUtils;
import io.agora.chat.uikit.utils.EaseSmileUtils;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.R;

public class SearchMessageAdapter extends EaseBaseRecyclerViewAdapter<ChatMessage> {
    private String keyword;

    @Override
    public MessageViewHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_row_chat_history, parent, false);
        return new MessageViewHolder(view);
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    private class MessageViewHolder extends ViewHolder<ChatMessage> {
        private EaseImageView avatar;
        private TextView name;
        private TextView time;
        private ImageView msg_state;
        private TextView mentioned;
        private TextView message;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            avatar = findViewById(R.id.avatar);
            name = findViewById(R.id.name);
            time = findViewById(R.id.time);
            msg_state = findViewById(R.id.msg_state);
            mentioned = findViewById(R.id.mentioned);
            message = findViewById(R.id.message);
        }

        @Override
        public void setData(ChatMessage item, int position) {
            ChatMessage.ChatType chatType = item.getChatType();
            time.setText(EaseDateUtils.getTimestampString(mContext, new Date(item.getMsgTime())));
            int defaultAvatar = 0;
            if (chatType == ChatMessage.ChatType.GroupChat || chatType == ChatMessage.ChatType.ChatRoom) {
                defaultAvatar = io.agora.chat.uikit.R.drawable.ease_default_group_avatar;
            } else {
                defaultAvatar = io.agora.chat.uikit.R.drawable.ease_default_avatar;
            }
            name.setText(item.getFrom());
            avatar.setImageResource(defaultAvatar);

            if (item.direct() == ChatMessage.Direct.SEND && item.status() == ChatMessage.Status.FAIL) {
                msg_state.setVisibility(View.VISIBLE);
            } else {
                msg_state.setVisibility(View.GONE);
            }

            EaseUserProfileProvider userProvider = EaseUIKit.getInstance().getUserProvider();
            if (userProvider != null) {
                EaseUser user = userProvider.getUser(item.getFrom());
                if (user != null) {
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Drawable drawable = this.avatar.getDrawable();

                        try {
                            //Compatible with local images
                            Integer intAvatar = Integer.valueOf(user.getAvatar());
                            Glide.with(mContext)
                                    .load(intAvatar)
                                    .placeholder(defaultAvatar)
                                    .error(drawable)
                                    .into(this.avatar);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Glide.with(mContext)
                                    .load(user.getAvatar())
                                    .placeholder(defaultAvatar)
                                    .error(drawable)
                                    .into(this.avatar);
                        }
                    }
                }
            }

            String content = EaseSmileUtils.getSmiledText(mContext, EaseUtils.getMessageDigest(item, mContext)).toString();
            message.post(() -> {
                String subContent = EaseEditTextUtils.ellipsizeString(message, content, keyword, message.getWidth());
                SpannableStringBuilder builder = EaseEditTextUtils.highLightKeyword(mContext, subContent, keyword);
                if (builder != null) {
                    message.setText(builder);
                } else {
                    message.setText(content);
                }
            });
        }
    }
}
