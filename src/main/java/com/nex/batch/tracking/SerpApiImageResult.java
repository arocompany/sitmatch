package com.nex.batch.tracking;

import lombok.Data;

import java.util.List;

@Data
public class SerpApiImageResult {

    private String error;

    private List<ImagesResult> inline_images;

    private List<ImagesResult> images_results;

}