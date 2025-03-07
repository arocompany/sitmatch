package com.nex.search.entity.result;

import lombok.Data;

@Data
public class CustomResults {
    private int position;
    private String source;
    private String title;
    private String link;
    private String image;

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
