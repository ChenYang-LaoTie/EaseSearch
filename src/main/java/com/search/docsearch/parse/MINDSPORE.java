package com.search.docsearch.parse;

import com.search.docsearch.constant.Constants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MINDSPORE {

    public Map<String, Object> parse(File mdFile) throws Exception {
        String originalPath = mdFile.getPath();
        String fileName = mdFile.getName();
        String path = originalPath
                .replace("\\", "/")
                .replace(Constants.BASEPATH, "")
                .replace("\\\\", "/");

        System.out.println(path);

        Map<String, Object> jsonMap = new HashMap<>();
        return jsonMap;
    }
}
