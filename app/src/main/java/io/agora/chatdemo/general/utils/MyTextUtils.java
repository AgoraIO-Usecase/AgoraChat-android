package io.agora.chatdemo.general.utils;

import android.text.TextUtils;

import java.nio.charset.StandardCharsets;

public class MyTextUtils {
    /**
     * Get bytes length
     * @param content
     * @return
     */
    public static int getBytesLength(String content) {
        if(TextUtils.isEmpty(content)) {
            return 0;
        }
        return content.getBytes(StandardCharsets.UTF_8).length;
    }
}
