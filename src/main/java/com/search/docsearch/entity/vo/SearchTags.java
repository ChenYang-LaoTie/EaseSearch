package com.search.docsearch.entity.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SearchTags implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "lang can not be null")
    private String lang;

    private String category;
    
    @NotBlank(message = "want can not be null")
    private String want;

    private Map<String, String> condition;

}


