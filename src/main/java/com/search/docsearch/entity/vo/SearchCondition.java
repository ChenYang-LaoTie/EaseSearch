package com.search.docsearch.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private String lang;
    private int page = 1;
    private int pageSize = 10;
    private String keyword;
    private String type;
    //    private List<Map<String, String>> limit;
//    private List<Map<String, String>> filter;
    private String docsVersion;
}
