package com.nex.search.entity.result;

import com.nex.search.entity.SearchResultEntity;
import lombok.Data;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Images_resultsByText that = (Images_resultsByText) o;
        return link == that.link;
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}