package io.agora.chatdemo.chat;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.agora.chatdemo.chat.models.UrlPreViewBean;


public class UrlPreViewHelper {
    private static final Map<String, UrlPreViewBean> previewMap = new HashMap<>();
    public static final String URL_REGEX = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
            + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";

    public static final String imageUrlRegex = "(http(s?):)|([/|.|w|s])*.(?:jpg|gif|png)";

    public static final String[] imageExtensions = {".jpeg", ".jpg", ".png", ".gif", ".bmp"};


    public static void saveUrlPreviewInfo(String msgId,UrlPreViewBean bean){
        if (!TextUtils.isEmpty(msgId)){
            previewMap.put(msgId,bean);
        }
    }

    public static UrlPreViewBean getUrlPreviewInfo(String msgId){
        if (previewMap.size() > 0 && !TextUtils.isEmpty(msgId)){
            if (previewMap.containsKey(msgId)){
                return previewMap.get(msgId);
            }
        }
        return null;
    }

    public static void clearPreviewInfo(){
        previewMap.clear();
    }

    /**
     * Determine whether the string contains a URL address
     * @param content String to match
     * @return Does it include a URL address
     */
    public static boolean containsUrl(String content) {
        Pattern p = Pattern.compile(URL_REGEX);
        Matcher m = p.matcher(content);
        boolean b = m.find();
        return b;
    }

    /**
     * Determine if the URL is an image type
     * @param url
     * @return
     */
    public static boolean isPicture(String url){
        String lowercaseFilename = url.toLowerCase();
        for(String extension : imageExtensions) {
            if(lowercaseFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}
