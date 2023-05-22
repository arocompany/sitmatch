package com.nex.search.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchInfoDto {

    private SearchInfoEntity searchInfoEntity;
    private String srchProgPer;
    private String tsiKeywordHiddenValue; // url에 들어가는 hidden값
}
