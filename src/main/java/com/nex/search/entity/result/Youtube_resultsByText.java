package com.nex.search.entity.result;

import lombok.Data;

import java.util.Map;

@Data
public class Youtube_resultsByText {

    private String position_on_page;

    private String title;

    private String link;

    private Map<String, String> thumbnail;


}