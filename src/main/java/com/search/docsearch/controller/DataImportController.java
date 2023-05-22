package com.search.docsearch.controller;


import com.search.docsearch.config.MySystem;
import com.search.docsearch.service.DataImportService;
import com.search.docsearch.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.net.URL;

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

    @Value("${kafka.need}")
    private boolean needKafka;

    /**
     *  该方法在项目启动时就会运行
     * @param args
     */
    @Override
    public void run(ApplicationArguments args) {
//        try {
//            //导入es数据
//            dataImportService.refreshDoc();
//            //如果配置钟需要kafka则启动监听
//            if (needKafka) {
//                dataImportService.listenKafka();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        OPENEULER openeuler = new OPENEULER();
//        List<Map<String, Object>> r = new ArrayList<>();
//        openeuler.serviceInfo(r);
        try {
            String path = "https://datastat.openeuler.org/query/all?community=openeuler";
            URL url = new URL(path);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.connect();
            System.out.println(connection.getContentType());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 对外提供的webhook
     * @param data
     * @param parameter
     */
    @PostMapping("/hook/{parameter}")
    public void webhook(@RequestBody String data, @PathVariable String parameter) {
        if (needKafka) {
            //将webhook接收到的数据发送到kafka
            dataImportService.sendKafka(data, parameter);
        }

    }

}
