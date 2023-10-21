package com.search.docsearch.controller;


import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import com.search.docsearch.aop.LimitRequest;
import com.search.docsearch.aop.LogAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.search.docsearch.entity.vo.SearchDocs;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.DivideService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/search/sort")
public class DivideController {
    @Autowired
    private DivideService divideService;



    @LogAction(type = "Category search", OperationResource = "Other Documents")
    @PostMapping("/{type}")
    @LimitRequest(callTime = 1, callCount = 30)
    public SysResult DivideBLog(@PathVariable @NotBlank(message = "must have a type") String type, @RequestBody @NotEmpty(message = "Requires at least one condition") Map<String, String> m){

        try {
            Map<String, Object> result = divideService.advancedSearch(m, type);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("advancedSearch error is: " + e.getMessage());
        }


        return SysResult.fail("查询失败", null);
    }

    @LogAction(type = "Category search", OperationResource = "Documents")
    @PostMapping("docs")
    @LimitRequest(callTime = 1, callCount = 30)
    public SysResult DivideDocs(@RequestBody @Validated SearchDocs searchDocs) {
        try {
            Map<String, Object> result = divideService.docsSearch(searchDocs);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("docsSearch error is: " + e.getMessage());
        }


        return SysResult.fail("查询失败", null);
    }

}
