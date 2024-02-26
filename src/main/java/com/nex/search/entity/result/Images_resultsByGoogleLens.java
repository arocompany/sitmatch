package com.nex.search.entity.result;

import lombok.Data;

@Data
public class Images_resultsByGoogleLens {
    private String title;
    private String link;
    private String thumbnail;
    private String original;
    private String actual_image_width;
    private String actual_image_height;

    public boolean isInstagram() {
        return "Instagram".equals(link);
    }
    public boolean isFacebook() {
        return "Facebook".equals(link);
    }
    public boolean isTwitter() {
        return "Twitter".equals(link);
    }

}