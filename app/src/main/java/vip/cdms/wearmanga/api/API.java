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
    public interface JsonDataCallback {
        void onFailure(Exception e);

        void onResponse(JSONObject json_root_data);
    }

    public interface JsonDataCallbackAutoE {
        void onResponse(JSONObject json_root_data);
    }
    public static JsonDataCallback getJsonDataCallbackAutoE(
            Activity activity,
            JsonDataCallbackAutoE jsonDataCallbackAutoE
    ) {
        return new JsonDataCallback() {
            @Override
            public void onFailure(Exception e) {
                ActivityUtils.alert(activity, e);
            }
            @Override
            public void onResponse(JSONObject json_root_data) {
                jsonDataCallbackAutoE.onResponse(json_root_data);
            }
        };
    }

    public static class OkhttpJsonDataCallback implements Callback {
        private final JsonDataCallback callback;
        public OkhttpJsonDataCallback(JsonDataCallback callback) {
            this.callback = callback;
        }

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
                callback.onResponse(json_root_data);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }
    }
}
