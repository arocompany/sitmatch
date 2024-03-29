package com.nex.batch.tracking;

import com.nex.search.entity.RequestSerpApiLogEntity;
import lombok.Data;

import java.util.Map;

@Data
public class ImagesResult {
    private Integer tsiUno;
    private String original;
    private String source;
    private String title;
    private String link;
    private String thumbnail;
    private String source_name;
    private int position;
    private int original_width;
    private int original_height;

    private Map<String, Object> thumbnailMap;
    private Map<String, Object> orginalMap;

    private RequestSerpApiLogEntity rslInfo;

    public boolean isInstagram() {
        return "Instagram".equals(source);
    }
    public boolean isFacebook() {
        return "Facebook".equals(source);
    }
    public boolean isTwitter() {
        return "Twitter".equals(source);
    }

}