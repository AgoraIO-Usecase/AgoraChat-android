package io.agora.chatdemo.general.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class CommonUtils {

    public static void copyContentToClipboard(Context context,String content ) {
        // Get a clipboard manager：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // create ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // Put the ClipData contents into the system clipboard.
        cm.setPrimaryClip(mClipData);
    }
}
