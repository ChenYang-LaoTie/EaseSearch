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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        log.info("===============开始拉取仓库资源=================");
        ProcessBuilder pb = new ProcessBuilder(s.initDoc);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            log.info(line);
        }
        dataImportService.refreshDoc();
        if (needKafka) {
            dataImportService.listenKafka();
        }
    }


    /**
     * 定时任务
     */
    @GetMapping("updoc")
//    @Scheduled(cron = "${scheduled.cron}")
    public String asyncrefreshDoc() throws IOException {
        dataImportService.asyncrefreshDoc();
        return "Now try to start updating the document, it will take about three minutes";
    }

    @PostMapping("/hook/{parameter}")
    public void webhook(@RequestBody String data, @PathVariable String parameter) {
        dataImportService.sendKafka(data, parameter);
    }

    @GetMapping("syn")
    public void scheduledTask() {
        dataImportService.refreshSynIndex();
    }

}
