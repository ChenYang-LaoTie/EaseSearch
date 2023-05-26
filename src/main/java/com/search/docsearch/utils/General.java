package com.search.docsearch.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class General {
    static final String REGEX_CHINESE = "[\u4e00-\u9fa5]";// 中文正则
    private static final HashMap<String, String> REPLACE_MAP = new HashMap<String, String>() {{
        put("_", " ");
        put("&", " ");
        put("+", " ");
    }};

    public static String removeChinese(String str) {
        Pattern pat = Pattern.compile(REGEX_CHINESE);
        Matcher mat = pat.matcher(str);
        return mat.replaceAll(" ");
    }

    public static boolean haveChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String replacementCharacter(String str) {
        for (Map.Entry<String, String> entry : REPLACE_MAP.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        String reply = str;
        return reply;
    }

}
