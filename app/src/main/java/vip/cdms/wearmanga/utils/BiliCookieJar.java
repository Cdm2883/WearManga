package vip.cdms.wearmanga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class BiliCookieJar implements CookieJar {
    private final Context context;

    public BiliCookieJar(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("bili", MODE_PRIVATE);
        return new ArrayList<Cookie>(){{
            add(new Cookie.Builder()
                    .name("DedeUserID")
                    .value(sharedPreferences.getString("DedeUserID", ""))
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("DedeUserID__ckMd5")
                    .value(sharedPreferences.getString("DedeUserID__ckMd5", ""))
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("SESSDATA")
                    .value(sharedPreferences.getString("SESSDATA", ""))
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("bili_jct")
                    .value(sharedPreferences.getString("bili_jct", ""))
                    .domain("bilibili.com")
                    .path("/")
                    .build());
        }};
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {}
}
