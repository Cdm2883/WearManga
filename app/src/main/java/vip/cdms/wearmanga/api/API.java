package vip.cdms.wearmanga.api;

import android.app.Activity;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import vip.cdms.wearmanga.utils.ActivityUtils;

import java.io.IOException;

public class API {
    public interface JsonDataCallback<T> {
        void onFailure(Exception e, JSONObject json_root);

        void onResponse(T json_root_data);
    }

    public interface JsonDataCallbackAutoE<T> {
        void onResponse(T json_root_data);
    }
    public static <T> JsonDataCallback<T> getJsonDataCallbackAutoE(
            Activity activity,
            JsonDataCallbackAutoE<T> jsonDataCallbackAutoE
    ) {
        return new JsonDataCallback<T>() {
            @Override
            public void onFailure(Exception e, JSONObject json_root) {
                ActivityUtils.alert(activity, e);
            }
            @Override
            public void onResponse(T json_root_data) {
                jsonDataCallbackAutoE.onResponse(json_root_data);
            }
        };
    }

    public static class OkhttpJsonDataCallback<T> implements Callback {
        private final JsonDataCallback<T> callback;
        public OkhttpJsonDataCallback(JsonDataCallback<T> callback) {
            this.callback = callback;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            callback.onFailure(e, null);
        }
        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            String result = null;
            try {
                assert response.body() != null;
                result = response.body().string();
            } catch (IOException e) {
                callback.onFailure(e, null);
            }
            try {
                JSONObject json_root = JSONObject.parseObject(result);
                Object code_ = json_root.get("code");
                if (code_ instanceof Number) {
                    int code = (Integer) code_;
                    String msg = json_root.getString("msg");
                    if (msg == null) msg = json_root.getString("message");
                    if (code != 0) throw new BiliAPIError(code, msg);
                } else if (code_ instanceof String) {
                    String code = (String) code_;
                    String msg = json_root.getString("msg");
                    if (msg == null) msg = json_root.getString("message");
                    throw new BiliAPIError(code, msg);
                }
                callback.onResponse((T) json_root.get("data"));
            } catch (Exception e) {
                JSONObject json_root = null;
                try {
                    json_root = JSONObject.parseObject(result);
                } catch (Exception ignored) {}
                callback.onFailure(e, json_root);
            }
        }
    }
}
