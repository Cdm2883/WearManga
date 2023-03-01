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

/**
 * Bili认证Cookie
 */
public class BiliCookieJar implements CookieJar {
    private final Context context;

    public final String DedeUserID;
    public final String DedeUserID__ckMd5;
    public final String SESSDATA;
    public final String bili_jct;

    public BiliCookieJar(Context context) {
        this.context = context;

        SharedPreferences sharedPreferences = context.getSharedPreferences("bili", MODE_PRIVATE);
        DedeUserID = sharedPreferences.getString("DedeUserID", "");
        DedeUserID__ckMd5 = sharedPreferences.getString("DedeUserID__ckMd5", "");
        SESSDATA = sharedPreferences.getString("SESSDATA", "");
        bili_jct = sharedPreferences.getString("bili_jct", "");
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        return new ArrayList<Cookie>(){{
            add(new Cookie.Builder()
                    .name("DedeUserID")
                    .value(DedeUserID)
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("DedeUserID__ckMd5")
                    .value(DedeUserID__ckMd5)
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("SESSDATA")
                    .value(SESSDATA)
                    .domain("bilibili.com")
                    .path("/")
                    .build());
            add(new Cookie.Builder()
                    .name("bili_jct")
                    .value(bili_jct)
                    .domain("bilibili.com")
                    .path("/")
                    .build());
        }};
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {}
}
