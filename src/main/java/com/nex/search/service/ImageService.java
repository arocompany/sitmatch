package com.nex.search.service;

import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.SitProperties;
import com.nex.search.entity.SearchResultEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
    private final SitProperties sitProperties;

    public <RESULT> void saveImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, boolean isForGoogleLens) throws IOException {

        try {
            String fileName = "";

            if (isForGoogleLens) {
                fileName = CommonStaticSearchUtil.generateRandomFileName(30);
            } else {
                fileName = UUID.randomUUID().toString();
            }

            String imageUrl = getOriginalFn.apply(result);
            imageUrl = imageUrl != null ? getOriginalFn.apply(result) : getThumbnailFn.apply(result);

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
                        extension = extension.replaceAll(regExpr, "");
                    }

                    if (fileName.indexOf(".") > 0) {
                        extension_ = fileName.substring(fileName.length() - 3);
                    } else {
                        if (! StringUtils.hasText(extension)) extension = ".jpg";
                        extension_ = extension.substring(1);
                    }

                    File destDir = new File(sitProperties.getFileLocation2() + folder + File.separator + tsiUno);
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
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public <RESULT> void saveYoutubeImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, Map<String,String>> getThumnailFn) throws IOException {
        String imageUrl = getThumnailFn.apply(result).get("static");
        byte[] imageBytes = null;
        if (imageUrl != null) { // .toString()
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                imageUrl = getThumnailFn.apply(result).toString();
                imageUrl = imageUrl.replace("%7Bstatic%3Dhttps","https");
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            }

            // 에러가 안나도 imageBytes 가 null 일 때가 있음
            if (imageBytes == null) {
                imageUrl = getThumnailFn.apply(result).toString();
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
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

                File destdir = new File(sitProperties.getFileLocation2() + folder + File.separator + tsiUno);
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

    public <RESULT> void saveYandexReverseImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, Map<String, Object>> getOriginalFn, Function<RESULT, Map<String, Object>> getThumbnailFn, boolean isForGoogleLens) throws IOException {
        String fileName = "";

        if(isForGoogleLens){
            fileName = CommonStaticSearchUtil.generateRandomFileName(30);
        }else{
            fileName = UUID.randomUUID().toString();
        }

        String imageUrl = getOriginalFn.apply(result).get("link").toString();
        imageUrl = imageUrl != null ? getOriginalFn.apply(result).get("link").toString() : getThumbnailFn.apply(result).get("link").toString();

        byte[] imageBytes;
        if (imageUrl != null) {
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                imageUrl = getThumbnailFn.apply(result).get("link").toString();
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            }

            if (imageBytes == null) {
                imageUrl = getThumbnailFn.apply(result).get("link").toString();
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

                File destDir = new File(sitProperties.getFileLocation2() + folder + File.separator + tsiUno);
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
}
