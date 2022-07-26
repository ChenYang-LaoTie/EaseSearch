package com.search.docsearch.controller;


import com.search.docsearch.config.mySystem;
import com.search.docsearch.constant.EulerTypeConstants;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class SearchController {

    @Autowired
    private SearchService searchService;
    @Autowired
    @Qualifier("setConfig")
    private mySystem s;

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
        condition.setKeyword(condition.getKeyword().replace("#", ""));
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


    /**
     * 定时任务
     */
    @Scheduled(cron = "${scheduled.cron}")
    public String scheduledTask() throws IOException {
        Process process;
        try {
            log.info("===============开始拉取仓库资源=================");
            process = Runtime.getRuntime().exec(s.updateDoc);
            process.waitFor();
            List<String> result = IOUtils.readLines(process.getInputStream(), StandardCharsets.UTF_8);
            log.info(result.toString());
            log.info("===============仓库资源拉取成功=================");
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }

        searchService.refreshDoc();
        return "success";
    }
}
