package vip.cdms.wearmanga.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ActivityUtils {
    public static void alert(Activity activity, CharSequence title, CharSequence message) {
        activity.runOnUiThread(() -> {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(activity);
            if (title != null) materialAlertDialogBuilder.setTitle(title);
            if (message != null) materialAlertDialogBuilder.setMessage(message);
            materialAlertDialogBuilder.show();
        });
    }

    public static void restartApp(Activity activity) {
        final Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        // 杀掉以前进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
