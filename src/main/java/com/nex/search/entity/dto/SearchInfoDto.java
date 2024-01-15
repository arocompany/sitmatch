package com.nex.search.entity.dto;

import com.nex.search.entity.SearchInfoEntity;
import lombok.Data;

@Data
public class SearchInfoDto {

    private SearchInfoEntity searchInfoEntity;
    private String srchProgPer;
    private String tsiKeywordHiddenValue; // url에 들어가는 hidden값
}
