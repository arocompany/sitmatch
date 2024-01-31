package com.nex.search.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class SerpApiImageResultForYandex {

    private String error;

    private List<Images_resultsByImageForYandex> image_results;
}