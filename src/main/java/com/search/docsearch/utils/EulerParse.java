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
import org.yaml.snakeyaml.Yaml;

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


            Yaml yaml = new Yaml();
            Map<String, Object> ret = yaml.load(r);
            for (Map.Entry<String, Object> entry : ret.entrySet()) {
                jsonMap.put(entry.getKey(), entry.getValue());
            }
        }

        return jsonMap;
    }


    public static String EulerGetValue(String r, String t) {

        if (!r.contains("\n"+t)) {
            return "";
        }

        r = r.substring(r.indexOf("\n"+t) + t.length() + 1);
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
