package com.nex.search.entity.result;

import lombok.Data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Images_resultsByGoogleLens that = (Images_resultsByGoogleLens) o;
        return link == that.link;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}