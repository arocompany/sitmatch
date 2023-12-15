package com.nex.search.entity;

import lombok.Data;

@Data
public class Images_resultsByGoogleLens {
    private String title;
    private String link;
    private String thumbnail;
    private String actual_image_width;
    private String actual_image_height;

    public boolean isInstagram() {
        return "Instagram".equals(link);
    }
    public boolean isFacebook() {
        return "Facebook".equals(link);
    }

}