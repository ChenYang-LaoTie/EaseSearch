package com.search.docsearch.config;


import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties()
@Slf4j
public class SystemConfig {

    @Value("${system}")
    private String system;
    @Value("${dep}")
    private String dep;

    @Bean
    public MySystem setConfig() {
        log.info("system -> " + system);
        system = system.toLowerCase(Locale.ROOT);
        dep = dep.toLowerCase(Locale.ROOT);
        MySystem mySystem = new MySystem();

        mySystem.setSystem(system);

        if (dep.equals("test")) {
            mySystem.setIndex(system + "_test");
        } else {
            mySystem.setIndex(system + "_articles");
        }
        mySystem.setTrackerIndex(system + "_tracker");

        return mySystem;
    }
}
