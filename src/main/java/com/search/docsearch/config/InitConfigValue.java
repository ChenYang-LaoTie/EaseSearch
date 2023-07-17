package com.search.docsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties()
public class InitConfigValue {

    @Value("${config-path}")
    private String configPath;

    @Bean
    public void deletEvnformApplication() {
        // if (FileUtils.deleteFile(configPath)) {
        //     log.info("delete application success");
        // } else {
        //     log.info("delete application fail");
        // }
        System.out.println("++++++++++++++++++" + configPath);
    }

}
