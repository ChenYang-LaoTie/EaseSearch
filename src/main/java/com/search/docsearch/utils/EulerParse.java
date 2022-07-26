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

        Node document = parser.parse(fileContent);

        Document node = Jsoup.parse(renderer.render(document));
        if (EulerTypeConstants.DOCS.equals(type)) {
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
            Element tag = node.getElementsByTag("hr").first();
            if (tag != null) {
                Element t = tag.nextElementSibling();
                r = t.text();
                t.remove();
                jsonMap.put("textContent", node.text());
            }

            if (EulerTypeConstants.SHOWCASE.equals(type)) {
                jsonMap.put("title", EulerGetValue(r, "company"));
                jsonMap.put("industry", EulerGetValue(r, "industry"));
                jsonMap.put("company", EulerGetValue(r, "company"));
                jsonMap.put("summary", EulerGetValue(r, "summary"));
                jsonMap.put("img", EulerGetValue(r, "img"));
            } else {
                jsonMap.put("title", EulerGetValue(r, "title"));
            }
        }

        return jsonMap;
    }


    public static String EulerGetValue(String r, String t) {
        if (!r.contains(t)) {
            return "";
        }
        String m = ":";

        r = r.substring(r.indexOf(t) + t.length());
        r = r.substring(r.indexOf(m) + m.length());

        if (!r.contains(":")) {
            return r.trim().replaceAll("\"", "");
        }

        r = r.substring(0, r.indexOf(":"));
        r = r.substring(0, r.lastIndexOf(" ")).trim();
        r = r.replaceAll("\"", "");
        return r;
    }

}
