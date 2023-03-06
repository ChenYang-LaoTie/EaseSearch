package com.search.docsearch.controller;


import com.search.docsearch.config.MySystem;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SearchTags;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
     * 查询文档
     *
     * @param condition 封装查询条件
     * @return 搜索结果
     */
    @PostMapping("docs")
    public SysResult searchDocByKeyword(@RequestBody SearchCondition condition) {
        if (!StringUtils.hasText(condition.getKeyword())) {
            return SysResult.fail("keyword must not null", null);
        }
//        condition.setKeyword(condition.getKeyword().replace("#", ""));
        try {
            Map<String, Object> result = searchService.searchByCondition(condition);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return SysResult.fail("查询失败", null);
    }

    @PostMapping("sugg")
    public SysResult getSuggestion(@RequestBody SearchCondition condition) {
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
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return SysResult.fail("查询失败", null);
    }


    @PostMapping("count")
    public SysResult getCount(@RequestBody SearchCondition condition) {
        try {
            Map<String, Object> result = searchService.getCount(condition);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return SysResult.fail("查询失败", null);
    }

    @PostMapping("pop")
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
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return SysResult.fail("查询失败", null);
    }


    @PostMapping("sort")
    public SysResult makeSort(@RequestBody Map<String, String> m) {

        try {
            Map<String, Object> result = searchService.advancedSearch(m);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return SysResult.fail("查询失败", null);
    }

    @PostMapping("tags")
    public SysResult getTags(@RequestBody SearchTags searchTags) {
        try {
            Map<String, Object> result = searchService.getTags(searchTags);
            if (result == null) {
                return SysResult.fail("内容不存在", null);
            }
            return SysResult.ok("查询成功", result);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return SysResult.fail("查询失败", null);
    }

}
