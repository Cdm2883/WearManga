package vip.cdms.wearmanga.api;

import android.graphics.Bitmap;
import android.util.Log;
import com.alibaba.fastjson.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static com.google.zxing.client.j2se.MatrixToImageConfig.BLACK;
import static com.google.zxing.client.j2se.MatrixToImageConfig.WHITE;

/**
 * web端扫码登录异步接口
 * @see <a href="https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/login/login_action/QR.md">https://github.com/SocialSisterYi/bilibili-API-collect/blob/master/login/login_action/QR.md</a>
 */
public class LoginByQRCode {
    /**
     * 申请二维码Callback
     */
    public interface getQRCodeCallback {
        void onFailure(Exception e);

        /**
         * 获取成功
         * @param url 二维码内容 (登录页面 url)
         * @param qrcode_key 扫码登录秘钥 (恒为32字符)
         */
        void onResponse(String url, String qrcode_key);
    }

    /**
     * 申请二维码
     */
    public static void getQRCode(getQRCodeCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url("https://passport.bilibili.com/x/passport-login/web/qrcode/generate")
                .build();
        client.newCall(getRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    assert response.body() != null;
                    String result = response.body().string();
                    JSONObject json_root = JSONObject.parseObject(result);
                    int code = json_root.getInteger("code");
                    if (code != 0) throw new BiliAPIError(code, json_root.getString("message"));
                    JSONObject json_root_data = json_root.getJSONObject("data");
                    callback.onResponse(
                            json_root_data.getString("url"),
                            json_root_data.getString("qrcode_key")
                    );
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    /**
     * 生成二维码
     * @param str 内容
     * @param widthAndHeight 宽高 (px)
     */
    public static Bitmap createQRCode(String str, int widthAndHeight) {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }else{
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }



    /**
     * 扫码登录Callback
     */
    public interface loginCallback {
        void onFailure(Exception e);

        /**
         * 获取成功
         */
        void onResponse(String DedeUserID, String DedeUserID__ckMd5, String SESSDATA, String bili_jct);
    }

    /**
     * 扫码登录
     */
    public static void login(String qrcode_key, loginCallback callback) {
        OkHttpClient client = new OkHttpClient();
        Request getRequest = new Request.Builder()
                .url(
                        HttpUrl.get("https://passport.bilibili.com/x/passport-login/web/qrcode/poll")
                                .newBuilder()
                                .addQueryParameter("qrcode_key", qrcode_key)
                                .build()
                )
                .build();
        client.newCall(getRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    assert response.body() != null;
                    String result = response.body().string();
                    JSONObject json_root = JSONObject.parseObject(result);
                    int json_root_code = json_root.getInteger("code");
                    if (json_root_code != 0) throw new BiliAPIError(json_root_code, json_root.getString("message"));
                    JSONObject json_root_data = json_root.getJSONObject("data");
                    int json_root_data_code = json_root_data.getInteger("code");
                    if (json_root_data_code != 0) throw new BiliAPIError(json_root_data_code, json_root_data.getString("message"));

//                    assert response.priorResponse() != null;
//                    assert response.priorResponse().networkResponse() != null;
                    List<String> setCookieHeaders = response/*.priorResponse().networkResponse()*/.headers("Set-Cookie");
                    String DedeUserID = null, DedeUserID__ckMd5 = null, SESSDATA = null, bili_jct = null;
                    for (String setCookieHeader : setCookieHeaders) {
                        setCookieHeader = setCookieHeader.split(";")[0];
                        String cookieName = setCookieHeader.split("=")[0].trim();
                        String cookieContent = setCookieHeader.split("=")[1].trim();
                        if (cookieName.equalsIgnoreCase("DedeUserID")) DedeUserID = cookieContent;
                        else if (cookieName.equalsIgnoreCase("DedeUserID__ckMd5")) DedeUserID__ckMd5 = cookieContent;
                        else if (cookieName.equalsIgnoreCase("SESSDATA")) SESSDATA = cookieContent;
                        else if (cookieName.equalsIgnoreCase("bili_jct")) bili_jct = cookieContent;
                    }
                    callback.onResponse(DedeUserID, DedeUserID__ckMd5, SESSDATA, bili_jct);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onFailure(e);
                }
            }
        });
    }
}