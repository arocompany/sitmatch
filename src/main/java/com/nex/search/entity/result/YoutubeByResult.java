package com.nex.search.entity.result;

import lombok.Data;

import java.util.List;

@Data
public class YoutubeByResult {

    private String error;

    private List<Youtube_resultsByText> video_results;
}