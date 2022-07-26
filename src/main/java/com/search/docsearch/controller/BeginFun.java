package com.search.docsearch.controller;


import com.search.docsearch.config.mySystem;
import com.search.docsearch.constant.EulerTypeConstants;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class BeginFun implements ApplicationRunner {
    @Autowired
    public SearchService searchService;
    @Autowired
    @Qualifier("setConfig")
    private mySystem s;


    @Override
    public void run(ApplicationArguments args) throws IOException {

        try {
            log.info("===============开始拉取仓库资源=================");
            ProcessBuilder pb = new ProcessBuilder(s.initDoc);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                log.info(line);
            }

            log.info("===============仓库资源拉取成功=================");
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        searchService.refreshDoc();
    }


}
