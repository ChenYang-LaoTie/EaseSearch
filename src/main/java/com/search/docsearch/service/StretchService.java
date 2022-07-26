package com.search.docsearch.service;

import com.search.docsearch.entity.vo.SearchCondition;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StretchService {

    Map<String, Object> searchShowcase(SearchCondition condition) throws IOException;

    Map<String, Object> searchShowTags() throws IOException;
}
