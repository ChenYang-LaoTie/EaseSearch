package com.search.docsearch.service.impl;

import com.search.docsearch.config.MySystem;
import com.search.docsearch.entity.Article;
import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SearchTags;
import com.search.docsearch.service.SearchService;
import com.search.docsearch.utils.EulerParse;
import com.search.docsearch.utils.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.term.TermSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    @Qualifier("setConfig")
    private MySystem s;


    @Override
    public void refreshDoc() throws IOException {
        File indexFile = new File(s.basePath);
        if (!indexFile.exists()) {
            log.error(String.format("%s 文件夹不存在", indexFile.getPath()));
            log.error("服务器开小差了");
            return;
        }
        log.info("开始更新es文档");
        String lang = "";
        String deleteType = "";
        File[] typeDir;
        File[] languageDir = indexFile.listFiles();
        assert languageDir != null;
        for (File languageFile : languageDir) {
            lang = languageFile.getName();
            String saveIndex = s.index + "_" + lang;
            try {
                makeIndex(saveIndex);
            } catch (Exception e) {
                log.error(e.getMessage());
                continue;
            }

            typeDir = languageFile.listFiles();
            assert typeDir != null;
            for (File typeFile : typeDir) {
                if (typeFile.isDirectory()) {
                    BulkRequest bulkRequest = new BulkRequest();
                    deleteType = typeFile.getName();
                    Collection<File> listFiles = FileUtils.listFiles(typeFile, new String[]{"md", "html"}, true);
                    System.out.println(lang + "/" + deleteType + " -- " + listFiles.size());
                    for (File mdFile : listFiles) {
                        if (!mdFile.getName().startsWith("_")) {
                            try {
                                Map<String, Object> map = EulerParse.parse(lang, deleteType, mdFile);
                                if (map != null) {
                                    IndexRequest indexRequest = new IndexRequest(saveIndex).id(IdUtil.getId()).source(map);
                                    bulkRequest.add(indexRequest);
                                }
                            } catch (Exception e) {
                                log.info(mdFile.getPath());
                                log.error(e.getMessage());
                            }
                        }
                    }
                    log.info(deleteType + " have " + bulkRequest.requests().size());
                    DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(saveIndex);
                    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                    boolQueryBuilder.must(new TermQueryBuilder("deleteType.keyword", deleteType));
                    deleteByQueryRequest.setQuery(boolQueryBuilder);
                    BulkByScrollResponse r = restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
                    if (bulkRequest.requests().size() > 0) {
                        BulkResponse q = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

                        log.info("wrong ? " + q.hasFailures());
                        log.info(lang + "/" + deleteType + "更新成功");
                    }

                }
            }
        }
        log.info("所有文档更新成功");
    }

    public void makeIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        if (exists) {
            return;
        }

        CreateIndexRequest request1 = new CreateIndexRequest(index);
        File mappingJson = FileUtils.getFile(s.mappingPath);
        String mapping = FileUtils.readFileToString(mappingJson, StandardCharsets.UTF_8);

        request1.mapping(mapping, XContentType.JSON);
        request1.setTimeout(TimeValue.timeValueMillis(1));

        restHighLevelClient.indices().create(request1, RequestOptions.DEFAULT);
    }

    @Override
    @Async("threadPoolTaskExecutor")
    public void AsyncrefreshDoc() throws IOException {
        boolean success = false;
        try {
            log.info("===============开始更新仓库资源=================");
            ProcessBuilder pb = new ProcessBuilder(s.updateDoc);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                log.info(line);
                if (line.contains("build complete in")) {
                    success = true;
                }
            }
            log.info("===============仓库资源更新成功=================");
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        if (success) {
            log.info("全局更新成功!");
        } else {
            log.info("全局更新失败，开始局部更新");
            ProcessBuilder pb = new ProcessBuilder(s.updateLocal);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }

        refreshDoc();
    }


    public Map<String, Object> getSuggestion(String keyword, String lang) throws IOException {
        String saveIndex = s.index + "_" + lang;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        SuggestionBuilder<TermSuggestionBuilder> termSuggestionBuilder =
                SuggestBuilders.termSuggestion("textContent")
                        .text(keyword)
                        .minWordLength(2)
                        .prefixLength(0)
                        .analyzer("ik_smart")
                        .size(3)
                        .suggestMode(TermSuggestionBuilder.SuggestMode.ALWAYS);

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("my_sugg", termSuggestionBuilder);

        SearchRequest suggRequest = new SearchRequest(saveIndex);

        suggRequest.source(searchSourceBuilder.suggest(suggestBuilder));

        SearchResponse suggResponse = restHighLevelClient.search(suggRequest, RequestOptions.DEFAULT);

        List<String> suggestList = new ArrayList<>();
        for (int i = 0; i <= 3; i++) {
            StringBuilder sb = new StringBuilder();
            boolean isNew = false;
            for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> my_sugg : suggResponse.getSuggest().getSuggestion("my_sugg")) {
                String op = my_sugg.getText().string();
                if (my_sugg.getOptions().size() > i) {
                    op = my_sugg.getOptions().get(i).getText().string();
                    isNew = true;
                }
                sb.append(op).append(" ");
            }
            if (isNew) {
                suggestList.add(sb.toString());
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("suggestList", suggestList);
        return result;
    }


    @Override
    public Map<String, Object> searchByCondition(SearchCondition condition) throws IOException {
        String saveIndex = s.index + "_" + condition.getLang();

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", condition.getKeyword());

        SearchRequest request = BuildSearchRequest(condition, saveIndex);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Article> data = new ArrayList<>();

        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, Object> map = hit.getSourceAsMap();
            StringBuilder highLight = new StringBuilder(map.get("textContent").toString());
            String title = map.getOrDefault("title", "").toString();
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("textContent")) {
                highLight = new StringBuilder();
                for (Text textContent : highlightFields.get("textContent").getFragments()) {
                    highLight.append(textContent.toString()).append("<br>");
                }
            }
            if (highlightFields.containsKey("title")) {
                title = highlightFields.get("title").getFragments()[0].toString();
            }
            Article article = new Article().setId(hit.getId())
                    .setArticleName(map.get("articleName").toString())
                    .setLang(map.getOrDefault("lang", "").toString())
                    .setPath(map.getOrDefault("path", "").toString())
                    .setTitle(title)
                    .setVersion(map.getOrDefault("version", "").toString())
                    .setType(map.getOrDefault("type", "").toString())
                    .setTextContent(highLight.toString());
            data.add(article);
        }
        if (data.isEmpty()) {
            return null;
        }


        result.put("page", condition.getPage());
        result.put("pageSize", condition.getPageSize());
        result.put("records", data);
        return result;
    }

    public SearchRequest BuildSearchRequest(SearchCondition condition, String index) {
        int startIndex = (condition.getPage() - 1) * condition.getPageSize();
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (StringUtils.hasText(condition.getType())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("type.keyword", condition.getType()));
        }

        MatchPhraseQueryBuilder ptitleMP = QueryBuilders.matchPhraseQuery("title", condition.getKeyword()).slop(2);
        ptitleMP.boost(200);
        MatchPhraseQueryBuilder ptextContentMP = QueryBuilders.matchPhraseQuery("textContent", condition.getKeyword()).slop(2);
        ptextContentMP.boost(100);

        boolQueryBuilder.should(ptitleMP).should(ptextContentMP);

        MatchQueryBuilder titleMP = QueryBuilders.matchQuery("title", condition.getKeyword());
        titleMP.boost(2);
        MatchQueryBuilder textContentMP = QueryBuilders.matchQuery("textContent", condition.getKeyword());
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
        sourceBuilder.from(startIndex).size(condition.getPageSize());
        sourceBuilder.timeout(TimeValue.timeValueMinutes(1L));
        request.source(sourceBuilder);
        return request;
    }


    public Map<String, Object> getCount(String keyword, String lang) throws IOException {
        String saveIndex = s.index + "_" + lang;
        SearchRequest request = new SearchRequest(saveIndex);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        MatchQueryBuilder titleMP = QueryBuilders.matchQuery("title", keyword);
        titleMP.boost(2);
        MatchQueryBuilder textContentMP = QueryBuilders.matchQuery("textContent", keyword);
        textContentMP.boost(1);
        boolQueryBuilder.should(titleMP).should(textContentMP);
        boolQueryBuilder.minimumShouldMatch(1);

        sourceBuilder.query(boolQueryBuilder);

        sourceBuilder.aggregation(AggregationBuilders.terms("data").field("type.keyword"));
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map<String, Object>> numberList = new ArrayList<>();
        Map<String, Object> numberMap = new HashMap<>();
        numberMap.put("doc_count", response.getHits().getTotalHits().value);
        numberMap.put("key", "all");
        numberList.add(numberMap);
        ParsedTerms aggregation = response.getAggregations().get("data");
        List<? extends Terms.Bucket> buckets = aggregation.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Map<String, Object> countMap = new HashMap<>();
            countMap.put("key", bucket.getKeyAsString());
            countMap.put("doc_count", bucket.getDocCount());
            numberList.add(countMap);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("total", numberList);
        return result;
    }

    @Override
    public Map<String, Object> advancedSearch(Map<String, String> search) throws Exception {
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

            HighlightBuilder highlightBuilder = new HighlightBuilder()
                    .field("textContent")
                    .field("title")
                    .fragmentSize(100)
                    .preTags("<span>")
                    .postTags("</span>");
            sourceBuilder.highlighter(highlightBuilder);
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
    public Map<String, Object> getTags(SearchTags searchTags) throws Exception {
        String saveIndex = s.index + "_" + searchTags.getLang();

        SearchRequest request = new SearchRequest(saveIndex);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery("category.keyword", searchTags.getCategory()));


        if (searchTags.getCondition() != null) {
            for (Map.Entry<String, String> entry : searchTags.getCondition().entrySet()) {
                boolQueryBuilder.filter(QueryBuilders.termQuery(entry.getKey() + ".keyword", entry.getValue()));
            }
        }

        BucketOrder bucketOrder = BucketOrder.key(false);
        sourceBuilder.aggregation(AggregationBuilders.terms("data").field(searchTags.getWant() + ".keyword").size(10000).order(bucketOrder));
        sourceBuilder.query(boolQueryBuilder);
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
