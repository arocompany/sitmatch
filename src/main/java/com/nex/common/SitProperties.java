package com.nex.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Data
@Component
public class SitProperties {
    @Value("${python.video.module}") private String pythonVideoModule;
    @Value("${search.yandex.text.url}") private String textYandexUrl;
    @Value("${search.yandex.text.gl}") private String textYandexGl;
    @Value("${search.yandex.text.no_cache}") private String textYandexNocache;
    @Value("${search.yandex.text.location}") private String textYandexLocation;
    @Value("${search.yandex.text.api_key}") private String textYandexApikey;
    @Value("${search.yandex.image.engine}") private String imageYandexEngine;
    @Value("${search.yandex.text.count.limit}") private int textYandexCountLimit;
    @Value("${file.location1}") private String fileLocation1;
    @Value("${file.location2}") private String fileLocation2;
    @Value("${file.location3}") private String fileLocation3;
    @Value("${search.server.url}") private String serverIp;
    @Value("search.yandex.text.tbm") private String textYandexTbm;
    @Value("search.yandex.text.engine") private String textYandexEngine;
}
