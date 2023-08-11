package io.agora.chatdemo.chat.chatrow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.utils.EaseSmileUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.chatrow.AutolinkSpan;
import io.agora.chat.uikit.widget.chatrow.EaseChatRowText;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.models.UrlPreViewBean;
import io.agora.util.EMLog;

public class ChatRowCustomTextView extends EaseChatRowText {
    public static final String AT_PREFIX = "@";
    public static final String AT_SUFFIX = " ";
    private TextView mContent;
    private TextView mTitle;
    private TextView mDescribe;
    private TextView tvTranslation;
    private TextView tvTranslationTag;
    private EaseImageView mIcon;
    private RelativeLayout tvContentLayout;
    private ConstraintLayout describeLayout;
    private ConstraintLayout lyTranslation;
    private String translationContent;
    private String oldTranslationContent;
    private boolean isShowOriginal = false;
    private translationClickListener listener;

    public ChatRowCustomTextView(Context context, boolean isSender) {
        super(context, isSender);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.layout_chat_url_preview_recieved
                : R.layout.layout_chat_url_preview_send, this);
    }

    @Override
    protected void onFindViewById() {
        super.onFindViewById();
        mTitle = findViewById(R.id.tv_title);
        mContent = findViewById(R.id.tv_chatcontent);
        mDescribe = findViewById(R.id.tv_describe);
        mIcon = findViewById(R.id.iv_icon);
        describeLayout = findViewById(R.id.describe_layout);
        tvContentLayout = findViewById(R.id.tv_chatcontent_layout);
        lyTranslation = findViewById(R.id.cl_translationLayout);
        tvTranslation = findViewById(R.id.tv_translation);
        tvTranslationTag = findViewById(R.id.tv_translation_tag);
    }

    @Override
    public void onSetUpView() {
        super.onSetUpView();

        lyTranslation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onTranslationClick(v,message);
                }
            }
        });

        lyTranslation.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null){
                    return listener.onTranslationLongClick(v,message);
                }
                return false;
            }
        });

        if (message.getType() == ChatMessage.Type.TXT){
            TextMessageBody body = (TextMessageBody)message.getBody();
            if (body != null){
                String localTargetLanguage = DemoHelper.getInstance().getModel().getTargetLanguage();
                oldTranslationContent = body.getMessage();
                List<TextMessageBody.TranslationInfo> translations = body.getTranslations();
                if (translations.size() > 0 && !isSender){
                    for (TextMessageBody.TranslationInfo translation : translations) {
                        if (translation.languageCode.equals(localTargetLanguage)){
                            translationContent = translation.translationText;
                        }else {
                            TextMessageBody.TranslationInfo translationInfo = translations.get(0);
                            translationContent = translationInfo.translationText;
                        }
                        tvTranslationTag.setVisibility(VISIBLE);
                        lyTranslation.setVisibility(VISIBLE);
                        switchTranslation();
                    }
                }else {
                    lyTranslation.setVisibility(GONE);
                    tvTranslationTag.setVisibility(GONE);
                }

                if (DemoHelper.getInstance().containsUrl(((TextMessageBody) message.getBody()).getMessage())){
                    urlPreView();
                }else {
                    loadErrorChangeBg();
                }

                replacePickAtSpan();
            }
        }
    }

    private void urlPreView() {
        Spannable spannable = (Spannable) mContent.getText();
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        if (spans.length <= 0){
            return;
        }
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
                    if (isSender){
                        tvContentLayout.setBackground(context.getResources().getDrawable(R.drawable.shape_url_preview_send_top_bg));
                    }else {
                        tvContentLayout.setBackground(context.getResources().getDrawable(R.drawable.shape_url_preview_receive_top_bg));
                    }
                }

                @Override
                public void onError(int error, String errorMsg) {
                    loadErrorChangeBg();
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
                                    loadErrorChangeBg();
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
                                    loadErrorChangeBg();
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

            if (TextUtils.isEmpty(urlPreviewInfo.getDescribe()) && TextUtils.isEmpty(urlPreviewInfo.getTitle())){
                describeLayout.setVisibility(GONE);
            }else {
                describeLayout.setVisibility(VISIBLE);
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
                    for (Element linkTag : linkTags) {
                        String href = linkTag.attr("href");
                        String rel = linkTag.attr("rel");

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

                urlPreViewBean.setTitle(title);
                urlPreViewBean.setPrimaryImg(logoUrl);
                urlPreViewBean.setDescribe(descriptionContent);

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

    private void loadErrorChangeBg(){
        mIcon.setVisibility(GONE);
        describeLayout.setVisibility(GONE);
        if (isSender){
            tvContentLayout.setBackground(context.getResources().getDrawable(R.drawable.ease_chat_bubble_send_bg));
        }else {
            tvContentLayout.setBackground(context.getResources().getDrawable(R.drawable.ease_chat_bubble_receive_bg));
        }
    }


    private void switchTranslation(){
        int start = 0; int end = 0;
        String tag = "";
        if (isShowOriginal){
            // 设置内容
            String oldTranslation = convertSpecialSymbols(oldTranslationContent);
            Spannable span = EaseSmileUtils.getSmiledText(context, oldTranslation);
            tvTranslation.setText(span, TextView.BufferType.SPANNABLE);
            tag = context.getResources().getString(R.string.translation_view_translation);
            start = tag.length() - 16;
        }else{
            String translation = convertSpecialSymbols(translationContent);
            Spannable span = EaseSmileUtils.getSmiledText(context, translation);
            tvTranslation.setText(span, TextView.BufferType.SPANNABLE);
            tag = context.getResources().getString(R.string.translation_original_text);
            start = tag.length() - 18;
        }
        end = tag.length();
        if (!TextUtils.isEmpty(tag)){
            SpannableString spannableString = new SpannableString(tag);
            ClickableSpan ClickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    isShowOriginal = !isShowOriginal;
                    switchTranslation();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(getResources().getColor(R.color.color_main_blue));
                    ds.setUnderlineText(false);
                }
            };

            if (end > 0){
                spannableString.setSpan(ClickableSpan, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTranslationTag.setMovementMethod(LinkMovementMethod.getInstance());
            }
            tvTranslationTag.setText(spannableString);
        }
    }

    public void setTranslationOnClickListener(translationClickListener listener){
        this.listener = listener;
    }

    public interface translationClickListener{
        void onTranslationClick(View view, ChatMessage message);
        boolean onTranslationLongClick(View view,ChatMessage message);
    }

    private static String convertSpecialSymbols(String str) {
        String pattern = "\\[([^\\]]+)\\]";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String insideBracket = m.group(1);
            String converted = convertSymbols(insideBracket);
            m.appendReplacement(sb, "[" + Matcher.quoteReplacement(converted) + "]");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String convertSymbols(String str) {
        // Replace special symbols
        String[] specialSymbols = {"：","！","/", "@", "#", "$", "……", "&", "*", "（", "）", "_", "+", "{}", "<>", "?",};
        String[] convertedSymbols = {":","!","|", "@", "#", "$", "^", "&", "*", "(", ")", "_", "+", "{", "<", ">", "?",};

        for (int i = 0; i < specialSymbols.length; i++) {
            str = str.replace(specialSymbols[i], convertedSymbols[i]);
        }
        return str;
    }

    private void replacePickAtSpan(){
        if (message.ext().containsKey(EaseConstant.MESSAGE_ATTR_AT_MSG)){
            String atAll = ""; String atMe = "";
            int start = 0;  int end = 0;
            try {
                JSONArray jsonArray = message.getJSONArrayAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG);
                for(int i = 0; i < jsonArray.length(); i++){
                    String username = jsonArray.getString(i);
                    if(ChatClient.getInstance().getCurrentUser().equals(username)){
                        atMe = username;
                    }
                }
            } catch (Exception e) {
                String atUsername = message.getStringAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, null);
                if(atUsername != null){
                    String s = atUsername.toUpperCase();
                    if(s.equals((EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL).toUpperCase())){
                        atAll = atUsername;
                    }
                }
            }

            if (!TextUtils.isEmpty(atMe)){
                atMe = AT_PREFIX + atMe + AT_SUFFIX;
                start = mContent.getText().toString().indexOf(atMe);
                end = start + atMe.length();
            }else {
                atAll = AT_PREFIX + atAll + AT_SUFFIX;
                start = mContent.getText().toString().indexOf(atAll);
                end = start + atAll.length();
            }

            if (start != -1 && end > 0){
                Spannable spannable = (Spannable) mContent.getText();
                spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(io.agora.chat.uikit.R.color.color_conversation_title)), start
                        , end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }
}
