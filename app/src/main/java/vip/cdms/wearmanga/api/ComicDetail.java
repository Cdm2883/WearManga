package vip.cdms.wearmanga.api;


import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ComicDetail {
    public static void get(CookieJar cookieJar, int comic_id, API.JsonDataCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "  \"comic_id\": " + comic_id + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url("https://manga.bilibili.com/twirp/comic.v1.Comic/ComicDetail?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback(callback));
    }
}
