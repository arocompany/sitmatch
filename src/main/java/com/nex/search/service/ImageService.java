package com.nex.search.service;

import com.nex.common.CommonStaticSearchUtil;
import com.nex.search.entity.SearchResultEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ResourceLoader resourceLoader;

    @Value("${file.location2}") private String fileLocation2;

    public <RESULT> void saveImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, boolean isForGoogleLens) throws IOException {

        String fileName = "";

        if(isForGoogleLens){
            fileName = CommonStaticSearchUtil.generateRandomFileName(30);
        }else{
            fileName = UUID.randomUUID().toString();
        }

        String imageUrl = getOriginalFn.apply(result);
        imageUrl = imageUrl != null ? getOriginalFn.apply(result) : getThumbnailFn.apply(result);

        log.info("imageUrl: "+imageUrl);

        //2023-03-26 에러 나는 url 처리
        byte[] imageBytes;
        if (imageUrl != null) {
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                imageUrl = getThumbnailFn.apply(result);
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            }

            if (imageBytes == null) {
                imageUrl = getThumbnailFn.apply(result);
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            }

            if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("") && imageBytes != null) {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String folder = now.format(formatter);
                String restrictChars = "|\\\\?*<\":>/";
                String regExpr = "[" + restrictChars + "]+";
                String extension = "";
                String extension_ = "";
                if (resource.getFilename().indexOf(".") > 0) {
                    extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                    extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                }

                if(fileName.indexOf(".") > 0){
                    extension_ = fileName.substring(fileName.length()-3);
                }else{
                    extension_ = extension.substring(1);
                }

                File destDir = new File(fileLocation2 + folder + File.separator + tsiUno);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                Files.write(Paths.get(destDir + File.separator + fileName + extension), imageBytes);
                sre.setTsrImgExt(extension_);
                sre.setTsrImgName(fileName + extension);
                sre.setTsrImgPath((destDir + File.separator).replaceAll("\\\\", "/"));

                Image img = new ImageIcon(destDir + File.separator + fileName + extension).getImage();
                sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                sre.setTsrImgSize(String.valueOf(destDir.length() / 1024));
                img.flush();
            }
        } else {

        }
    }

    public <RESULT> void saveYoutubeImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, Map<String,String>> getThumnailFn) throws IOException {
        // Function<RESULT, String> getPositionFn,
        log.info("saveYoutubeImageFile 진입 ===============");
        log.info("getThumbnailFn: " + getThumnailFn);
        // Map<String, String> imageUrl = "11".equals(sre.getTsrSns()) ? getPositionFn.apply(result) : getThumnailFn.apply(result);
        String imageUrl = getThumnailFn.apply(result).get("static");
        // imageUrl = imageUrl.replace("%7Bstatic%3Dhttps","https");

        log.info("imageUrl: "+imageUrl);
        // imageUrl = imageUrl != null ? getPositionFn.apply(result) : getThumnailFn.apply(result);

        //2023-03-26 에러 나는 url 처리
        byte[] imageBytes = null;
        if (imageUrl != null) { // .toString()
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                //구글인 경우 IGNORE
//                if ("11".equals(sre.getTsrSns())) {
                imageUrl = getThumnailFn.apply(result).toString();
                imageUrl = imageUrl.replace("%7Bstatic%3Dhttps","https");
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//                }
//                else {
//                    log.error(e.getMessage(), e);
//                    System.out.println("catch else e"+e.getMessage());
//                    throw new RuntimeException(e);
//                }
            }

            // 에러가 안나도 imageBytes 가 null 일 때가 있음
            if (imageBytes == null) {
                imageUrl = getThumnailFn.apply(result).toString();
                log.debug("imageUrl: "+imageUrl);

                resource = resourceLoader.getResource(imageUrl);
                log.debug("resource: " + resource);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                log.debug("imageBytes: "+  restTemplate.getForObject(imageUrl, byte[].class) );
            }

            if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("") && imageBytes != null) {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String folder = now.format(formatter);
                String restrictChars = "|\\\\?*<\":>/";
                String regExpr = "[" + restrictChars + "]+";
                String uuid = UUID.randomUUID().toString();
                String extension = "";
                String extension_ = "";
                if (resource.getFilename().indexOf(".") > 0) {
                    extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                    extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                    extension_ = extension.substring(1);
                }

                File destdir = new File(fileLocation2 + folder + File.separator + tsiUno);
                if (!destdir.exists()) {
                    destdir.mkdirs();
                }

                Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                sre.setTsrImgExt(extension_);
                sre.setTsrImgName(uuid + extension);
                sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                img.flush();
            }
        } else {

        }

    }
}
