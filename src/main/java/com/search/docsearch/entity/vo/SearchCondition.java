package com.search.docsearch.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "lang can not be null")
    private String lang;

    @Min(value = 1, message = "page must be greater than 0")
    private int page = 1;

    @Max(value = 100, message = "pageSize must be less than 100")
    private int pageSize = 10;

    @NotBlank(message = "keyword can not be null")
    private String keyword;
    
    private String type;
    private List<Map<String, String>> limit;
    private List<Map<String, String>> filter;
}
