package com.search.docsearch.config;


import com.search.docsearch.constant.EulerTypeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@Slf4j
public class systemConfig {

    @Value("${system}")
    private String system;
    @Value("${docsversion}")
    private String docsVersion;


    @Bean
    public mySystem setConfig() {
        if (system.equalsIgnoreCase(EulerTypeConstants.SYSTEM)) {
            return new mySystem(EulerTypeConstants.SYSTEM, docsVersion, EulerTypeConstants.INDEX, EulerTypeConstants.MAPPINGPATH, EulerTypeConstants.BASEPATH, EulerTypeConstants.INITDOC, EulerTypeConstants.UPDATEDOC);
        }
        return new mySystem(EulerTypeConstants.SYSTEM, docsVersion, EulerTypeConstants.INDEX, EulerTypeConstants.MAPPINGPATH, EulerTypeConstants.BASEPATH, EulerTypeConstants.INITDOC, EulerTypeConstants.UPDATEDOC);

    }
}
