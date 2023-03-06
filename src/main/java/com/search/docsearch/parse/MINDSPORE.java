package com.search.docsearch.parse;

import com.search.docsearch.constant.Constants;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MINDSPORE {


    public static final String LANG_EN = "/en/";
    public static final String LANG_ZH = "/zh-CN/";

    public Map<String, Object> parse(File file) throws Exception {

        String originalPath = file.getPath();
        String fileName = file.getName();
        String path = originalPath
                .replace("\\", "/")
                .replace(Constants.BASEPATH, "")
                .replace("\\\\", "/");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("path", path);

        String c = path.substring(0, path.indexOf("/"));
        String components = switch (c) {
            case "docs" -> "MindSpore";
            case "lite" -> "MindSpore Lite";
            case "mindpandas" -> "MindPandas";
            case "mindinsight" -> "MindInsight";
            case "mindarmour" -> "MindArmour";
            case "serving" -> "MindSpore Serving";
            case "federated" -> "MindSpore Federated";
            case "golden_stick" -> "MindSpore Golden Stick";
            case "xai" -> "MindSpore XAI";
            case "devtoolkit" -> "MindSpore Dev Toolkit";
            case "graphlearning" -> "MindSpore Graph Learning";
            case "reinforcement" -> "MindSpore Reinforcement";
            case "probability" -> "MindSpore Probability";
            case "hub" -> "MindSpore Hub";
            case "mindelec" -> "MindElec";
            case "mindsponge" -> "MindSPONGE";
            case "mindflow" -> "MindFlow";
            case "mindquantum" -> "MindQuantum";
            default -> c;
        };

        jsonMap.put("components", components);


        if (path.contains(LANG_EN)) {
            jsonMap.put("lang", "en");
            String v = path.substring(path.indexOf(LANG_EN) + LANG_EN.length());
            String version = v.substring(0, v.indexOf("/"));
            jsonMap.put("version", version);
        } else if (path.contains(LANG_ZH)) {
            jsonMap.put("lang", "zh");
            String v = path.substring(path.indexOf(LANG_ZH) + LANG_ZH.length());
            String version = v.substring(0, v.indexOf("/"));
            jsonMap.put("version", version);
        } else {
            jsonMap.put("lang", "zh");
        }

        String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        if (path.contains("/api/")) {
            jsonMap.put("type", "api");
            if (!parseHtml(jsonMap, fileContent)) {
                return null;
            }
        } else if (path.startsWith("tutorials/")) {
            jsonMap.put("type", "tutorials");
            if (!parseHtml(jsonMap, fileContent)) {
                return null;
            }
        } else if (path.startsWith("install/")) {
            jsonMap.put("type", "install");
            if (!parseInstall(jsonMap, fileContent)) {
                return null;
            }
        } else {
            jsonMap.put("type", "docs");
            if (!parseHtml(jsonMap, fileContent)) {
                return null;
            }
        }
        return jsonMap;
//        return null;
    }

    public Boolean parseHtml(Map<String, Object> jsonMap, String fileContent) {
        String title = "";
        String textContent = "";
        Document node = Jsoup.parse(fileContent);

        Elements sections = node.getElementsByClass("section");
        if (sections.size() > 0) {
            textContent = sections.text();
            Element one = sections.get(0);

            Elements enTitle = one.getElementsByAttributeValue("title","Permalink to this headline");
            Elements zhTitle = one.getElementsByAttributeValue("title","永久链接至标题");
            if (enTitle.size() > 0) {
                title = enTitle.get(0).parent().text();
            } else if (zhTitle.size() > 0) {
                title = zhTitle.get(0).parent().text();
            } else {
                System.out.println("https://www.mindspore.cn/" + jsonMap.get("path"));
                return false;
            }
        } else {
            return false;
        }
        jsonMap.put("title", title);
        jsonMap.put("textContent", textContent);
        return true;
    }


    public Boolean parseInstall(Map<String, Object> jsonMap, String fileContent) {
        return false;
    }

}
