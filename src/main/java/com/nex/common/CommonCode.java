package com.nex.common;

public interface CommonCode {
    String snsTypeGoogle = "11";
    String snsTypeTwitter = "13";

    String snsTypeInstagram = "15";
    String snsTypeFacebook = "17";

    String searchTypeKeyword = "11";
    String searchTypeKeywordImage = "13";
    String searchTypeKeywordVideo = "15";
    String searchTypeImage = "17";
    String searchTypeVideo = "19";

    String searchStateIng = "11";
    String searchStateFinish = "13";
    String searchStateIngCalc = "15";
    String searchStateFinishCalc = "17";

    String searchNationCodeUs = "us";
    String searchNationCodeKr = "kr";
    String searchNationCodeCn = "cn";
    String searchNationCodeNl = "nl";
    String searchNationCodeTh = "th";
    String searchNationCodeRu = "ru";
    String searchNationCodeVn = "vn";

    String SerpAPIEngineGoogle = "google";
    String SerpAPIEngineYoutube = "youtube";
    String SerpAPIEngineGoogleLens = "google_lens";
    String SerpAPIEngineGoogleLensImageSourcesApi = "google_lens_image_sources";
    String SerpAPIEngineGoogleReverseImage = "google_reverse_image";
    String SerpAPIEngineBaidu = "baidu";
    String SerpAPIEngineBing = "bing";
    String SerpAPIEngineDuckduckgo = "duckduckgo";
    String SerpAPIEngineYahoo = "yahoo";
    String SerpAPIEngineYandex = "yandex";
    String SerpAPIEngineYandexImage = "yandex_images";
    String SerpAPIEngineNaver = "naver";

    int RequestSerpApiLogInit = 0;
    int RequestSerpApiLogSuccess = 1;
    int RequestSerpApiLogFail = 2;
}
