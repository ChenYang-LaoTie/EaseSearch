package com.search.docsearch.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
import com.search.docsearch.service.SearchService;

@Component
@RestController
public class DataImportController {

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
