package vip.cdms.wearmanga.api;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

public class HomeAPI {
    // 为你推荐
    public static void HomeRecommend(CookieJar cookieJar, API.JsonDataCallback<JSONObject> callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create("{\n" +
                        "    \"page_num\": 1,\n" +
                        "    \"seed\": \"0\"\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url("https://manga.bilibili.com/twirp/comic.v1.Comic/HomeRecommend?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    // ？？？ 热门tabId: 271
    public static void GetClassPageLayout(int tab_id, CookieJar cookieJar, API.JsonDataCallback<JSONObject> callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create("{\n" +
                        "    \"tab_id\": " + tab_id + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url("https://manga.bilibili.com/twirp/comic.v1.Comic/GetClassPageLayout?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
    // 和上面那个连用
    public static void GetClassPageSixComics(int id, int page_num, int page_size, CookieJar cookieJar, API.JsonDataCallback<JSONObject> callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create("{\n" +
                        "    \"id\": " + id + ",\n" +
                        "    \"isAll\": 0,\n" +
                        "    \"page_num\": " + page_num + ",\n" +
                        "    \"page_size\": " + page_size + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url("https://manga.bilibili.com/twirp/comic.v1.Comic/GetClassPageSixComics?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
}
