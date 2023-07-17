package com.search.docsearch.config;

import org.springframework.beans.factory.annotation.Value;

import com.search.docsearch.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitConfigValue {

    @Value("${config-path}")
    private static String configPath;

    public static void deletEvnformApplication() {
        if (FileUtils.deleteFile(configPath)) {
            log.info("delete application success");
        } else {
            log.info("delete application fail");
        }
    }

}
