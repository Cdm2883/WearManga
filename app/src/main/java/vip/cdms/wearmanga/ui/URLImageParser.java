package vip.cdms.wearmanga.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.text.Html;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLImageParser implements Html.ImageGetter {
    TextView textView;
    private final Context context;

    public URLImageParser(TextView textView, Context context) {
        this.context = context;
        this.textView = textView;
    }

    public Drawable getDrawable(String source) {
        int width = Target.SIZE_ORIGINAL;
        int height = Target.SIZE_ORIGINAL;

        Pattern patternWH = Pattern.compile("^https?://.*\\.hdslb\\.com/bfs/.+/.+\\.(?:jpg|png|gif|webp)@(?:([0-9]+)w)?_?(?:([0-9]+)h)?$", Pattern.CASE_INSENSITIVE);
        Matcher matcherWH = patternWH.matcher(source);
        if (matcherWH.find()) {
            String group1 = matcherWH.group(1);
            if (group1 != null) width = Integer.parseInt(group1);
            String group2 = matcherWH.group(2);
            if (group2 != null) height = Integer.parseInt(group2);
        }

        final LevelListDrawable drawable = new LevelListDrawable();
        int finalWidth = width;
        int finalHeight = height;
        Glide.with(context)
                .asBitmap()
                .load(source)
                .into(new SimpleTarget<Bitmap>(finalWidth, finalHeight) {
                    @Override
                    public void onResourceReady(
                            @NonNull @NotNull Bitmap resource,
                            @Nullable @org.jetbrains.annotations.Nullable Transition<? super Bitmap> transition
                    ) {
//                        if (resource == null) return;
                        Bitmap resourceResize;

                        if (resource.getWidth() == finalWidth || resource.getHeight() == finalHeight) resourceResize = resource;
                        else {
                            resourceResize = Bitmap.createBitmap(finalWidth, finalHeight, resource.getConfig());
                            Canvas canvas = new Canvas(resourceResize);
                            canvas.drawBitmap(resource, null, new Rect(0, 0, resourceResize.getWidth(), resourceResize.getHeight()), null);
                        }

                        BitmapDrawable bitmapDrawable = new BitmapDrawable(resourceResize);
                        drawable.addLevel(1, 1, bitmapDrawable);
                        drawable.setBounds(0, 0, resourceResize.getWidth(), resourceResize.getHeight());
                        drawable.setLevel(1);
                        // 防止图文重叠
                        textView.invalidate();
                        textView.setText(textView.getText());
                    }
                });
        return drawable;
    }
}

