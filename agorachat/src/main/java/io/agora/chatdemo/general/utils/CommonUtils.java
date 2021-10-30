package io.agora.chatdemo.general.utils;

import android.content.Context;

public class CommonUtils {

    public static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }
    public static float getAbsDimen(Context context, int resId){
        return context.getResources().getDimension(resId);
    }
    public static float getSpDimen(Context context, int resId){
        return UIUtils.px2sp(context, (int) context.getResources().getDimension(resId));
    }
    public static float getDpDimen(Context context, int resId){
        return UIUtils.px2dp(context, (int) context.getResources().getDimension(resId));
    }
}
