package io.agora.chatdemo.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import io.agora.ValueCallBack;
import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.utils.EaseSmileUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.chatrow.AutolinkSpan;
import io.agora.chat.uikit.widget.chatrow.EaseChatRow;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.models.UrlPreViewBean;
import io.agora.util.EMLog;

public class ChatRowUrlPreView extends EaseChatRow {
    private TextView mContent;
    private TextView mTitle;
    private TextView mDescribe;
    private EaseImageView mIcon;

    public ChatRowUrlPreView(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowUrlPreView(Context context, ChatMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.layout_chat_url_preview_recieved
                : R.layout.layout_chat_url_preview_send, this);
    }

    @Override
    protected void onFindViewById() {
        mTitle = findViewById(R.id.tv_title);
        mContent = findViewById(R.id.tv_content);
        mDescribe = findViewById(R.id.tv_describe);
        mIcon = findViewById(R.id.iv_icon);
    }

    @Override
    protected void onSetUpView() {
        if (message.getBody() != null && message.getBody() instanceof TextMessageBody){
            TextMessageBody messageBody = (TextMessageBody)message.getBody();
            String content = messageBody.getMessage();
            Spannable span = EaseSmileUtils.getSmiledText(context, content);
            // 设置内容
            mContent.setText(span, TextView.BufferType.SPANNABLE);

            mContent.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mContent.setTag(R.id.action_chat_long_click,true);
                    if (itemClickListener != null) {
                        return itemClickListener.onBubbleLongClick(v, message);
                    }
                    return false;
                }
            });
        }
        replaceSpan();
    }

    /**
     * 解决长按事件与relink冲突，参考：https://www.jianshu.com/p/d3bef8449960
     */
    private void replaceSpan() {
        Spannable spannable = (Spannable) mContent.getText();
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        String url = spans[0].getURL();
        int index = spannable.toString().indexOf(url);
        int end = index + url.length();
        if (index == -1) {
            if (url.contains("http://")) {
                url = url.replace("http://", "");
            } else if (url.contains("https://")) {
                url = url.replace("https://", "");
            } else if (url.contains("rtsp://")) {
                url = url.replace("rtsp://", "");
            }
            index = spannable.toString().indexOf(url);
            end = index + url.length();
        }

        UrlPreViewBean urlPreviewInfo = DemoHelper.getInstance().getUrlPreviewInfo(message.getMsgId());
        if (urlPreviewInfo == null){
            parsingUrl(spans[0].getURL(),message.getMsgId(),new ValueCallBack<UrlPreViewBean>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onSuccess(UrlPreViewBean value) {
                    itemActionCallback.refreshView();
                }

                @Override
                public void onError(int error, String errorMsg) {
                    mIcon.setVisibility(GONE);
                    mDescribe.setVisibility(GONE);
                    mTitle.setVisibility(GONE);
                    EMLog.e("ChatRowUrlPreview","parsingUrl onError" + errorMsg + error);
                }
            });
        }else {

            if (TextUtils.isEmpty(urlPreviewInfo.getPrimaryImg())){
                mIcon.setVisibility(GONE);
            }else {
                mIcon.setVisibility(VISIBLE);
                if (urlPreviewInfo.getPrimaryImg().endsWith(".gif")){
                    Glide.with(context)
                            .asGif().load(urlPreviewInfo.getPrimaryImg())
                            .listener(new RequestListener<GifDrawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                    mIcon.setVisibility(GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(mIcon);
                }else {
                    Glide.with(context)
                            .load(urlPreviewInfo.getPrimaryImg())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    mIcon.setVisibility(GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(mIcon);
                }
            }

            if (TextUtils.isEmpty(urlPreviewInfo.getDescribe())){
                mDescribe.setVisibility(GONE);
            }else {
                mDescribe.setVisibility(VISIBLE);
                mDescribe.setText(urlPreviewInfo.getDescribe());
            }

            if (TextUtils.isEmpty(urlPreviewInfo.getTitle())){
                mIcon.setVisibility(GONE);
                mDescribe.setVisibility(GONE);
                mTitle.setVisibility(GONE);
            }else {
                mTitle.setVisibility(VISIBLE);
                mTitle.setText(urlPreviewInfo.getTitle());
            }

        }

        if (index != -1) {
            spannable.removeSpan(spans[0]);
            spannable.setSpan(new AutolinkSpan(spans[0].getURL()), index
                    , end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    private void parsingUrl(String url,String msgId, ValueCallBack<UrlPreViewBean> callBack){
        EaseThreadManager.getInstance().runOnIOThread(()->{
            try {
                UrlPreViewBean urlPreViewBean = new UrlPreViewBean();
                String descriptionContent = "";
                String logoUrl = "";
                String src = "";
                Document document = Jsoup.connect(url)
                        .header("content-type","text/html;charset=utf-8")
                        .timeout(5000).get();

                String title = document.title();

                Element description = document.select("head meta[name=description]").first();

                Element metaTag = document.selectFirst("head meta[property=og:image]");
                if (metaTag != null){
                    src = metaTag.attr("content");
                }

                Elements linkTags = document.select("head link");
                if (linkTags != null){
                    // 遍历linkTags，解析相关属性值
                    for (Element linkTag : linkTags) {
                        String href = linkTag.attr("href");
                        String rel = linkTag.attr("rel");

                        // 如果rel属性值为"apple-touch-icon-precomposed"，则输出href属性值
                        if (rel.equals("apple-touch-icon-precomposed") && DemoHelper.getInstance().isPicture(href)) {
                            src = href;
                        }
                    }
                }

                Element logoElement = document.selectFirst("link[rel='icon']");
                if (logoElement != null && TextUtils.isEmpty(src)){
                    src = logoElement.attr("href");
                }

                Element imgElement = document.selectFirst("img");
                if (imgElement != null && TextUtils.isEmpty(src)){
                    src = imgElement.absUrl("src");
                }

                if (!DemoHelper.getInstance().containsUrl(src)){
                    // 如果不是标准url路径 判断是否是 //开头或者 /开头做相应处理
                    if(src.startsWith("//")){
                        logoUrl = "http:" + src;
                    }else {
                        logoUrl = url + src;
                    }
                }else {
                    if(src.startsWith("//")){
                        logoUrl = "http:" + src;
                    } else if (src.startsWith("www")){
                        logoUrl = "http://" + src;
                    }else {
                        logoUrl = src;
                    }
                }

                // Get the content of the description node
                if (description != null){
                    descriptionContent = description.attr("content");
                }

                urlPreViewBean.setTitle(title);//标题
                urlPreViewBean.setPrimaryImg(logoUrl); // 首图
                urlPreViewBean.setDescribe(descriptionContent); // 内容

                EMLog.d("ChatRowUrlPreview",
                        "title:" + title +"\n"
                                + "description " + descriptionContent + "\n"
                                + "logo " + logoUrl + "\n");

                DemoHelper.getInstance().saveUrlPreviewInfo(msgId,urlPreViewBean);

                post(()->{
                    callBack.onSuccess(urlPreViewBean);
                });
            } catch (IOException e) {
                e.printStackTrace();
                post(()->{
                    callBack.onError(1,e.getMessage());
                });
            }
        });
    }
}
