package vip.cdms.wearmanga.utils;

import android.content.Context;

public class MathUtils {
    /**
     * 除法
     * @param dividend 被除数
     * @param divisor 除数
     * @param accuracy 保留几位小数
     */
    public static double divide(int dividend, int divisor, int accuracy) {
        return Math.round(dividend * Math.pow(10, accuracy) / divisor) / Math.pow(10.0, accuracy);
    }

    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        // 获取屏幕密度
        final float scale = context.getResources().getDisplayMetrics().density;
        // 结果+0.5是为了int取整时更接近
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
