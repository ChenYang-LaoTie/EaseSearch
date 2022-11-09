package com.search.docsearch.controller;


import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
@Slf4j
@RestController
public class BeginFun implements ApplicationRunner {
    @Autowired
    public SearchService searchService;
    @Autowired
    @Qualifier("setConfig")
    private MySystem s;


    @Override
    public void run(ApplicationArguments args) throws IOException {
        log.info("===============开始拉取仓库资源=================");
            ProcessBuilder pb = new ProcessBuilder(s.initDoc);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                log.info(line);
            }
        searchService.refreshDoc();
    }


}
