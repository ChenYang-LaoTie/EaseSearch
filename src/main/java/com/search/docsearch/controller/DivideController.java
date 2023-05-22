package com.search.docsearch.controller;


import com.search.docsearch.entity.vo.SearchDocs;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.DivideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/search/sort")
public class DivideController {
    @Autowired
    private DivideService divideService;



    @PostMapping("/{type}")
    public SysResult DivideBLog(@PathVariable String type, @RequestBody Map<String, String> m){

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

    @PostMapping("docs")
    public SysResult DivideDocs(@RequestBody SearchDocs searchDocs) {
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
