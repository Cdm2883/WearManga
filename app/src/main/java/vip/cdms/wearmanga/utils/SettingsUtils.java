package vip.cdms.wearmanga.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import java.util.Set;

public class SettingsUtils {
    private static SharedPreferences settings = null;

    public static SharedPreferences getSettings(Context context) {
        if (settings != null) return settings;
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings;
    }

    public static String getString(String key, String defValue) {
        if (settings == null) return defValue;
        return settings.getString(key, defValue);
    }
    public static Set<String> getStringSet(String key, Set<String> defValues) {
        if (settings == null) return defValues;
        return settings.getStringSet(key, defValues);
    }
    public static int getInt(String key, int defValue) {
        if (settings == null) return defValue;
        return settings.getInt(key, defValue);
    }
    public static long getLong(String key, long defValue) {
        if (settings == null) return defValue;
        return settings.getLong(key, defValue);
    }
    public static float getFloat(String key, float defValue) {
        if (settings == null) return defValue;
        return settings.getFloat(key, defValue);
    }
    public static boolean getBoolean(String key, boolean defValue) {
        if (settings == null) return defValue;
        return settings.getBoolean(key, defValue);
    }
}
