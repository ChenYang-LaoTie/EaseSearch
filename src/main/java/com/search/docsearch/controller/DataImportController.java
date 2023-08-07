package com.search.docsearch.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
import com.search.docsearch.service.SearchService;
import com.search.docsearch.utils.FileUtils;

import lombok.extern.slf4j.Slf4j;

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

    /**
     * 该方法在项目启动时就会运行
     * 
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
        String applictionPath = System.getenv("APPLICATION_PATH");
        if (StringUtils.hasText(applictionPath)) {
            if (FileUtils.deleteFile(applictionPath)) {
                log.info("delete application success");
            } else {
                log.info("delete application fail");
            }
        } else {
            log.info("application path is null");
        }
        
        try {
            // 导入es数据
            dataImportService.refreshDoc();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    /**
     * 对外提供的webhook
     * 
     * @param data
     * @param parameter
     */
    @PostMapping("/hook/{parameter}")
    public void webhook(@RequestBody @NotBlank(message = "hook data can not be blank") String data,
            @PathVariable @NotBlank(message = "must have a parameter") String parameter) {
        dataImportService.addForum(data, parameter);
    }

}
