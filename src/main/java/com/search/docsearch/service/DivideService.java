package com.search.docsearch.service;

import java.io.IOException;
import java.util.Map;

import com.search.docsearch.entity.vo.SearchDocs;

public interface DivideService {

    Map<String, Object> advancedSearch(Map<String, String> search, String category) throws Exception;

    Map<String, Object> docsSearch(SearchDocs searchDocs) throws IOException;
}
