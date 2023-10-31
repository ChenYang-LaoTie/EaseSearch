package com.search.docsearch.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchDocs {

    @NotBlank(message = "lang can not be null")
    @Pattern(regexp = "((^zh$|^en$|^ZH$|^EN$))")
    private String lang;

    @Range(min = 1, max = 100, message = "page must be greater than 0 and less than 100 ")
    private int page = 1;

    @Range(min = 5, max = 20, message = "pageSize must be greater than 5 and less than 20 ")
    private int pageSize = 10;
    
    @NotBlank(message = "keyword can not be null")
    @Pattern(regexp = "^[\\u4E00-\\u9FA5A-Za-z0-9.\\-_]+$", message = "Keyword format is invalid")
    @Size(max = 20)
    private String keyword;
    
    @Pattern(regexp = "^[\\x20a-zA-Z0-9.\\-_]*$", message = "Version format is invalid.")
    @Size(max = 50)
    private String version;
}
