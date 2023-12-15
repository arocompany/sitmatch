package com.nex.search.entity;

import lombok.Data;

import java.util.List;

@Data
public class GoogleLensImagesByImageResult {

    private String error;
    // private Map<String, String> image_sources_search;
    //private List<Images_resultsByGoogleLens> visual_matches;            // 구글렌즈 결과
    private List<Images_resultsByGoogleLens> image_sources; // 구글렌즈 페이지 토큰
    // private List<Images_resultsByImage> image_sources; // 구글렌즈 페이지 토큰
    // private String page_token;
   // private List<Images_resultsByGoogleImageSources> image_sources;

}