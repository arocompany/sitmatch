package com.nex.download;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
public class DownloadUtil {
    public void downloadZip(DownloadDto downloadDto, HttpServletResponse response) {
        if(downloadDto.getZipFileName() == null || "".equals(downloadDto.getZipFileName()))
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");

        if(downloadDto.getSourceFiles() == null || downloadDto.getSourceFiles().size() == 0)
            throw new IllegalArgumentException("파일이 존재하지 않습니다.");

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + new String(downloadDto.getZipFileName().getBytes(StandardCharsets.UTF_8)) + ".zip;");
        response.setStatus(HttpServletResponse.SC_OK);

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())){
            for(String sourceFile : downloadDto.getSourceFiles()) {
                Path path = Path.of(sourceFile);
                try (FileInputStream fis = new FileInputStream(path.toFile())) {
                    ZipEntry zipEntry = new ZipEntry(path.getFileName().toString());
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = fis.read(buffer)) >=0) {
                        zos.write(buffer, 0, length);
                    }
                } catch (Exception e){
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    throw new IllegalArgumentException("파일 변환 작업중, ["+sourceFile + "] 파일을 찾을 수 없습니다.");
                } finally {
                    zos.flush();
                    zos.closeEntry();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                response.flushBuffer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
