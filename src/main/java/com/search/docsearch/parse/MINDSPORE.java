package com.search.docsearch.parse;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.search.docsearch.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MINDSPORE {

    public static final String LANG_EN = "/en/";
    public static final String LANG_ZH = "/zh-CN/";
    public static final String MINDSPORE_OFFICIAL = "https://www.mindspore.cn";

    public Map<String, Object> parse(File file) throws Exception {

        String originalPath = file.getPath();
        String fileName = file.getName();
        String path = originalPath
                .replace("\\", "/")
                .replace(Constants.BASEPATH, "")
                .replace("\\\\", "/");

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("path", path);
        jsonMap.put("articleName", fileName);

        String c = path.substring(0, path.indexOf("/"));

        switch (c) {
            case "docs" ->  jsonMap.put("components", "MindSpore");
            case "lite" -> jsonMap.put("components", "MindSpore Lite");
            case "mindpandas" -> jsonMap.put("components", "MindPandas");
            case "mindinsight" -> jsonMap.put("components", "MindInsight");
            case "mindarmour" -> jsonMap.put("components", "MindArmour");
            case "serving" -> jsonMap.put("components", "MindSpore Serving");
            case "federated" -> jsonMap.put("components", "MindSpore Federated");
            case "golden_stick" -> jsonMap.put("components", "MindSpore Golden Stick");
            case "xai" -> jsonMap.put("components", "MindSpore XAI");
            case "devtoolkit" -> jsonMap.put("components", "MindSpore Dev Toolkit");
            case "graphlearning" -> jsonMap.put("components", "MindSpore Graph Learning");
            case "reinforcement" -> jsonMap.put("components", "MindSpore Reinforcement");
            case "probability" -> jsonMap.put("components", "MindSpore Probability");
            case "hub" -> jsonMap.put("components", "MindSpore Hub");
            case "mindelec" -> jsonMap.put("components", "MindElec");
            case "mindsponge" -> jsonMap.put("components", "MindSPONGE");
            case "mindflow" -> jsonMap.put("components", "MindFlow");
            case "mindquantum" -> jsonMap.put("components", "MindQuantum");
        };



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
            if (!path.startsWith("install/")) {
                System.out.println("------------- " + path);
                jsonMap.put("lang", "zh");
            }
        }

        String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        if (path.contains("/api/") || path.contains("/api_python/")) {
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
    }

    public Boolean parseHtml(Map<String, Object> jsonMap, String fileContent) {
        String title = "";
        String textContent = "";
        Document node = Jsoup.parse(fileContent);

        Elements sections = node.getElementsByClass("section");
        if (sections.size() > 0) {

            Element one = sections.get(0);

            Elements enTitle = one.getElementsByAttributeValue("title", "Permalink to this headline");
            Elements zhTitle = one.getElementsByAttributeValue("title", "永久链接至标题");
            if (enTitle.size() > 0) {
                Element t = enTitle.get(0).parent();
                title = t.text();
                t.remove();
            } else if (zhTitle.size() > 0) {
                Element t = zhTitle.get(0).parent();
                title = t.text();
                t.remove();
            } else {
                System.out.println("https://www.mindspore.cn/" + jsonMap.get("path"));
                return false;
            }
            title = title.replaceAll("¶", "");
        } else {
            return false;
        }

        textContent = sections.text();
        jsonMap.put("title", title);
        jsonMap.put("textContent", textContent);
        return true;
    }


    public Boolean parseInstall(Map<String, Object> jsonMap, String fileContent) {
        String fileName = (String) jsonMap.get("articleName");
        if (fileName.endsWith("_en.md")) {
            jsonMap.put("lang", "en");
        } else {
            jsonMap.put("lang", "zh");
        }
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(fileContent);

        Document node = Jsoup.parse(renderer.render(document));
        Element t = node.body().child(0);
        String title = t.text();
        t.remove();
        String textContent = node.text();


        String path = (String) jsonMap.get("path");
        String v = path.substring(path.indexOf("/") + 1);
        int location = v.indexOf("/");
        if (location > 0) {
            jsonMap.put("version", v.substring(0, location));
        } else {
            jsonMap.put("version", "-");
        }

        jsonMap.put("title", title);
        jsonMap.put("textContent", textContent);
        jsonMap.put("path", "install/detail?path=" + path);
        return true;
    }


    public List<Map<String, Object>> customizeData() throws Exception {
        List<Map<String, Object>> r = new ArrayList<>();
        String path = MINDSPORE_OFFICIAL + "/selectWebNews";

        HttpURLConnection connection = null;
        String result;  // 返回结果字符串
        for (int i = 1; ; i++) {
            TimeUnit.SECONDS.sleep(10);
            try {
                JSONObject param = new JSONObject();
                param.put("category", null);
                param.put("newsTime", "");
                param.put("pageCurrent", i);
                param.put("tag", "zh");
                param.put("type", 0);
                connection = sendHTTP(path, "POST", param.toString());
                if (connection.getResponseCode() == 200) {
                    result = ReadInput(connection.getInputStream());
                    if (!setData(result, r, "zh")) {
                        break;
                    }
                } else {
                    log.error(path + " - ", connection.getResponseCode());
                    return null;
                }

                param.put("tag", "en");
                connection = sendHTTP(path, "POST", param.toString());
                if (connection.getResponseCode() == 200) {
                    result = ReadInput(connection.getInputStream());
                    if (!setData(result, r, "en")) {
                        break;
                    }
                } else {
                    log.error(path + " - ", connection.getResponseCode());
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
            }
        }


        return r;
    }


    private HttpURLConnection sendHTTP(String path, String method, String param) throws IOException {
        URL url = new URL(path);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-type", "application/json");
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(param);
        writer.flush();
        connection.connect();
        return connection;
    }

    private HttpURLConnection sendGET(String path, String method) throws IOException {
        URL url = new URL(path);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.connect();
        return connection;
    }

    private String ReadInput(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuffer sbf = new StringBuffer();
        String temp = null;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sbf.toString();

    }

    private boolean setData(String data, List<Map<String, Object>> r, String lang) {
        JSONObject post = JSON.parseObject(data);
        JSONArray records = post.getJSONArray("records");
        if (records.size() <= 0) {
            return false;
        }

        String path = "";
        HttpURLConnection connection = null;
        String result;
        for (int i = 0; i < records.size(); i++) {
            JSONObject topic = records.getJSONObject(i);
            int id = topic.getInteger("id");
            String type = "";
            if (lang.equals("zh")) {
                type = getInformationType(topic.getString("type"));
            } else {
                type = getInformationTypeEn(topic.getString("type"));
            }


            path = String.format("https://www.mindspore.cn/selectNewsInfo?id=%d", id);

            try {
                connection = sendGET(path, "GET");
                if (connection.getResponseCode() == 200) {
                    result = ReadInput(connection.getInputStream());
                    JSONObject st = JSON.parseObject(result);
                    JSONObject detail = st.getJSONObject("detail");
                    String newsDetail = detail.getString("newsDetail");
                    //双重解析转义
                    Document node = Jsoup.parse(Jsoup.parse(newsDetail).text());
                    String textContent = node.text();
                    String title = st.getString("titie");

                    String category = detail.getString("category");

                    Map<String, Object> jsonMap = new HashMap<>();

                    jsonMap.put("title", title);
                    jsonMap.put("textContent", textContent);
                    jsonMap.put("lang", lang);
                    jsonMap.put("category", category);
                    jsonMap.put("subclass", type);
                    jsonMap.put("type", "information");
                    jsonMap.put("path", "news/newschildren?id=" + id);
                    r.add(jsonMap);
                } else {
                    log.error(path + " - ", connection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
            }

        }
        return true;
    }


    public String getInformationType(String t) {
        return switch (t) {
            case "1" -> "版本发布";
            case "2" -> "技术博客";
            case "3" -> "社区活动";
            case "4" -> "新闻";
            case "5" -> "案例";
            default -> "新闻";
        };
    }

    public String getInformationTypeEn(String t) {
        return switch (t) {
            case "1" -> "Version Release";
            case "2" -> "Blogs";
            case "3" -> "Activities";
            case "4" -> "News";
            default -> "News";
        };
    }
}
