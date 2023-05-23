package com.search.docsearch.parse;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OPENEULER {
    public static final String BASEPATH = "/usr/local/docs/target/";

    public static final String BLOG = "blog";
    public static final String BLOGS = "blogs";
    public static final String DOCS = "docs";
    public static final String NEWS = "news";
    public static final String OTHER = "other";
    public static final String MIGRATION = "migration";
    public static final String SHOWCASE = "showcase";
    public static final String EVENTS = "events";
    public static final String USERPRACTICE = "userPractice";


    public static final String FORUMDOMAIM = "https://forum.openeuler.org";

    public Map<String, Object> parse(File file) throws Exception {
        String originalPath = file.getPath();
        String fileName = file.getName();
        String path = originalPath
                .replace("\\", "/")
                .replace(BASEPATH, "")
                .replace("\\\\", "/")
                .replace(".md", "")
                .replace(".html", "");

        String lang = path.substring(0, path.indexOf("/"));

        String type = path.substring(lang.length() + 1, path.indexOf("/", lang.length() + 1));
        if (!DOCS.equals(type)
                && !BLOG.equals(type)
                && !BLOGS.equals(type)
                && !NEWS.equals(type)
                && !SHOWCASE.equals(type)
                && !MIGRATION.equals(type)
                && !EVENTS.equals(type)
                && !USERPRACTICE.equals(type)) {
            type = OTHER;
            if (!fileName.equals("index.html")) {
                return null;
            }

        }
        if (type.equals(OTHER) || type.equals(SHOWCASE) || type.equals(MIGRATION)) {
            path = path.substring(0, path.length() - 5);
        }
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("lang", lang);
        jsonMap.put("type", type);
        jsonMap.put("articleName", fileName);
        jsonMap.put("path", path);

        String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        if (fileName.endsWith(".html")) {
            parseHtml(jsonMap, fileContent);
        } else {
            if (DOCS.equals(type)) {
                parseDocsType(jsonMap, fileContent, fileName, path, type);
            } else {
                parseUnDocsType(jsonMap, fileContent);
            }
        }

        if (jsonMap.get("title") == "" && jsonMap.get("textContent") == "") {
            return null;
        }
        return jsonMap;
    }


    public static void parseHtml(Map<String, Object> jsonMap, String fileContent) {
        Document node = Jsoup.parse(fileContent);
        Elements titles = node.getElementsByTag("title");
        if (titles.size() > 0) {
            jsonMap.put("title", titles.first().text());
        }

        Elements elements = node.getElementsByTag("main");
        if (elements.size() > 0) {
            Element mainNode = elements.first();
            jsonMap.put("textContent", mainNode.text());
        }
    }

    public static void parseDocsType(Map<String, Object> jsonMap, String fileContent, String fileName, String path, String type) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(fileContent);
        Document node = Jsoup.parse(renderer.render(document));

        if (node.getElementsByTag("h1").size() > 0) {
            jsonMap.put("title", node.getElementsByTag("h1").first().text());
        } else {
            jsonMap.put("title", fileName);
        }

        if (node.getElementsByTag("a").size() > 0 && node.getElementsByTag("ul").size() > 0) {
            Element a = node.getElementsByTag("a").first();
            if (a.attr("href").startsWith("#")) {
                node.getElementsByTag("ul").first().remove();
            }
        }
        jsonMap.put("textContent", node.text());

        String version = path.replaceFirst(jsonMap.get("lang") + "/" + type + "/", "");
        version = version.substring(0, version.indexOf("/"));

        jsonMap.put("version", version);
    }

    public static void parseUnDocsType(Map<String, Object> jsonMap, String fileContent) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String r = "";
        if (fileContent.contains("---")) {
            fileContent = fileContent.substring(fileContent.indexOf("---") + 3);
            if (fileContent.contains("---")) {
                r = fileContent.substring(0, fileContent.indexOf("---"));
                fileContent = fileContent.substring(fileContent.indexOf("---") + 3);
            }
        }


        Node document = parser.parse(fileContent);
        Document node = Jsoup.parse(renderer.render(document));
        jsonMap.put("textContent", node.text());

        Yaml yaml = new Yaml();
        Map<String, Object> ret = yaml.load(r);
        String key = "";
        Object value = "";
        for (Map.Entry<String, Object> entry : ret.entrySet()) {
            key = entry.getKey().toLowerCase(Locale.ROOT);
            value = entry.getValue();
            if (key.equals("date")) {
                //需要处理日期不标准导致的存入ES失败的问题。
                String dateString = "";
                if (value.getClass().getSimpleName().equals("Date")) {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    dateString = format.format(value);
                } else {
                    dateString = value.toString();
                }
                Pattern pattern = Pattern.compile("\\D"); //匹配所有非数字
                Matcher matcher = pattern.matcher(dateString);
                dateString = matcher.replaceAll("-");
                if (dateString.length() < 10) {
                    StringBuilder stringBuilder = new StringBuilder(dateString);
                    if (stringBuilder.charAt(7) != '-') {
                        stringBuilder.insert(5, "0");
                    }
                    if (stringBuilder.length() < 10) {
                        stringBuilder.insert(8, "0");
                    }
                    dateString = stringBuilder.toString();
                }
                value = dateString;
            }
            if (key.equals("author") && value instanceof String) {
                value = new String[]{value.toString()};
            }
            if (key.equals("head")) {
                continue;
            }
            jsonMap.put(key, value);
        }
        if (jsonMap.containsKey("date")) {
            jsonMap.put("archives", jsonMap.get("date").toString().substring(0, 7));
        }

    }


    public Map<String, Object> parseHook(String data) {
        int index = data.indexOf(" ");
        String parameter = data.substring(0, index);
        String value = data.substring(index);


        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("type", "forum");
        jsonMap.put("lang", "zh");
        JSONObject post = JSON.parseObject(value);
        JSONObject hookData = JSON.parseObject(post.get("post").toString());
        if (hookData.containsKey("id")) {
            jsonMap.put("eulerForumId", hookData.getString("id"));
        } else {
            return null;
        }
        if (hookData.containsKey("topic_title")) {
            jsonMap.put("title", hookData.getString("topic_title"));
        } else {
            return null;
        }
        if (hookData.containsKey("raw")) {
            jsonMap.put("textContent", hookData.getString("raw"));
        } else {
            return null;
        }
        if (hookData.containsKey("topic_slug") && hookData.containsKey("topic_id")) {
            jsonMap.put("path", "/t/" + hookData.getString("topic_slug") + "/" + hookData.getString("topic_id"));
        } else {
            return null;
        }

        //验证是否为删除
        //为了清除http请求缓存所在请求路径上加了随机数
        String p = FORUMDOMAIM + jsonMap.get("path") + "?ran=" + Math.random();
        HttpURLConnection connection = null;
        try {
            connection = sendHTTP(p, "GET");
            if (connection.getResponseCode() != 200) {
                jsonMap.put("delete", "true");
            }

        } catch (Exception e) {
            log.error("Connection failed, error is: " + e.getMessage());
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }

        return jsonMap;

    }

    public List<Map<String, Object>> customizeData() {
        List<Map<String, Object>> r = new ArrayList<>();

        if (!setForum(r)) {
            log.error("博客数据添加失败");
            return null;
        }

        if (!serviceInfo(r)) {
            log.error("服务数据添加失败");
            return null;
        }

        return r;
    }

    private boolean setForum(List<Map<String, Object>> r) {
        String path = FORUMDOMAIM + "/latest.json?no_definitions=true&page=";

        String req = "";
        HttpURLConnection connection = null;
        String result;  // 返回结果字符串
        for (int i = 0; ; i++) {
            req = path + i;
            try {
                connection = sendHTTP(req, "GET");
                TimeUnit.SECONDS.sleep(30);
                if (connection.getResponseCode() == 200) {
                    result = ReadInput(connection.getInputStream());
                    if (!setData(result, r)) {
                        break;
                    }
                } else {
                    log.error(req + " - ", connection.getResponseCode());
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                log.error("Connection failed, error is: " + e.getMessage());
                return false;
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
            }
        }
        return true;
    }

    private boolean setData(String data, List<Map<String, Object>> r) {
        JSONObject post = JSON.parseObject(data);
        JSONObject topicList = post.getJSONObject("topic_list");
        JSONArray jsonArray = topicList.getJSONArray("topics");
        if (jsonArray.size() <= 0) {
            return false;
        }
        String path = "";
        HttpURLConnection connection = null;
        String result;  // 返回结果字符串
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject topic = jsonArray.getJSONObject(i);
            String id = topic.getString("id");
            String slug = topic.getString("slug");
            path = String.format("%s/t/%s/%s.json?track_visit=true&forceLoad=true", FORUMDOMAIM, slug, id);
            try {
                connection = sendHTTP(path, "GET");
                if (connection.getResponseCode() == 200) {
                    result = ReadInput(connection.getInputStream());
                    JSONObject st = JSON.parseObject(result);
                    JSONObject postStream = st.getJSONObject("post_stream");
                    JSONArray posts = postStream.getJSONArray("posts");
                    JSONObject pt = posts.getJSONObject(0);

                    String cooked = pt.getString("cooked");
                    Parser parser = Parser.builder().build();
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    Node document = parser.parse(cooked);
                    Document node = Jsoup.parse(renderer.render(document));
                    Map<String, Object> jsonMap = new HashMap<>();
                    jsonMap.put("textContent", node.text());
                    jsonMap.put("title", st.getString("title"));
                    jsonMap.put("eulerForumId", pt.get("id"));
                    jsonMap.put("type", "forum");
                    jsonMap.put("lang", "zh");
                    jsonMap.put("path", "/t/" + slug + "/" + id);

                    r.add(jsonMap);
                } else {
                    log.error(path + " - ", connection.getResponseCode());
                }
            } catch (IOException e) {
                log.error("Connection failed, error is: " + e.getMessage());
            } finally {
                if (null != connection) {
                    connection.disconnect();
                }
            }
        }
        return true;
    }


    public boolean serviceInfo(List<Map<String, Object>> r) {
        //从ops中获取数据
        String url = "https://ops.osinfra.cn/api/app_resources/sla_export";
        HttpURLConnection connection = null;
        String result;  // 返回结果字符串
        try {
            connection = sendHTTP(url, "GET");
            if (connection.getResponseCode() != 200) {
                return false;
            }

            XSSFWorkbook workbook = new XSSFWorkbook(connection.getInputStream());
            XSSFSheet sheet = workbook.getSheet("Sla");
            int lastRowIndex = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowIndex; i++) {
                XSSFRow row = sheet.getRow(i);
                if (row.getCell(3) == null || !row.getCell(3).getStringCellValue().toLowerCase(Locale.ROOT).equals("openeuler")) {
                    continue;
                }
                if (canBeEntered(row)) {
                    Map<String, Object> jsonMap = new HashMap<>();
                    String title = row.getCell(0).getStringCellValue();
                    String textContent = row.getCell(1).getStringCellValue();
                    String path = row.getCell(2).getStringCellValue();
                    jsonMap.put("title", title);
                    jsonMap.put("textContent", textContent);
                    jsonMap.put("path", path);
                    jsonMap.put("type", "service");
                    jsonMap.put("lang", "zh");
                    r.add(jsonMap);
                }
            }


        } catch (Exception e) {
            log.error("Connection failed, error is: " + e.getMessage());
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
        return true;
    }


    private boolean canBeEntered(XSSFRow row) {
        if (row.getCell(0) == null) {
            return false;
        }
        if (row.getCell(1) == null) {
            return false;
        }
        if (row.getCell(2) == null) {
            return false;
        }
        String url = row.getCell(2).getStringCellValue();
        HttpURLConnection connection = null;
        try {
            connection = sendHTTP(url, "GET");
            if (connection.getResponseCode() != 200) {
                return false;
            }
            //如果接口未返回html界面说明是后端数据服务，不加入搜索。
            if (!connection.getContentType().contains("text/html")) {
                return false;
            }
        } catch (Exception e) {
            log.error("get - " + url + " error: " + e.getMessage());
            //有些服务经常访问超时，但是不代表服务本身不可访问。
            return e.getMessage().contains("Connection timed out");
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }

        return true;
    }

    private HttpURLConnection sendHTTP(String path, String method) throws IOException {
        URL url = new URL(path);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(60000);
        connection.setReadTimeout(60000);
        connection.connect();
        return connection;
    }

    private String ReadInput(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sbf = new StringBuilder();
        String temp = null;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
        }
        try {
            br.close();
        } catch (IOException e) {
            log.error("read input failed, error is: " + e.getMessage());
        }
        try {
            is.close();
        } catch (IOException e) {
            log.error("close stream failed, error is: " + e.getMessage());
        }
        return sbf.toString();

    }


}

