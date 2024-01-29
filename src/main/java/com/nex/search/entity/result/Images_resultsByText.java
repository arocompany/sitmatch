package com.nex.search.entity.result;

import lombok.Data;

@Data
public class Images_resultsByText {

    private int position;

    private String original;

    private int original_width;

    private int original_height;

    private String source;

    private String title;

    private String link;

    private String thumbnail;


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