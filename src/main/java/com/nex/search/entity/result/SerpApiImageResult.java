package com.nex.search.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class SerpApiImageResult {

    private String error;

    private List<Images_resultsByImage> inline_images;
}