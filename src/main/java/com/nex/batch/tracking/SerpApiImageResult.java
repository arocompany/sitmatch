package com.nex.batch.tracking;

import com.nex.search.entity.result.Images_resultsByGoogleLens;
import com.nex.search.entity.result.Images_resultsByImageForYandex;
import com.nex.search.entity.result.Youtube_resultsByText;
import lombok.Data;

import java.util.List;

@Data
public class SerpApiImageResult {

    private String error;

    //for google_revers
    private List<ImagesResult> inline_images;

    //for google
    private List<ImagesResult> images_results;

    //for youtube
    private List<Youtube_resultsByText> video_results;

    //for yandex image
    private List<Images_resultsByImageForYandex> image_results;

    //for google lens
    private List<Images_resultsByGoogleLens> image_sources;
}