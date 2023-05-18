package com.search.docsearch.service.impl;

import com.search.docsearch.config.KafkaConfig;
import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.search.docsearch.constant.Constants;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class DataImportServiceImpl implements DataImportService {
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    @Qualifier("setConfig")
    private MySystem s;

    @Autowired
    @Qualifier("initKafka")
    private KafkaConfig kafkaConfig;

    private static final String GLOBAL_LOCK_ID = "global_lock";

    @Override
    @Async("threadPoolTaskExecutor")
    public void refreshDoc() throws IOException {
        if (!doRefresh()) {
            //如果先行条件不成立则该服务启动不更新es
            log.info("===============本次服务启动不更新文档=================");
            return;
        }

        log.info("===============开始运行bash脚本=================");
        ProcessBuilder pb = new ProcessBuilder(s.initDoc);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            log.info(line);
        }
        log.info("===============bash脚本运行完成=================");

        File indexFile = new File(s.basePath);
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
                    Class<?> c = Class.forName(className);
                    Method m = c.getMethod("parse", File.class);
                    Object map = m.invoke(c.getDeclaredConstructor().newInstance(), paresFile);
                    if (map != null) {
                        Map<String, Object> d = (Map<String, Object>) map;
                        insert(d, s.getIndex() + "_" + d.get("lang"));
                        idSet.add((String) d.get("path"));
                    }

                } catch (Exception e) {
                    log.info(paresFile.getPath());
                    log.error("error: " + e.getMessage());
                }
            }
        }

        try {
            String className = "com.search.docsearch.parse." + s.getSystem().toUpperCase(Locale.ROOT);
            Class<?> c = Class.forName(className);
            Method m = c.getMethod("customizeData");
            Object map = m.invoke(c.getDeclaredConstructor().newInstance());
            if (map == null) {
                log.error("自定义数据获取失效，不更新该部分");
                globalUnlock();
                return;
            }

            List<Map<String, Object>> d = (List<Map<String, Object>>) map;
            System.out.println("============== " + d.size());
            for (Map<String, Object> lm : d) {
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

    public void globalUnlock() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(s.index + "_" + "lock", GLOBAL_LOCK_ID);
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
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
    public void sendKafka(String data, String parameter) {
        String topic = s.getSystem() + "_search_topic";
        ProducerRecord<String, String> mess = new ProducerRecord<String, String>(topic, parameter + " " + data);
        System.out.println("producer -> " + parameter + " " + data);
        kafkaConfig.kafkaProducer.send(mess);
    }


    @Override
    @Async("threadPoolTaskExecutor")
    public void listenKafka() {
        KafkaConsumer<String, String> kafkaConsumer = kafkaConfig.kafkaConsumer;
        String topic = s.getSystem() + "_search_topic";
        kafkaConsumer.subscribe(Collections.singleton(topic));
        while (true) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    System.out.println("coo - > " + record.value() + "---" + record.topic());

                    String className = "com.search.docsearch.parse." + s.getSystem().toUpperCase(Locale.ROOT);
                    Class<?> c = Class.forName(className);
                    Method m = c.getMethod("parseHook", String.class);
                    Object map = m.invoke(c.getDeclaredConstructor().newInstance(), record.value());
                    if (map != null) {
                        Map<String, Object> d = (Map<String, Object>) map;
                        renew(d, s.getIndex() + "_" + d.get("lang"));
                    }

                } catch (Exception e) {
                    log.error("error: " + e.getMessage());
                }
            }
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
