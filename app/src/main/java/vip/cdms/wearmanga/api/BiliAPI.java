package vip.cdms.wearmanga.api;

import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import vip.cdms.wearmanga.utils.BiliCookieJar;

public class BiliAPI {

    /** 评论区类型代码 - */
    public static final int REPLY_TYPE_MANGA_MCID = 22;
    public static final int REPLY_TYPE_MANGA_EPID = 29;

    /** 评论区排序方式 - 按时间 */
    public static final int REPLY_SORT_TIME = 0;
    /** 评论区排序方式 - 按点赞数 */
    public static final int REPLY_SORT_UP = 1;
    /** 评论区排序方式 - 按回复数 */
    public static final int REPLY_SORT_REPLY = 2;

    /**
     * 获取评论区明细 - 翻页加载
     * @param type 评论区类型代码*
     * @param oid 目标评论区 id*
     * @param sort 排序方式
     * @param ps 每页项数 (1~49, 默认为20)
     * @param pn 页码 (默认为1)
     * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/list.md#%E8%8E%B7%E5%8F%96%E8%AF%84%E8%AE%BA%E5%8C%BA%E6%98%8E%E7%BB%86_%E7%BF%BB%E9%A1%B5%E5%8A%A0%E8%BD%BD">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/list.md#获取评论区明细_翻页加载</a>
     */
    public static void reply(
            CookieJar cookieJar,
            int type,
            int oid,
            Integer sort,
            Integer ps,
            Integer pn,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        HttpUrl.Builder httpUrlBuilder = HttpUrl.get("https://api.bilibili.com/x/v2/reply").newBuilder();
        httpUrlBuilder.addQueryParameter("type", String.valueOf(type));
        httpUrlBuilder.addQueryParameter("oid", String.valueOf(oid));
        if (sort != null) httpUrlBuilder.addQueryParameter("sort", String.valueOf(sort));
        if (ps != null) httpUrlBuilder.addQueryParameter("ps", String.valueOf(ps));
        if (pn != null) httpUrlBuilder.addQueryParameter("pn", String.valueOf(pn));

        Request getRequest = new Request.Builder()
                .url(httpUrlBuilder.build())
                .build();
        client.newCall(getRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 获取评论区明细 - 获取指定评论的回复
     * @param type 评论区类型代码*
     * @param oid 目标评论区 id*
     * @param root 根回复 rpid*
     * @param ps 每页项数 (1~20, 默认为20)
     * @param pn 页码 (默认为1)
     * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/list.md#%E8%8E%B7%E5%8F%96%E6%8C%87%E5%AE%9A%E8%AF%84%E8%AE%BA%E7%9A%84%E5%9B%9E%E5%A4%8D">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/list.md#获取指定评论的回复</a>
     */
    public static void replyReply(
            CookieJar cookieJar,
            int type,
            int oid,
            long root,
            Integer ps,
            Integer pn,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();

        HttpUrl.Builder httpUrlBuilder = HttpUrl.get("https://api.bilibili.com/x/v2/reply/reply").newBuilder();
        httpUrlBuilder.addQueryParameter("type", String.valueOf(type));
        httpUrlBuilder.addQueryParameter("oid", String.valueOf(oid));
        httpUrlBuilder.addQueryParameter("root", String.valueOf(root));
        if (ps != null) httpUrlBuilder.addQueryParameter("ps", String.valueOf(ps));
        if (pn != null) httpUrlBuilder.addQueryParameter("pn", String.valueOf(pn));

        Request getRequest = new Request.Builder()
                .url(httpUrlBuilder.build())
                .build();
        client.newCall(getRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }

    /**
     * 评论区操作 - 点赞评论
     * @param type 评论区类型代码*
     * @param oid 目标评论区 id*
     * @param rpid 目标评论rpid*
     * @param like 操作 (true: 点赞, false: 取消赞)
     * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/action.md#%E7%82%B9%E8%B5%9E%E8%AF%84%E8%AE%BA">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/docs/comment/action.md#点赞评论</a>
     */
    public static void replyAction(
            BiliCookieJar cookieJar,
            int type,
            int oid,
            long rpid,
            boolean like,
            API.JsonDataCallback<JSONObject> callback
    ) {
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .build();
        FormBody formBody = new FormBody.Builder()
                .add("type", String.valueOf(type))
                .add("oid", String.valueOf(oid))
                .add("rpid", String.valueOf(rpid))
                .add("action", String.valueOf(like ? 1 : 0))
                .add("csrf", cookieJar.bili_jct)
                .build();
        Request postRequest = new Request.Builder()
                .url("https://api.bilibili.com/x/v2/reply/action")
                .post(formBody)
                .build();
        client.newCall(postRequest).enqueue(new API.OkhttpJsonDataCallback<>(callback));
    }
}
