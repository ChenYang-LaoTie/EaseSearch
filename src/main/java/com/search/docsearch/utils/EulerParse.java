package com.search.docsearch.utils;

import com.search.docsearch.constant.EulerTypeConstants;

import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EulerParse {


    public static Map<String, Object> parseMD(String lang, String deleteType, File mdFile) throws Exception {
        String type = deleteType;
        String fileName = mdFile.getName();
        String path = mdFile.getPath()
                .replace("\\", "/")
                .replace(EulerTypeConstants.BASEPATH + lang + "/", "")
                .replace("\\\\", "/")
                .replace(".md", "");
        if (!EulerTypeConstants.DOCS.equals(deleteType) && !EulerTypeConstants.BLOGS.equals(deleteType) && !EulerTypeConstants.NEWS.equals(deleteType) && !EulerTypeConstants.SHOWCASE.equals(deleteType)) {
            type = EulerTypeConstants.OTHER;
            if(!fileName.equals("README.md")) {
                return null;
            }
            path = path.replace("README", "");
        }


        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("lang", lang);
        jsonMap.put("deleteType", deleteType);
        jsonMap.put("type", type);
        jsonMap.put("articleName", fileName);
        jsonMap.put("path", path);

        String fileContent = FileUtils.readFileToString(mdFile, StandardCharsets.UTF_8);


        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();


        if (EulerTypeConstants.DOCS.equals(type)) {
            Node document = parser.parse(fileContent);
            Document node = Jsoup.parse(renderer.render(document));

            if (node.getElementsByTag("h1").size() > 0) {
                jsonMap.put("title", node.getElementsByTag("h1").first().text());
            } else {
                jsonMap.put("title", mdFile.getName());
            }

            if (node.getElementsByTag("a").size() > 0 && node.getElementsByTag("ul").size() > 0) {
                Element a = node.getElementsByTag("a").first();
                if (a.attr("href").startsWith("#")) {
                    node.getElementsByTag("ul").first().remove();
                }
            }
            jsonMap.put("textContent",node.text());

            String version = path.replaceFirst(type + "/", "");
            version = version.substring(0, version.indexOf("/"));

            jsonMap.put("version", version);
        } else {
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
            jsonMap.put("title", EulerGetValue(r, "title"));

            jsonMap.put("category", EulerGetValue(r, "category"));
            jsonMap.put("tags", EulerGetValue(r, "tags"));
            jsonMap.put("author", EulerGetValue(r, "author"));
            jsonMap.put("summary", EulerGetValue(r, "summary"));
            jsonMap.put("industry", EulerGetValue(r, "industry"));
            jsonMap.put("company", EulerGetValue(r, "company"));
            jsonMap.put("banner", EulerGetValue(r, "banner"));
            jsonMap.put("img", EulerGetValue(r, "img"));

            if (r.contains("date")) {
                jsonMap.put("date", EulerGetValue(r, "date").trim());
            }
            if (r.contains("archives")) {
                jsonMap.put("archives", EulerGetValue(r, "archives").trim());
            }

        }

        return jsonMap;
    }


    public static String EulerGetValue(String r, String t) {
        if (!r.contains(t)) {
            return "";
        }

        r = r.substring(r.indexOf(t) + t.length());
        r = r.substring(r.indexOf(":") + 1);

        if (r.contains("\r")) {
            r = r.substring(0, r.indexOf("\r"));
        } else if (r.contains("\n")){
            r = r.substring(0, r.indexOf("\n"));
        }

        r = r.replaceAll("\"", "").replaceAll("'", "").trim();
        return r;
    }
}
