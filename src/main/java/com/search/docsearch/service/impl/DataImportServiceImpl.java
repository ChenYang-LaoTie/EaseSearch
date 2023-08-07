package com.search.docsearch.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.constant.Constants;
import com.search.docsearch.service.DataImportService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DataImportServiceImpl implements DataImportService {
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    @Qualifier("setConfig")
    private MySystem s;

    private static final String GLOBAL_LOCK_ID = "global_lock";

    @Override
    @Async("threadPoolTaskExecutor")
    public void refreshDoc() {
        if (!doRefresh()) {
            //如果先行条件不成立则该服务启动不更新es
            log.info("===============本次服务启动不更新文档=================");
            return;
        }

        File indexFile = new File(s.getTargetPath());
        if (!indexFile.exists()) {
            log.error(String.format("%s 文件夹不存在", indexFile.getPath()));
            log.error("服务器开小差了");
            globalUnlock();
            return;
        }

        log.info("开始更新es文档");

        Set<String> idSet = new HashSet<>();
        Collection<File> listFiles = FileUtils.listFiles(indexFile, new String[]{"md", "html"}, true);
        for (File paresFile : listFiles) {
            if (!paresFile.getName().startsWith("_")) {
                try {
                    String className = "com.search.docsearch.parse." + s.getSystem().toUpperCase(Locale.ROOT);
                    Class<?> clazz = Class.forName(className);
                    Method method = clazz.getMethod("parse", File.class);
                    Object result = method.invoke(clazz.getDeclaredConstructor().newInstance(), paresFile);
                    if (result != null) {
                        Map<String, Object> escape = (Map<String, Object>) result;
                        insert(escape, s.getIndex() + "_" + escape.get("lang"));
                        idSet.add((String) escape.get("path"));
                    }

                } catch (Exception e) {
                    log.info(paresFile.getPath());
                    log.error("error: " + e.getMessage());
                }
            }
        }

        try {
            String className = "com.search.docsearch.parse." + s.getSystem().toUpperCase(Locale.ROOT);
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod("customizeData");
            Object result = method.invoke(clazz.getDeclaredConstructor().newInstance());
            if (result == null) {
                log.error("自定义数据获取失效，不更新该部分");
                globalUnlock();
                return;
            }

            List<Map<String, Object>> escape = (List<Map<String, Object>>) result;
            System.out.println("============== " + escape.size());
            for (Map<String, Object> lm : escape) {
                insert(lm, s.getIndex() + "_" + lm.get("lang"));
                idSet.add((String) lm.get("path"));
            }

        } catch (Exception e) {
            log.error("error: " + e.getMessage());
            globalUnlock();
            return;
        }

        deleteExpired(idSet);

        log.info("所有文档更新成功");
        globalUnlock();
    }


    public boolean doRefresh() {
        try {
            makeIndex(s.index + "_" + "lock", null);
            //如果检测到有超时锁，先删除锁
            GetRequest getRequest = new GetRequest(s.index + "_" + "lock", GLOBAL_LOCK_ID);
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
                String postDate = (String) getResponse.getSource().get("postDate");
                SimpleDateFormat bjSdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                bjSdf.setTimeZone(TimeZone.getTimeZone(Constants.SHANGHAI_TIME_ZONE));
                Date date = bjSdf.parse(postDate);
                if ((new Date().getTime() - date.getTime()) > Constants.MILLISECONDS_OF_A_DAY) {
                    globalUnlock();
                }
            }

            //使用es实现本次全局锁，对本次操作加锁
            globalLock();

            //创建index
            makeIndex(s.index + "_" + "zh", s.mappingPath);
            makeIndex(s.index + "_" + "en", s.mappingPath);
            makeIndex(s.index + "_" + "ru", s.mappingPath);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public void globalLock() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        Date date = new Date();
        SimpleDateFormat bjSdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        bjSdf.setTimeZone(TimeZone.getTimeZone(Constants.SHANGHAI_TIME_ZONE));
        jsonMap.put("postDate", bjSdf.format(date));

        IndexRequest indexRequest = new IndexRequest(s.index + "_" + "lock").id(GLOBAL_LOCK_ID).source(jsonMap);
        indexRequest.opType(DocWriteRequest.OpType.CREATE);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    public void globalUnlock() {
        try {
            DeleteRequest deleteRequest = new DeleteRequest(s.index + "_" + "lock", GLOBAL_LOCK_ID);
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("Failed to unlock index, the error is: " + e.getMessage());
        }
    }

    public void renew(Map<String, Object> data, String index) throws Exception {
        DeleteRequest deleteRequest = new DeleteRequest(index, (String) data.get("path"));
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

        if (data.containsKey("delete")) {
            return;
        }

        IndexRequest indexRequest = new IndexRequest(index).id((String) data.get("path")).source(data);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

    }

    public void insert(Map<String, Object> data, String index) throws Exception {
        IndexRequest indexRequest = new IndexRequest(index).id((String) data.get("path")).source(data);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    public void makeIndex(String index, String mappingPath) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        if (exists) {
            return;
        }

        CreateIndexRequest request1 = new CreateIndexRequest(index);
        if (StringUtils.hasText(mappingPath)) {
            File mappingJson = FileUtils.getFile(mappingPath);
            String mapping = FileUtils.readFileToString(mappingJson, StandardCharsets.UTF_8);

            request1.mapping(mapping, XContentType.JSON);
            request1.setTimeout(TimeValue.timeValueMillis(1));
        }
        restHighLevelClient.indices().create(request1, RequestOptions.DEFAULT);
    }

    @Override
    public void addForum(String data, String parameter) {
        try {
            String className = "com.search.docsearch.parse." + s.getSystem().toUpperCase(Locale.ROOT);
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod("parseHook", String.class);
            Object result = method.invoke(clazz.getDeclaredConstructor().newInstance(), parameter + " " + data);
            if (result != null) {
                Map<String, Object> escape = (Map<String, Object>) result;
                renew(escape, s.getIndex() + "_" + escape.get("lang"));
            }
        } catch (Exception e) {
            log.error("error: " + e.getMessage());
        }
        
    }

    @Override
    public void deleteExpired(Set<String> idSet) {
        try {
            long st = System.currentTimeMillis();
            int scrollSize = 500;//一次读取的doc数量
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());//读取全量数据
            searchSourceBuilder.size(scrollSize);
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(10));//设置一次读取的最大连接时长
            SearchRequest searchRequest = new SearchRequest(s.getIndex() + "_*");
            searchRequest.source(searchSourceBuilder);
            searchRequest.scroll(scroll);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            String scrollId = searchResponse.getScrollId();

            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                if (!idSet.contains(hit.getId())) {
                    DeleteRequest deleteRequest = new DeleteRequest(hit.getIndex(), hit.getId());
                    DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                }
            }


            while (hits.length > 0) {
                SearchScrollRequest searchScrollRequestS = new SearchScrollRequest(scrollId);
                searchScrollRequestS.scroll(scroll);
                SearchResponse searchScrollResponseS = restHighLevelClient.scroll(searchScrollRequestS, RequestOptions.DEFAULT);
                scrollId = searchScrollResponseS.getScrollId();

                hits = searchScrollResponseS.getHits().getHits();
                for (SearchHit hit : hits) {
                    if (!idSet.contains(hit.getId())) {
                        DeleteRequest deleteRequest = new DeleteRequest(hit.getIndex(), hit.getId());
                        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
                    }
                }
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);

            restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            System.out.println("time:" + (System.currentTimeMillis() - st));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

}
