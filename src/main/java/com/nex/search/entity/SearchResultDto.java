package com.nex.search.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResultDto {

    private SearchResultEntity searchResultEntity;
    private String tmrSimilarity;
}
