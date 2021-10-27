package io.agora.chatdemo.general.utils;

import android.content.Context;

/**
 * Created by 许成谱 on 2021/10/26 0026 20:44.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class CommonUtils {

    public static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }

    public static float getDimen(Context context, int resId){
        return context.getResources().getDimension(resId);
    }
}
