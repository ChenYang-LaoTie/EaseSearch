package com.search.docsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.search.docsearch.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class InitConfigValue {

    @Value("${config-path}")
    private String configPath;

    @Bean
    public void deletEvnformApplication() {
        if (FileUtils.deleteFile(configPath)) {
            log.info("delete application success");
        } else {
            log.info("delete application fail");
        }
    }

}
