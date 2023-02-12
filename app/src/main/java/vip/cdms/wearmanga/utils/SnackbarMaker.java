package vip.cdms.wearmanga.utils;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarMaker {
    public static Snackbar makeTop(View view, CharSequence text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        ViewGroup.LayoutParams snackbarViewLayoutParams = snackbarView.getLayoutParams();
        FrameLayout.LayoutParams snackbarFrameLayoutParams = new FrameLayout.LayoutParams(snackbarViewLayoutParams.width, snackbarViewLayoutParams.height);
        snackbarFrameLayoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        snackbarFrameLayoutParams.topMargin = 10;
        snackbarFrameLayoutParams.leftMargin = 10;
        snackbarFrameLayoutParams.rightMargin = 10;
        snackbarView.setLayoutParams(snackbarFrameLayoutParams);
        return snackbar;
    }
    public static Snackbar makeTopAction(View view, CharSequence text, int duration, CharSequence actionText, View.OnClickListener actionListener) {
        Snackbar snackbar = makeTop(view, text, duration);
        snackbar.setAction(actionText, actionListener);
        return snackbar;
    }
}
