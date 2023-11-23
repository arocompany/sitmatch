package com.nex.Chart.dto;

import java.util.List;

public class SearchKeywordCntDto {
    private List<String> dateList;
    private List<String> cntList;
    private List<String> resultList;

    public SearchKeywordCntDto(List<String> dateList, List<String> cntList, List<String> resultList){
        this.dateList = dateList;
        this.cntList = cntList;
        this.resultList = resultList;
    }

}
