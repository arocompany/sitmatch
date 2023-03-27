package com.nex.search.entity;

import lombok.Data;

@Data
public class Images_resultsByImage {

    private String original;

    private String source;

    private String title;

    private String link;

    private String thumbnail;

    private String source_name;


    public boolean isInstagram() {
        return "Instagram".equals(source);
    }

    public boolean isFacebook() {
        return "Facebook".equals(source);
    }

}