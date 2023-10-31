package com.nex.search.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Youtube_resultsByText {

    private String position_on_page;

    private String title;

    private String link;

    private Map<String, String> thumbnail;

/*
    private String thumbnail;

    private String source_name;
*/


/*
    public boolean isInstagram() {
        return "Instagram".equals(source);
    }

    public boolean isFacebook() {
        return "Facebook".equals(source);
    }
*/

}