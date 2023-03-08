package com.search.docsearch.parse;

import com.search.docsearch.constant.Constants;
import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        jsonMap.put("articleName", fileName);

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
            if (!path.startsWith("install/")) {
                System.out.println("------------- " + path);
                jsonMap.put("lang", "zh");
            }
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
    }

    public Boolean parseHtml(Map<String, Object> jsonMap, String fileContent) {
        String title = "";
        String textContent = "";
        Document node = Jsoup.parse(fileContent);

        Elements sections = node.getElementsByClass("section");
        if (sections.size() > 0) {

            Element one = sections.get(0);

            Elements enTitle = one.getElementsByAttributeValue("title","Permalink to this headline");
            Elements zhTitle = one.getElementsByAttributeValue("title","永久链接至标题");
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
        Element t =  node.body().child(0);
        String title =t.text();
        t.remove();
        String textContent = node.text();

        jsonMap.put("title", title);
        jsonMap.put("textContent", textContent);
        return true;
    }


    public List<Map<String, Object>> customizeData() {
        List<Map<String, Object>> r = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        try {

            String url = System.getenv("murl");
            String username = System.getenv("musername");
            String password = System.getenv("mpassword");
            url = "jdbc:mysql://192.168.1.203:3306/website?useUnicode=true&characterEncoding=utf-8&useSSL=false";
            username = "root";
            password = "root";


            conn = DriverManager.getConnection(url,username,password);

            stmt = conn.createStatement();

            String sql;

            sql = "SELECT * FROM website.webnews;";
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                Map<String, Object> jsonMap = new HashMap<>();
                String id = rs.getString("id");
                if (id == null || id.isBlank()) {
                    continue;
                }

                String s = "SELECT newsDetail FROM website.newsdetail WHERE newsId = ?;";
                pstmt = conn.prepareStatement(s);
                pstmt.setString(1, id);
                ResultSet rd = pstmt.executeQuery();
                String textContent = "";
                while (rd.next()) {
                    String detail = rd.getString("newsDetail");
                    //双重解析转义
                    Document node = Jsoup.parse(Jsoup.parse(detail).text());
                    textContent = node.text();
                }

                String title = rs.getString("newsTitle");
                if (title == null || title.isBlank()) {
                    continue;
                }

                String lang = rs.getString("tag");
                if (lang == null || lang.isBlank()) {
                    continue;
                }

                String category = rs.getString("category");
                if (category != null) {
                    category = "";
                }

                String type = rs.getString("type");
                if (type == null || type.isBlank()) {
                    type = "0";
                }
                type = getInformationType(type);


                jsonMap.put("title", title);
                jsonMap.put("textContent", textContent);
                jsonMap.put("lang", lang);
                jsonMap.put("category", category);
                jsonMap.put("subclass", type);
                jsonMap.put("type", "Information");

                r.add(jsonMap);
            }

            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
                if(pstmt!=null) pstmt.close();
            }catch(SQLException se2){
                System.out.println(se2.getMessage());
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        return r;
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
}
