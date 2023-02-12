package vip.cdms.wearmanga.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONType;

import java.util.regex.Pattern;

public class StringUtils {
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
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
}
