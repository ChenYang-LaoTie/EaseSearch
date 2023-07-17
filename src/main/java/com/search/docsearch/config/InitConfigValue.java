package com.search.docsearch.config;

import com.search.docsearch.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitConfigValue {

    // @Value("${config-path}")
    // private static String configPath;
    private static String configPath = "src/main/resources/application.properties";

    public static void deletEvnformApplication() {
        if (FileUtils.deleteFile(configPath)) {
            log.info("delete application success");
        } else {
            log.info("delete application fail");
        }
    }

}
