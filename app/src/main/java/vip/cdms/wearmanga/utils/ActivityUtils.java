package vip.cdms.wearmanga.utils;

import android.app.Activity;
import android.content.Intent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import vip.cdms.wearmanga.api.BiliAPIError;

public class ActivityUtils {
    public static void alert(Activity activity, CharSequence title, CharSequence message) {
        activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(activity);
            if (title != null) materialAlertDialogBuilder.setTitle(title);
            if (message != null) materialAlertDialogBuilder.setMessage(message);
            materialAlertDialogBuilder.show();
        });
    }
    public static void alert(Activity activity, Exception exception) {
        if (exception instanceof BiliAPIError) {
            BiliAPIError biliAPIError = (BiliAPIError) exception;
            alert(activity, "CODE: " + biliAPIError.getCode(), biliAPIError.getMessage());
        } else alert(activity, null, exception.toString());
        exception.printStackTrace();
    }

    public static void restartApp(Activity activity) {
        final Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        // 杀掉以前进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
