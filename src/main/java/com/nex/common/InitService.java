package com.nex.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.SitMatchApplication;
import com.nex.batch.ScheduleTasks;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitService {
    private String location;
    private final ServletWebServerApplicationContext context;
    private final ScheduleTasks scheduleTasks;

    @PostConstruct
    public void init(){
        log.info("init Service -- start");

        try{

            String osName = System.getProperty("os.name").toLowerCase();

            // 플랫폼에 따른 루트 디렉토리 결정
            String rootDir;
            if (osName.contains("win")) {
                // Windows일 경우
                rootDir = "C:/";
            } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("mac")) {
                // Unix 또는 macOS일 경우
                rootDir = "/";
            } else {
                // 다른 플랫폼에 대한 처리
                rootDir = "Unknown";
            }

            String applicationName = context.getApplicationName();
            if(!StringUtils.hasText(applicationName)){
                applicationName = "/temp";
            }

            String filePath = "app/{applicationName}/config";
            String configName = "/app.config";
            filePath = rootDir + filePath.replaceAll("/\\{applicationName}", applicationName);

            location = filePath + configName;

            // 파일이 존재하는지 확인
            File configFile = new File(filePath);
            if (configFile.exists() && configFile.isDirectory()) {

                // 파일이 존재하면 파일에서 읽어와서 Config 클래스에 바인딩
                readConfigFromFile();
            } else {
                // 파일이 존재하지 않으면 Config 클래스의 속성 값을 사용하여 파일 생성 후 바인딩
                if(configFile.mkdirs()) {
                    createConfigFile();
                }
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

            scheduleTasks.stopScheduler();
            scheduleTasks.startScheduler();
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
