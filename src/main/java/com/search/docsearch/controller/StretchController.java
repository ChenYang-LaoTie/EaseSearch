package com.search.docsearch.controller;


import com.search.docsearch.config.mySystem;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SysResult;
import com.search.docsearch.service.StretchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class StretchController {

    @Autowired
    private StretchService stretchService;
    @Autowired
    @Qualifier("setConfig")
    private mySystem s;



    /**
     * 查询文档
     *
     * @param condition 封装查询条件
     * @return 搜索结果
     */
    @PostMapping("showcase")
    public SysResult searchShowcase(@RequestBody SearchCondition condition) {
        try {
            Map<String, Object> result = stretchService.searchShowcase(condition);
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
     * 查询文档
     *
     * @return 搜索结果
     */
    @PostMapping("industry")
    public SysResult getIndustry() {
        try {
            Map<String, Object> result = stretchService.searchShowTags();
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


}
