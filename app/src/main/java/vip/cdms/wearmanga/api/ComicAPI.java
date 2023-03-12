package vip.cdms.wearmanga.api;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

public class ComicAPI {
    public static final String HOST = "https://manga.bilibili.com/twirp/comic.v1.Comic";

    public static void ComicDetail(CookieJar cookieJar, int comic_id, boolean getEpList, API.JsonDataCallback<JSONObject> callback) {
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
                .url(HOST + "/ComicDetail?" + (getEpList ? "device=pc&" : "") + "platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
    public static void ComicDetail(CookieJar cookieJar, int comic_id, API.JsonDataCallback<JSONObject> callback) {
        ComicDetail(cookieJar, comic_id, true, callback);
    }

    public static void MoreRecommend(CookieJar cookieJar, int comic_id, API.JsonDataCallback<JSONObject> callback) {
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
                .url(HOST + "/MoreRecommend?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 获取单话详情
     * <pre><code>{
     *     "title": "大人的游戏",
     *     "comic_id": 29329,
     *     "short_title": "721",
     *     "comic_title": "有兽焉"
     * }</code></pre>
     * @param ep_id 当前话的id
     */
    public static void GetEpisode(
            CookieJar cookieJar,
            int ep_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "  \"id\": " + ep_id + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/GetEpisode?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 获取当前话全部图片地址
     * @param ep_id 当前话的id
     * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/manga/Download.md#%E8%8E%B7%E5%8F%96%E5%BD%93%E5%89%8D%E8%AF%9D%E5%85%A8%E9%83%A8%E5%9B%BE%E7%89%87%E5%9C%B0%E5%9D%80">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/manga/Download.md#获取当前话全部图片地址</a>
     */
    public static void GetImageIndex(
            CookieJar cookieJar,
            int ep_id,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        RequestBody requestBody = RequestBody.Companion.create(
                "{\n" +
                        "  \"ep_id\": " + ep_id + "\n" +
                        "}",
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/GetImageIndex?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 获取某一图片的token
     * @param url 图片的网站路径
     * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/manga/Download.md#%E8%8E%B7%E5%8F%96%E6%9F%90%E4%B8%80%E5%9B%BE%E7%89%87%E7%9A%84token">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/manga/Download.md#获取某一图片的token</a>
     */
    public static void ImageToken(
            CookieJar cookieJar,
            String url,
            API.JsonDataCallback<JSONArray> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
//        RequestBody requestBody = RequestBody.Companion.create(
//                "\"[\\\"" + url + "\\\"]\"",
//                MediaType.Companion.parse("application/json")
//        );
        JSONObject jsonBody = new JSONObject();
        JSONArray urls = new JSONArray();
        urls.add(url);
        jsonBody.put("urls", urls.toJSONString());
        RequestBody requestBody = RequestBody.Companion.create(
                jsonBody.toJSONString(),
                MediaType.Companion.parse("application/json")
        );
        Request postRequest = new Request.Builder()
                .url(HOST + "/ImageToken?device=pc&platform=web")
                .post(requestBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
    public static String ImageTokenParser(JSONArray json_root_data) {
        JSONObject json_root_data_0 = json_root_data.getJSONObject(0);
        return json_root_data_0.getString("url") + "?token=" + json_root_data_0.getString("token");
    }
}
