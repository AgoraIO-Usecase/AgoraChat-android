package io.agora.chatdemo.general.utils;

import android.content.Context;
import android.util.TypedValue;

import io.agora.chatdemo.DemoApplication;


public class UIUtils {

    public static int dp2px(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + 0.5);
    }

    public static int sp2px(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static int px2dp(Context context, int px) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (px / density + 0.5);
    }
    public static int px2sp(Context context, int px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (px / scaledDensity + 0.5);
    }

    public static String getString(Context context, int resId){
        return context.getResources().getString(resId);
    }

    public static String getString(int resId){
        return getString(DemoApplication.getInstance(),resId);
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
