package com.search.docsearch.controller;


import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
@RestController
public class DataImportController implements ApplicationRunner {

    @Autowired
    public SearchService searchService;

    @Autowired
    public DataImportService dataImportService;

    @Autowired
    @Qualifier("setConfig")
    private MySystem s;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Value("${kafka.need}")
    private boolean needKafka;

    @Override
    public void run(ApplicationArguments args) throws IOException {
        dataImportService.refreshDoc();

        if (needKafka) {
            dataImportService.listenKafka();
        }
    }

    @PostMapping("/hook/{parameter}")
    public void webhook(@RequestBody String data, @PathVariable String parameter) {
        dataImportService.sendKafka(data, parameter);
    }

}
