package vip.cdms.wearmanga.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import vip.cdms.wearmanga.utils.StringUtils;

public class BookshelfAPI {
    public static final String HOST = "https://manga.bilibili.com/twirp/bookshelf.v1.Bookshelf";

    // ---------- History ---------- //

    /**
     * 增加阅读历史
     * @param comic_id 漫画ID
     * @param ep_id 当前话ID
     */
    public static void AddHistory(
            CookieJar cookieJar,
            int comic_id,
            int ep_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "  \"comic_id\": " + comic_id + ",\n" +
                        "  \"ep_id\": " + ep_id + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/AddHistory?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    public static void DeleteHistory(
            CookieJar cookieJar,
            String comic_ids,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "    \"comic_ids\": \"" + comic_ids + "\"\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/DeleteHistory?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
    public static void DeleteHistory(
            CookieJar cookieJar,
            int comic_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        DeleteHistory(cookieJar, String.valueOf(comic_id), callback);
    }
    public static void DeleteHistory(
            CookieJar cookieJar,
            int[] comic_ids,
            API.JsonDataCallback<JSONObject> callback
    ) {
        DeleteHistory(cookieJar, StringUtils.join(",", comic_ids), callback);
    }

    /**
     * 阅读历史
     * @param page_num 页码
     * @param page_size 每页数量 (15)
     */
    public static void ListHistory(
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
                .url(HOST + "/ListHistory?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    // ---------- Favorite ---------- //

    public static void AddFavorite(
            CookieJar cookieJar,
            int comic_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "    \"comic_ids\": \"" + comic_id + "\"\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/AddFavorite?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    public static void DeleteFavorite(
            CookieJar cookieJar,
            String comic_ids,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "    \"comic_ids\": \"" + comic_ids + "\"\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/DeleteFavorite?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
    public static void DeleteFavorite(
            CookieJar cookieJar,
            int comic_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        DeleteFavorite(cookieJar, String.valueOf(comic_id), callback);
    }
    public static void DeleteFavorite(
            CookieJar cookieJar,
            int[] comic_ids,
            API.JsonDataCallback<JSONObject> callback
    ) {
        DeleteFavorite(cookieJar, StringUtils.join(",", comic_ids), callback);
    }

    /** 我的追漫 - 排序规则 - 追漫顺序 */
    public static final int LIST_FAV_ORDER_CHRONOLOGICAL = 1;
    /** 我的追漫 - 排序规则 - 更新时间 */
    public static final int LIST_FAV_ORDER_UPDATE = 2;
    /** 我的追漫 - 排序规则 - 最近阅读 */
    public static final int LIST_FAV_ORDER_RECENTLY_READ = 3;
    /** 我的追漫 - 排序规则 - 完成等免 */
    public static final int LIST_FAV_ORDER_WAIT_FREE = 4;
    /**
     * 我的追漫
     * @param order 排序规则
     * @param page_num 页码
     * @param page_size 每页数量 (15)
     */
    public static void ListFavorite(
            CookieJar cookieJar,
            int order,
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
                        "    \"page_size\": " + page_size + ",\n" +
                        "    \"order\": " + (order == LIST_FAV_ORDER_WAIT_FREE ? LIST_FAV_ORDER_RECENTLY_READ : order) + ",\n" +
                        "    \"wait_free\": " + (order == LIST_FAV_ORDER_WAIT_FREE ? 1 : 0) + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/ListFavorite?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
}
