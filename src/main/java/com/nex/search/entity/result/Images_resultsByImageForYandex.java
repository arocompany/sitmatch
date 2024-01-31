package com.nex.search.entity.result;

import lombok.Data;

import java.util.Map;

@Data
public class Images_resultsByImageForYandex {

    private Map<String, Object> original_image;

    private String source;

    private String title;

    private String link;

    private Map<String, Object> thumbnail;

    private String source_name;


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