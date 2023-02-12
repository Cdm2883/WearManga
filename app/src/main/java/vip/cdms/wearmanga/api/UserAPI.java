package vip.cdms.wearmanga.api;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class UserAPI {
    /* 导航栏用户信息 */
    public static void nav(CookieJar cookieJar, API.JsonDataCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        Request getRequest = new Request.Builder()
                .url("https://api.bilibili.com/x/web-interface/nav")
                .build();
        client.newCall(getRequest).enqueue(new API.OkhttpJsonDataCallback(callback));
    }
}
