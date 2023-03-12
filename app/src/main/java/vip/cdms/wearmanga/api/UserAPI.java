package vip.cdms.wearmanga.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

public class UserAPI {
    public static final String HOST = "https://manga.bilibili.com/twirp/user.v1.User";

    /** 导航栏用户信息 */
    public static void nav(CookieJar cookieJar, API.JsonDataCallback<JSONObject> callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        Request getRequest = new Request.Builder()
                .url("https://api.bilibili.com/x/web-interface/nav")
                .build();
        client.newCall(getRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 已购漫画
     * @param page_num 页码
     * @param page_size 每页数量 (15)
     */
    public static void GetAutoBuyComics(
            CookieJar cookieJar,
            int page_num,
            int page_size,
            API.JsonDataCallback<JSONArray> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "    \"page_num\": " + page_num + ",\n" +
                        "    \"page_size\": " + page_size + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/GetAutoBuyComics?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
}
