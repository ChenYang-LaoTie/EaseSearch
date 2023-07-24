package com.search.docsearch.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.entity.vo.SearchDocs;
import com.search.docsearch.service.DivideService;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class DivideServiceImpl implements DivideService {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    @Qualifier("trackerClient")
    private RestHighLevelClient trackerClient;

    @Autowired
    @Qualifier("setConfig")
    private MySystem s;


    @Override
    public Map<String, Object> advancedSearch(Map<String, String> search, String category) throws Exception {
        String saveIndex;
        String lang = search.get("lang");
        if (lang != null) {
            saveIndex = s.index + "_" + lang;
        } else {
            //在没有传语言时默认为zh
            saveIndex = s.index + "_zh";
        }
        SearchRequest request = new SearchRequest(saveIndex);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery("type" + ".keyword", category));


        int page = 1;
        int pageSize = 10;
        String keyword = "";

        for (Map.Entry<String, String> entry : search.entrySet()) {
            if (entry.getKey().equals("page")) {
                page = Integer.parseInt(entry.getValue());
                continue;
            }
            if (entry.getKey().equals("pageSize")) {
                pageSize = Integer.parseInt(entry.getValue());
                continue;
            }
            if (entry.getKey().equals("keyword")) {
                keyword = entry.getValue();
                continue;
            }

            boolQueryBuilder.filter(QueryBuilders.termQuery(entry.getKey() + ".keyword", entry.getValue()));
        }

        int startIndex = (page - 1) * pageSize;

        if (!keyword.equals("")) {
            MatchQueryBuilder titleMP = QueryBuilders.matchQuery("title", keyword);
            titleMP.boost(2);
            MatchQueryBuilder textContentMP = QueryBuilders.matchQuery("textContent", keyword);
            textContentMP.boost(1);
            boolQueryBuilder.should(titleMP).should(textContentMP);

            boolQueryBuilder.minimumShouldMatch(1);
        }

        sourceBuilder.from(startIndex).size(pageSize);
        sourceBuilder.query(boolQueryBuilder);

        sourceBuilder.sort("date", SortOrder.DESC);

        request.source(sourceBuilder);


        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            data.add(map);
        }
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("count", response.getHits().getTotalHits().value);
        result.put("records", data);
        return result;
    }

    @Override
    public Map<String, Object> docsSearch(SearchDocs searchDocs) throws IOException {
        String saveIndex = s.index + "_" + searchDocs.getLang();

        Map<String, Object> result = new HashMap<>();

        int startIndex = (searchDocs.getPage() - 1) * searchDocs.getPageSize();
        SearchRequest request = new SearchRequest(saveIndex);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.termQuery("type.keyword", "docs"));

        if (StringUtils.hasText(searchDocs.getVersion())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("version.keyword", searchDocs.getVersion()));
        }

        MatchPhraseQueryBuilder ptitleMP = QueryBuilders.matchPhraseQuery("title", searchDocs.getKeyword()).analyzer("ik_max_word").slop(2);
        ptitleMP.boost(200);
        MatchPhraseQueryBuilder ptextContentMP = QueryBuilders.matchPhraseQuery("textContent", searchDocs.getKeyword()).analyzer("ik_max_word").slop(2);
        ptextContentMP.boost(100);

        boolQueryBuilder.should(ptitleMP).should(ptextContentMP);

        MatchQueryBuilder titleMP = QueryBuilders.matchQuery("title", searchDocs.getKeyword());
        titleMP.boost(2);
        MatchQueryBuilder textContentMP = QueryBuilders.matchQuery("textContent", searchDocs.getKeyword());
        textContentMP.boost(1);
        boolQueryBuilder.should(titleMP).should(textContentMP);

        boolQueryBuilder.minimumShouldMatch(1);

        sourceBuilder.query(boolQueryBuilder);

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("textContent")
                .field("title")
                .fragmentSize(100)
                .preTags("<span>")
                .postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        sourceBuilder.from(startIndex).size(searchDocs.getPageSize());
        sourceBuilder.timeout(TimeValue.timeValueMinutes(1L));
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map<String, Object>> data = new ArrayList<>();

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            String text = (String)map.getOrDefault("textContent", "");
            if (text.length() > 200) {
                text = text.substring(0, 200) + "......";
            }
            map.put("textContent", text);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("textContent")) {
                StringBuilder highLight = new StringBuilder();
                for (Text textContent : highlightFields.get("textContent").getFragments()) {
                    highLight.append(textContent.toString()).append("<br>");
                }
                map.put("textContent", highLight.toString());
            }

            if (highlightFields.containsKey("title")) {
                map.put("title", highlightFields.get("title").getFragments()[0].toString());
            }

            data.add(map);
        }
        if (data.isEmpty()) {
            return null;
        }

        result.put("page", searchDocs.getPage());
        result.put("pageSize", searchDocs.getPageSize());
        result.put("count", response.getHits().getTotalHits().value);
        result.put("records", data);
        return result;
    }

}
