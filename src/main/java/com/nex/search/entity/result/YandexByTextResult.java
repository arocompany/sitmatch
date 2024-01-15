package com.nex.search.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class YandexByTextResult {

    private String error;

    private List<Images_resultsByText> images_results;

}