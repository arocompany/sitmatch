package com.nex.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.ServerInfo;
import org.springframework.util.StringUtils;

import java.io.File;

@Slf4j
public class CommonStaticUtil {
    public static String getRootDir(){
        String osName = System.getProperty("os.name").toLowerCase();

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

        String filePath = "app/{applicationName}/{tomcatVersion}";
        filePath = filePath.replaceAll("\\{applicationName}", getApplicationName());
        filePath = filePath.replaceAll("\\{tomcatVersion}", getTomcatVersion());
        filePath = rootDir + filePath;

        return filePath;
    }
    public static String getApplicationName(){
        String applicationName = "";
        String[] packageName = CommonStaticUtil.class.getPackage().getName().toString().split("\\.");
        if(packageName != null && packageName.length > 1){
            applicationName = packageName[1];
            if(!StringUtils.hasText(applicationName)){
                applicationName = "/temp";
            }
        }else{
            applicationName = "/temp";
        }

        applicationName = applicationName.replaceAll("/", "");

        return applicationName;
    }

    public static String getTomcatVersion(){
        String tomcatVersion = ServerInfo.getServerNumber();

        tomcatVersion = "tomcat"+tomcatVersion.replaceAll("/", "").toLowerCase();
        log.info("tomcatVersion " + tomcatVersion.toString());
        return tomcatVersion;
    }

    public static String getConfigPath(){
        String filePath = getRootDir();
        String configPath = "/config";
        String configName = "/app.config";

        File locationConfigPath = new File(filePath + configPath);
        if(! locationConfigPath.exists()){
            locationConfigPath.mkdirs();
        }

        return filePath + configPath + configName;
    }

    public static void getLogPath(){
        String logPath = "/log";
        String filePath = getRootDir();

        File locationLogPath = new File(filePath + logPath);
        if(! locationLogPath.exists()){
            locationLogPath.mkdirs();
        }
        System.setProperty("logFilePath", filePath + logPath);
    }
    public static void getUploadPath(){
        String uploadPath = "/upload";
        String filePath = getRootDir();

        File locationUploadPath = new File(filePath + uploadPath);
        if(! locationUploadPath.exists()){
            locationUploadPath.mkdirs();
        }
    }
}
