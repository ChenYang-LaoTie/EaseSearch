package com.search.docsearch.utils;

import java.io.File;

public class FileUtils {

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

}
