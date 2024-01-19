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

    @Override
    public String toString(){
        return "ConfigData{" +
                "isBatchFlag=" + isBatchFlag +
                ", batchCycleByHour=" + batchCycleByHour +
                ", driverClassName='" + driverClassName + '\'' +
                ", url='" + url + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", pythonVideoModule='" + pythonVideoModule + '\'' +
                ", searchServerUrl='" + searchServerUrl + '\'' +
                ", searchYandexTextApiKey='" + searchYandexTextApiKey + '\'' +
                '}';
    }
}
