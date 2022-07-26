package com.search.docsearch.service.impl;

import com.search.docsearch.config.mySystem;
import com.search.docsearch.constant.EulerTypeConstants;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.service.StretchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StretchServiceImpl implements StretchService {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    @Qualifier("setConfig")
    private mySystem s;
    @Override
    public Map<String, Object> searchShowcase(SearchCondition condition) throws IOException {
        int startIndex = (condition.getPage() - 1) * condition.getPageSize();
        SearchRequest request = new SearchRequest(s.index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.termQuery("type", EulerTypeConstants.SHOWCASE));

        if (StringUtils.hasText(condition.getKeyword())) {
            MatchQueryBuilder titleMP = QueryBuilders.matchQuery("title", condition.getKeyword());
            titleMP.boost(2);
            MatchQueryBuilder textContentMP = QueryBuilders.matchQuery("textContent", condition.getKeyword());
            textContentMP.boost(1);
            boolQueryBuilder.should(titleMP).should(textContentMP);
            boolQueryBuilder.minimumShouldMatch(1);
        }

        if (StringUtils.hasText(condition.getType())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("industry.keyword", condition.getType()));
        }


        sourceBuilder.query(boolQueryBuilder);
        request.source(sourceBuilder);
        sourceBuilder.from(startIndex).size(condition.getPageSize());
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<Map<String, Object>> data = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();

            data.add(map);
        }
        if (data.isEmpty()) {
            return null;
        }


        Map<String, Object> result = new HashMap<>();
        result.put("page", condition.getPage());
        result.put("pageSize", condition.getPageSize());
        result.put("records", data);
        return result;
    }

    @Override
    public Map<String, Object> searchShowTags() throws IOException {
        SearchRequest request = new SearchRequest(s.index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.termQuery("type", EulerTypeConstants.SHOWCASE));

        sourceBuilder.aggregation(AggregationBuilders.terms("data").field("industry.keyword")).size(100);
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        ParsedTerms aggregation = response.getAggregations().get("data");
        List<Map<String, Object>> numberList = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("key", bucket.getKeyAsString());
            countMap.put("count", bucket.getDocCount());
            numberList.add(countMap);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalNum", numberList);
        return result;
    }


}
