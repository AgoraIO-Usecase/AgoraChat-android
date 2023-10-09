package io.agora.chatdemo.general.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonUtils {
    private static long lastClickTime = 0;

    public static void copyContentToClipboard(Context context,String content ) {
        // Get a clipboard manager：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // create ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // Put the ClipData contents into the system clipboard.
        cm.setPrimaryClip(mClipData);
    }

    /**
     * Judge whether is json
     * @param content
     * @return
     */
    public static boolean isJson(String content) {
        try {
            if(content.contains("[")&&content.contains("]")){
                new JSONArray(content);
            }else{
                new JSONObject(content);
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Prevent multiple clicks
     * @return
     */
    public static synchronized boolean isCanClick(){
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 1000) {
            return false;
        }
        lastClickTime = time;
        return true;
    }
}
