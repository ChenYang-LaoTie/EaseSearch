package com.search.docsearch.service;

import java.io.IOException;
import java.util.Map;

import com.search.docsearch.entity.vo.SearchCondition;
import com.search.docsearch.entity.vo.SearchTags;

public interface SearchService {

    Map<String, Object> getSuggestion(String keyword, String lang) throws IOException;

    Map<String, Object> searchByCondition(SearchCondition condition) throws IOException;

    Map<String, Object> getCount(SearchCondition condition) throws IOException;

    Map<String, Object> advancedSearch(Map<String, String> search) throws Exception;

    Map<String, Object> getTags(SearchTags searchTags) throws Exception;

    String querySigName(String lang) throws Exception;

    String queryAll() throws Exception;

    String querySigReadme(String sig, String lang) throws Exception;

    String getEcosystemRepoInfo(String ecosystemType, String sortType, String sortOrder, String page,
            String pageSize, String lang) throws Exception;
}
