package com.search.docsearch.utils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParameterUtil {
    public static void vaildMap(Map<String, String> condition) {
        if (condition != null && condition.size() > 0) {
            for (String key : condition.keySet()) {
                if (!key.matches("^[a-zA-Z]+$") || key.length() > 50) {
                    throw new IllegalArgumentException("Invalid key");
                }
                if (!condition.get(key).matches("^[\\x20\\u4E00-\\u9FA5A-Za-z0-9.\\-_]+$")
                        || condition.get(key).length() > 50) {
                    throw new IllegalArgumentException("Invalid value");
                }
            }
        }
    }

    public static void vaildListMap(List<Map<String, String>> conditionList) {
        if (conditionList != null) {
            for (Map<String, String> condition : conditionList) {
                vaildMap(condition);
            }
        }
    }

    public static String vaildLang(String lang) {
        lang = lang == null ? "zh" : lang;
        if (!lang.equalsIgnoreCase("zh") && !lang.equalsIgnoreCase("en")) {
            throw new IllegalArgumentException("Invalid lang parameter");
        }
        return lang.toLowerCase(Locale.ROOT);
    }

    public static String vaildCommunity(String community) {
        if (!"mindspore".equalsIgnoreCase(community) && !"opengauss".equalsIgnoreCase(community) && !"openlookeng".equalsIgnoreCase(community)) {
            throw new IllegalArgumentException("Invalid community parameter");
        }
        return community.toLowerCase(Locale.ROOT);
    }

    public static String vaildEcosystemType(String ecosystemType) {
        ecosystemType = ecosystemType == null ? "Library" : ecosystemType;
        if (!"Library".equalsIgnoreCase(ecosystemType) && !"Tutorial".equalsIgnoreCase(ecosystemType) && !"Model".equalsIgnoreCase(ecosystemType)) {
            throw new IllegalArgumentException("Invalid ecosystemType parameter");
        }
        return ecosystemType;
    }

    public static String vaildSortType(String sortType) {
        sortType = sortType == null ? "star" : sortType;
        if (!"star".equalsIgnoreCase(sortType) && !"name".equalsIgnoreCase(sortType) && !"repo".equalsIgnoreCase(sortType)) {
            throw new IllegalArgumentException("Invalid sortType parameter");
        }
        return sortType;
    }

    public static String vaildSortOrder(String sortOrder) {
        sortOrder = sortOrder == null ? "desc" : sortOrder;
        if (!"desc".equalsIgnoreCase(sortOrder) && !"asc".equalsIgnoreCase(sortOrder)) {
            throw new IllegalArgumentException("Invalid sortOrder parameter");
        }
        return sortOrder;
    }

    public static String vaildPage(String page) {
        page = page == null ? "1" : page;
        if (Integer.parseInt(page) > 100 || Integer.parseInt(page) < 1) {
            throw new IllegalArgumentException("Invalid page parameter");
        }
        return page;
    }

    public static String vaildPageSize(String pageSize) {
        pageSize = pageSize == null ? "20" : pageSize;
        if (Integer.parseInt(pageSize) > 20 || Integer.parseInt(pageSize) < 5) {
            throw new IllegalArgumentException("Invalid pageSize parameter");
        }
        return pageSize;
    }
}
