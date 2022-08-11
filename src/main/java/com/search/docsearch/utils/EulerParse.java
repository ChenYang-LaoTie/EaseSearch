package com.search.docsearch.utils;

import com.search.docsearch.constant.Constants;

import org.apache.commons.io.FileUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EulerParse {

    public static final String BLOGS = "blog";
    public static final String DOCS = "docs";
    public static final String NEWS = "news";
    public static final String OTHER = "other";
    public static final String SHOWCASE = "showcase";


    public static Map<String, Object> parseMD(String lang, String deleteType, File mdFile) throws Exception {
        String type = deleteType;
        String fileName = mdFile.getName();
        String path = mdFile.getPath()
                .replace("\\", "/")
                .replace(Constants.BASEPATH + lang + "/", "")
                .replace("\\\\", "/")
                .replace(".md", "");
        if (!DOCS.equals(deleteType) && !BLOGS.equals(deleteType) && !NEWS.equals(deleteType) && !SHOWCASE.equals(deleteType)) {
            type = OTHER;
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


        if (DOCS.equals(type)) {
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

            String key = "";
            Object value = "";
            for (Map.Entry<String, Object> entry : ret.entrySet()) {
                //TODO 需要处理日期不标准导致的存入ES失败的问题。
                key = entry.getKey();
                value = entry.getValue();
                if (key.equals("archives")) {
                    value = value.toString().substring(0, 7);
                }
                jsonMap.put(key, value);
            }
        }
        if (jsonMap.get("title") == "" || jsonMap.get("textContent") == "") {
            return null;
        }

        return jsonMap;
    }

}
