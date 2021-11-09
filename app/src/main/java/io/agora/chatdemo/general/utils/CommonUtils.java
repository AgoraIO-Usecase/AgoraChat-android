package io.agora.chatdemo.general.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class CommonUtils {

    public static void copyContentToClipboard(Context context,String content ) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", content);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }
}
