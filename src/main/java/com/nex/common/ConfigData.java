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

    private String pythonVideoModule;
    private String hostImageUrl;

    private String serpApiKey;

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
                ", pythonVideoModule='" + pythonVideoModule + '\'' +
                ", hostImageUrl='" + hostImageUrl + '\'' +
                ", serpApiKey='" + serpApiKey + '\'' +
                '}';
    }
}
