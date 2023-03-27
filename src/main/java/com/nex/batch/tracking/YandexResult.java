package com.nex.batch.tracking;

import lombok.Data;

import java.util.List;

@Data
public class YandexResult {

    private String error;

    private List<YandexImagesResult> inline_images;

    private List<YandexImagesResult> images_results;

}