package com.nex.search.entity.result;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Images_resultsByImageForYandex that = (Images_resultsByImageForYandex) o;
//        return link == that.link;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(link);
//    }
}