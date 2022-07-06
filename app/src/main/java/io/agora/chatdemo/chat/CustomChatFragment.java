package io.agora.chatdemo.chat;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.giphy.sdk.core.GPHCore;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.models.enums.RatingType;
import com.giphy.sdk.core.models.enums.RenditionType;
import com.giphy.sdk.core.network.response.MediaResponse;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.themes.GridType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Time;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.widget.EaseChatInputMenu;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chatdemo.BuildConfig;
import io.agora.chatdemo.R;
import io.agora.util.EMLog;
import io.stipop.Stipop;
import io.stipop.StipopDelegate;
import io.stipop.models.SPPackage;
import io.stipop.models.SPSticker;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class CustomChatFragment extends EaseChatFragment implements StipopDelegate , EaseChatInputMenu.onTabChangedListener ,EaseChatInputMenu.onMenuStatusListener {
    public InputMethodManager inputManager;
    public GPHContentType contentType;
    public boolean isShow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        Giphy.INSTANCE.configure(getActivity(), BuildConfig.Giphy_ApIkey,true);


    }

    @Override
    public void initView() {
        super.initView();
        //Add extended emoji
        chatLayout.getChatInputMenu().getEmojiconMenu().addTabItem(R.drawable.em_sticker_selector);
        chatLayout.getChatInputMenu().getEmojiconMenu().addTabItem(R.drawable.em_gif_selsctor);

        chatLayout.getChatInputMenu().getEmojiconMenu().setTabBarVisibility(true);
        chatLayout.getChatInputMenu().setTabChangedListener(this);
        chatLayout.getChatInputMenu().setMenuStatus(this);

        Stipop.Companion.connect(getActivity(), ChatClient.getInstance().getCurrentUser(), this, null, Locale.getDefault(), null);

    }

        @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

            OnBackPressedCallback callback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                hideBottom();
                if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(callback);
    }

    @Override
    public boolean onStickerPackRequested(@NotNull SPPackage spPackage) {
        // return true Allow download of stickers
        return true;
    }

    @Override
    public boolean onStickerSelected(@NotNull SPSticker spSticker) {
        EMLog.e("onStickerSelected",
                  "\n  packageId: " + spSticker.getPackageId() +
                        "\n  stickerId: " + spSticker.getStickerId() +
                        "\n  stickerImg: " + spSticker.getStickerImg() +
                        "\n  stickerImgLocalFilePath: " + spSticker.getStickerImgLocalFilePath() +
                        "\n  favoriteYN: " + spSticker.getFavoriteYN() +
                        "\n  keyword: " + spSticker.getKeyword());
        hideBottom();
        if (isEmojiAsGif(spSticker.getStickerImg())){
            sendEmojiMessage(spSticker.getStickerImg(),getEmojiType(spSticker.getStickerImg()));
        }else {
            //Check if a local file exists
            //stickerImgLocalFilePath: /data/user/0/io.agora.chatdemo/files/stipop/550/1537519590937_carrot_07.png
            if (null != spSticker.getStickerImgLocalFilePath()){
                Uri uri = getUriForFile(getActivity(),new File(spSticker.getStickerImgLocalFilePath()));
                if (EaseFileUtils.isFileExistByUri(getActivity(), uri)){
                    EMLog.i("onStickerSelected","sendImageMessage");
                    chatLayout.sendImageMessage(uri);
                }
                EMLog.i("onStickerSelected","isFileExistByUri false");
            }else {
                EMLog.i("onStickerSelected","sendEmojiMessage");
                sendEmojiMessage(spSticker.getStickerImg(),getEmojiType(spSticker.getStickerImg()));
            }
        }
        return true;
    }

    public static Uri getUriForFile(Context context, @NonNull File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        } else {
            return Uri.fromFile(file);
        }
    }

    //Determine whether it is a gif type
    //https://img.stipop.io/1537519590917_carrot_02.png
    public boolean isEmojiAsGif(String url){
        String[] strs = url.split("[.]");
        if (strs[strs.length - 1].equals("gif")){
            return true;
        }
        return false;
    }

    public String getFileName(String url){
        int index = url.lastIndexOf("/");
        return url.substring(index+1);
    }

    public String getEmojiType(String url){
        int index = url.lastIndexOf(".");
        return url.substring(index+1);
    }



    public void hideBottom(){
        if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
        if(inputManager == null) {
            return ;
        }
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);
        chatLayout.getChatInputMenu().hideExtendContainer();
    }

    @Override
    public void onTabChanged(int index) {
        switch (index){
            case 0 :
                chatLayout.getChatInputMenu().getPrimaryMenu().getEditText().requestFocus();
                if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
                chatLayout.getChatInputMenu().showEmojiconMenu(true);
                if(inputManager == null) {
                    return ;
                }
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);
                break;
            case 1 :
                Stipop.Companion.showKeyboard();
                break;
            case 2 :
                chatLayout.getChatInputMenu().hideExtendContainer();
                if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
                show(false, new GiphyDialogFragment.GifSelectionListener() {
                    @Override
                    public void onGifSelected(@NotNull Media media, @org.jetbrains.annotations.Nullable String s, @NotNull GPHContentType gphContentType) {
                        EMLog.e("onGifSelected", gphContentType.toString()+
                                        "\n  getBitlyGifUrl: " + media.getBitlyGifUrl() +
                                        "\n  getBitlyUrl: " + media.getBitlyUrl() +
                                        "\n  getContentUrl: " + media.getContentUrl() +
                                        "\n  getEmbedUrl: " + media.getEmbedUrl() +
                                        "\n  getTid: " + media.getTid() +
                                        "\n  getUrl: " + media.getUrl() +
                                        "\n  getTitle: " + media.getTitle());
                        contentType = gphContentType;
                        EMLog.i("emoji",contentType.getMediaType().toString());
                        GPHCore.INSTANCE.gifById(media.getId(), new Function2<MediaResponse, Throwable, Unit>() {
                            @Override
                            public Unit invoke(MediaResponse mediaResponse, Throwable throwable) {
                                if (contentType.getMediaType() == MediaType.video){
                                    EMLog.i("onGifSelected","MediaType video is not supported ");
                                }else {
                                    sendEmojiMessage(mediaResponse.getData().getImages().getOriginal().getGifUrl(),"gif");
                                    EMLog.i("onGifSelected","invoke ：" + mediaResponse.getData().getImages().getOriginal().getGifUrl());
                                }
                                chatLayout.getChatInputMenu().hideExtendContainer();
                                return null;
                            }
                        });


                    }

                    @Override
                    public void onDismissed(@NotNull GPHContentType gphContentType) {
                        contentType = gphContentType;
                    }

                    @Override
                    public void didSearchTerm(@NotNull String s) {

                    }
                });
                break;
        }
    }


    public GiphyDialogFragment show(final  boolean withDarkTheme, final  GiphyDialogFragment.GifSelectionListener listener)
    {
        final GPHTheme theme = (withDarkTheme)
                ? GPHTheme.Dark
                : GPHTheme.Light;

        final GPHSettings settings = new GPHSettings();
        settings.setTheme(theme);
        settings.setRating(RatingType.pg13);
        settings.setRenditionType(RenditionType.fixedWidth);
        settings.setGridType(GridType.waterfall);
        settings.setShowCheckeredBackground(false);

        final GPHContentType[] contentTypes = new GPHContentType[5];
        contentTypes[3] = GPHContentType.sticker;
        contentTypes[2] = GPHContentType.gif;
        contentTypes[4] = GPHContentType.text;
        contentTypes[1] = GPHContentType.emoji;
        contentTypes[0] = GPHContentType.recents;
        settings.setMediaTypeConfig(contentTypes);
        settings.setSelectedContentType(GPHContentType.gif);

        settings.setGridType(GridType.waterfall);
        settings.setStickerColumnCount(3);
        final GiphyDialogFragment dialog = GiphyDialogFragment.Companion.newInstance(settings);
        dialog.setGifSelectionListener(listener);
        if (getActivity() != null)
        dialog.show(getActivity().getSupportFragmentManager(), "giphy_dialog");
        isShow = true;
        return dialog;
    }

    /**
     * Send emoji messages
     * @param url
     */
    public void sendEmojiMessage(String url,String type){
        EMLog.e("sendEmojiMessage",type);
        ChatMessage message = ChatMessage.createSendMessage(ChatMessage.Type.IMAGE);
        message.setTo(conversationId);
        message.setAttribute("emoji_url",url);
        message.setAttribute("emoji_type",type);
        ImageMessageBody body = new ImageMessageBody(new File(""));
        //sendMessage remoteUrl cannot be an empty string.
        body.setRemoteUrl(url);
        //set local path to empty string
        body.setLocalUrl("");
        body.setFileName(getFileName(url));
        message.addBody(body);
        chatLayout.sendMessage(message);
    }


    @Override
    public void onTouchItemOutside() {
        super.onTouchItemOutside();
        if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
    }

    @Override
    public void onViewDragging() {
        super.onViewDragging();
        if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
        chatLayout.getChatInputMenu().hideExtendContainer();
    }


    @Override
    public void onEmojiconMenuChanged(boolean isShow) {
        if (!isShow){
            if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
        }
    }

    @Override
    public void onExtendMenuChanged(boolean isShow) {
        if (isShow){
            if (Stipop.Companion.isShowing()) Stipop.Companion.hideKeyboard();
        }
    }
}

