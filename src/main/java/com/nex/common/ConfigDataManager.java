package com.nex.common;

import lombok.Data;

@Data
public class ConfigDataManager {
    private ConfigData defaultConfig;
    private ConfigData subConfig;

    private static ConfigDataManager configDataManager = new ConfigDataManager();
    private ConfigDataManager(){}
    public static ConfigDataManager getInstance(){
        if (configDataManager.defaultConfig == null) {
            configDataManager.defaultConfig = new ConfigData();
            configDataManager.subConfig = new ConfigData();
        }
        return configDataManager;
    }
}
