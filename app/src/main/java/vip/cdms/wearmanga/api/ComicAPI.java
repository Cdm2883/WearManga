package vip.cdms.wearmanga.api;


import okhttp3.*;

public class ComicAPI {
    public static void ComicDetail(CookieJar cookieJar, int comic_id, API.JsonDataCallback callback) {
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

    public static void MoreRecommend(CookieJar cookieJar, int comic_id, API.JsonDataCallback callback) {
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
                .url("https://manga.bilibili.com/twirp/comic.v1.Comic/MoreRecommend?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback(callback));
    }
}
