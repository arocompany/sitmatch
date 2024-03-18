package com.nex.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
@Slf4j
public class CmnUtil {
    public static final String IS_NOT_EMPTY = "VALUES_IS_NOT_EMPTY(NULL)";

    /*
     *  VO객체 내부의 원하는 변수만 NULL 및 공백 체크 순회
     *  @Param VO객체, 변수명 배열
     *  @Return NULL 값이 있을경우 해당 변수명 , 모두 NULL이 아닐경우 VALUES_STATE_GOOD 반환
     */
    public static String findEmptyValue(Object obj, String[] valueNames) throws IllegalAccessException {
        String stateCode = IS_NOT_EMPTY;
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            for (String str : valueNames) {
                if (field.getName().equals(str)) {
                    if (field.get(obj) == null || field.get(obj).toString().trim().equals("")) {
                        stateCode = str;
                        break;
                    }
                }
            }
        }
        return stateCode;
    }
    /*
     *  VO객체 내부의 원하는 변수만 NULL 및 공백 체크 순회
     *  @Param VO객체, 변수명 배열
     *  @Return NULL 값이 있을경우 true, 모두 NULL이 아닐경우 false
     */
    public static Boolean isEmptyValue(Object obj, String[] valueNames) throws IllegalAccessException {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            for (String str : valueNames) {
                if (field.getName().equals(str)) {
                    if (field.get(obj) == null || field.get(obj).toString().trim().equals("")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    public static String execPython(String[] command) throws IOException, InterruptedException {
//        log.info(" === execPython 진입 === ");
//        CommandLine commandLine = CommandLine.parse(command[0]);
//        for (int i = 1, n = command.length; i < n; i++) {
//            log.info("command.length: "+command.length);
//            log.info(" commandLine.addArgument(command[i]) :"+commandLine.addArgument(command[i]));
//            commandLine.addArgument(command[i]);
//        }
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);
//        DefaultExecutor executor = new DefaultExecutor();
//        executor.setStreamHandler(pumpStreamHandler);
//        log.info(" === execPython 중간 === ");
//
//        int result = executor.execute(commandLine);
//        log.error("result: " + result);
//        log.error("output: " + outputStream.toString());
//
//        return "";
//    }

    public static String execPython(String[] command) throws IOException, InterruptedException {
        log.info(" === execPython 진입 === ");
        CommandLine commandLine = CommandLine.parse(command[0]);
        for (int i = 1; i < command.length; i++) {
            log.info("command.length: "+command.length);
            log.info(" commandLine.addArgument(command[i]) : {}", command[i]);
            commandLine.addArgument(command[i]);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        log.info(" === execPython 중간 === {}", commandLine);

        int result = executor.execute(commandLine);
        log.error("result: " + result);
        log.error("output: " + outputStream.toString());

        return "";
    }
}
