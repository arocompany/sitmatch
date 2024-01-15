package com.nex.search.entity.dto;

import com.nex.search.entity.SearchResultEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResultDto {

    private SearchResultEntity searchResultEntity;
    private String tmrSimilarity;
}
