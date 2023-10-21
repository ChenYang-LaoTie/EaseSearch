package com.search.docsearch.controller;


import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.search.docsearch.aop.LimitRequest;
import com.search.docsearch.aop.LogAction;
import com.search.docsearch.config.MySystem;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SearchTags;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.SearchService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;
    @Autowired
    @Qualifier("setConfig")
    private MySystem s;

    /**
     * 查询文档，首页大搜索
     *
     * @param condition 封装查询条件
     * @return 搜索结果
     */
    @LogAction(type = "Global search", OperationResource = "Documents")
    @PostMapping("docs")
    @LimitRequest()
    public SysResult searchDocByKeyword(@RequestBody @Validated SearchCondition condition) {
        try {
            Map<String, Object> result = searchService.searchByCondition(condition);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("searchByCondition error is: " + e.getMessage());
        }
        return SysResult.fail("查询失败", null);
    }

    @LogAction(type = "Get aid", OperationResource = "Suggestion words")
    @PostMapping("sugg")
    @LimitRequest()
    public SysResult getSuggestion(@RequestBody @Validated SearchCondition condition) {
        if (!StringUtils.hasText(condition.getKeyword())) {
            return SysResult.fail("keyword must not null", null);
        }

        try {
            Map<String, Object> result = searchService.getSuggestion(condition.getKeyword(), condition.getLang());
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (IOException e) {
            log.error("getSuggestion error is: " + e.getMessage());
        }
        return SysResult.fail("查询失败", null);
    }


    @LogAction(type = "Get statistics", OperationResource = "Statistics data")
    @PostMapping("count")
    @LimitRequest()
    public SysResult getCount(@RequestBody @Validated SearchCondition condition) {
        try {
            Map<String, Object> result = searchService.getCount(condition);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("getCount error is: " + e.getMessage());
        }
        return SysResult.fail("查询失败", null);
    }



    @LogAction(type = "Get aid", OperationResource = "Popular terms")
    @PostMapping("pop")
    @LimitRequest(callTime = 1, callCount = 1000)
    public SysResult getPop(String lang) {
        try {
            String[] result = null;
            if (lang.equals("zh")) {
                result = new String[]{"迁移", "openGauss", "yum", "安装", "白皮书", "生命周期", "docker", "虚拟化"};
            } else {
                result = new String[]{"migration", "openGauss", "doc", "openstack", "cla"};
            }

            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("getPop error is: " + e.getMessage());
        }
        return SysResult.fail("查询失败", null);
    }


    @LogAction(type = "Filter documents", OperationResource = "Documents")
    @PostMapping("sort")
    @LimitRequest()
    public SysResult makeSort(@RequestBody Map<String, String> m) {
        try {
            Map<String, Object> result = searchService.advancedSearch(m);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("advancedSearch error is: " + e.getMessage());
        }


        return SysResult.fail("查询失败", null);
    }

    @LogAction(type = "Get", OperationResource = "Tags")
    @PostMapping("tags")
    @LimitRequest()
    public SysResult getTags(@RequestBody @Validated SearchTags searchTags) {
        try {
            Map<String, Object> result = searchService.getTags(searchTags);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error("getTags error is: " + e.getMessage());
        }


        return SysResult.fail("查询失败", null);
    }

}
