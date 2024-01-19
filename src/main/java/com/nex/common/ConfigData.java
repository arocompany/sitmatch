package com.nex.common;

import lombok.Data;

@Data
public class ConfigData {
    private Boolean isBatchFlag;
    private Integer batchCycleByHour;

    private String driverClassName;
    private String url;
    private String userName;
    private String password;

    private String serverUrl;
    private String pythonVideoModule;
    private String searchServerUrl;

    private String searchYandexTextApiKey;

//
//    private String externalLocation;
}
