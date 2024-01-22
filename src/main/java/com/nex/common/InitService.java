package com.nex.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitService {
    private String location;

    @PostConstruct
    public void init(){
        log.info("init Service -- start");

        try{
            CommonStaticUtil.getUploadPath();
            CommonStaticUtil.getLogPath();
            location = CommonStaticUtil.getConfigPath();

            File configFile = new File(location);
            if (configFile.exists()) {
                // 파일이 존재하면 파일에서 읽어와서 Config 클래스에 바인딩
                readConfigFromFile();
            } else {
                // 파일이 존재하지 않으면 Config 클래스의 속성 값을 사용하여 파일 생성 후 바인딩
                createConfigFile();

            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }

        log.info("init Service -- end");
    }

    private void readConfigFromFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ConfigData loadedConfig = objectMapper.readValue(new File(location), ConfigData.class);

            // 읽어온 값을 현재 Config 객체에 설정
//            config.setSomeProperty(loadedConfig.getSomeProperty());
            // 나머지 속성들도 동일하게 설정

            ConfigDataManager.getInstance().setDefaultConfig(loadedConfig);
            log.info("Config loaded from file: {}", loadedConfig.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createConfigFile() {
        try {
            // Config 클래스의 속성 값을 사용하여 새로운 Config 객체 생성
            ConfigData newConfig = new ConfigData();
            // 설정할 값들을 필요에 따라 설정
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(location), newConfig);

            log.info("Config file created: " + newConfig);
            readConfigFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void saveConfig(ConfigData config){
        try{
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            if(config.getIsBatchFlag() != null) configData.setIsBatchFlag(config.getIsBatchFlag());
            if(config.getBatchCycleByHour() != null) configData.setBatchCycleByHour(config.getBatchCycleByHour());
            if(config.getDriverClassName() != null) configData.setDriverClassName(config.getDriverClassName());
            if(config.getUrl() != null) configData.setUrl(config.getUrl());
            if(config.getUserName() != null) configData.setUserName(config.getUserName());
            if(config.getPassword() != null) configData.setPassword(config.getPassword());
            if(config.getServerUrl() != null) configData.setServerUrl(config.getServerUrl());
            if(config.getPythonVideoModule() != null) configData.setPythonVideoModule(config.getPythonVideoModule());
            if(config.getSearchServerUrl() != null) configData.setSearchServerUrl(config.getSearchServerUrl());
            if(config.getSearchYandexTextApiKey() != null) configData.setSearchYandexTextApiKey(config.getSearchYandexTextApiKey());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.writeValue(new File(location), configData);

            log.info("Config file created: " + configData);
            readConfigFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
