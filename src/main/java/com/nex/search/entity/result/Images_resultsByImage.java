package com.nex.search.entity.result;

import lombok.Data;

import java.util.Objects;

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
    public boolean isTwitter() {
        return "Twitter".equals(source);
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Images_resultsByImage that = (Images_resultsByImage) o;
//        return link == that.link;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(link);
//    }
}