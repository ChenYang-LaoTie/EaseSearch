package com.search.docsearch.entity.vo;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchTags{

    @NotBlank(message = "lang can not be null")
    @Pattern(regexp = "((^zh$|^en$|^ZH$|^EN$))")
    private String lang;

    @Pattern(regexp = "((^migration$|^news$|^forum$|^blog$|^docs$|^showcase$|^other$|^service$))", message = "category format is invalid")
    private String category;
    
    @NotBlank(message = "want can not be null")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-/_]+$", message = "want format is invalid.")
    @Size(max = 20)
    private String want;

    private Map<String, String> condition;

}


