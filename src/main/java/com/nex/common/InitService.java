package com.nex.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitService {
    private String location;
    private final ServletWebServerApplicationContext context;

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
            log.info("Config loaded from file: " + loadedConfig);
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
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(location), config);

            log.info("Config file created: " + config);
            readConfigFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
