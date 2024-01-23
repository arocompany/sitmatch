package com.nex.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Data
@Component
public class SitProperties {
    @Value("${python.video.module}") private String pythonVideoModule;
    @Value("${search.text.url}") private String textUrl;
    @Value("${search.text.gl}") private String textGl;
    @Value("${search.text.no_cache}") private String textNocache;
    @Value("${search.text.location}") private String textLocation;
//    @Value("${search.text.api_key}") private String textApikey;
    @Value("${search.image.engine}") private String imageEngine;
    @Value("${search.text.count.limit}") private int textCountLimit;
    @Value("${file.location1}") private String fileLocation1;
    @Value("${file.location2}") private String fileLocation2;
    @Value("${file.location3}") private String fileLocation3;
//    @Value("${search.server.url}") private String serverIp;
    @Value("${search.text.tbm}") private String textTbm;
    @Value("${search.text.engine}") private String textEngine;
}
