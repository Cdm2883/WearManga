package vip.cdms.wearmanga.utils;

import com.alibaba.fastjson.JSONArray;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-+]?\\d*$");
        return pattern.matcher(str).matches();
    }

    public static String shortTitle(String short_title) {
        if (isInteger(short_title))
            return "第 " + short_title + " 话";
        else return short_title;
    }

    public static String join(String delimiter, JSONArray jsonArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Object jsonElement : jsonArray) {
            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(delimiter);

            if (jsonElement == null) stringBuilder.append("null");
            else if (jsonElement instanceof String) stringBuilder.append((String) jsonElement);
            else if (jsonElement instanceof Boolean) stringBuilder.append((boolean) jsonElement);
            else if (jsonElement instanceof Number) stringBuilder.append(jsonElement);
            else if (jsonElement instanceof JSONArray) stringBuilder.append(join(delimiter, (JSONArray) jsonElement));
        }
        return stringBuilder.toString();
    }
    public static String join(String delimiter, int[] array) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int item : array) {
            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(delimiter);
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }

    public static String biliImageUrl(String imageUrl, int width, int height) {
//        Pattern pattern = Pattern.compile("^*.hdslb.com/bfs/.+/.+\\..*$");
        Pattern pattern = Pattern.compile("^.*.hdslb.com/bfs/.+/.+\\..*$");
        Matcher matcher = pattern.matcher(imageUrl);
        if (matcher.matches()) {
            double imageSize = SettingsUtils.getInt("image_size", 100) * 0.01;
            int imageQuality = SettingsUtils.getInt("image_quality", 100);
            StringBuilder append = new StringBuilder("@");
            if (width != -1) append.append((int) (width * imageSize)).append("w");
            if (width != -1 && height != -1) append.append("_");
            if (height != -1) append.append((int) (height * imageSize)).append("h");
            if ((width != -1 || height != -1) && imageQuality != 100) append.append("_");
            if (imageQuality != 100) append.append(imageQuality).append("q.webp");
            imageUrl = imageUrl + append;
        }
        return imageUrl;
    }
}
