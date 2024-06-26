package com.nex.download;

import com.nex.search.entity.VideoInfoEntity;
import com.nex.search.repo.VideoInfoRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DownloadController {
    private final DownloadUtil downloadUtil;
    private final VideoInfoRepository videoInfoRepository;


    @GetMapping("/download/zip/{tsiUno}")
    public void downloadZip(HttpServletResponse response,
                            @PathVariable("tsiUno") Integer tsiUno) {
        DownloadDto downloadDto = new DownloadDto();
        downloadDto.setZipFileName(tsiUno+"_image");

        List<VideoInfoEntity> imageList =videoInfoRepository.findAllByTsiUno(tsiUno);
        List<String> downloadFileList = new ArrayList<>();

        if(!imageList.isEmpty()){
            for (VideoInfoEntity videoInfoEntity : imageList) {
                downloadFileList.add(videoInfoEntity.getTviImgRealPath() + videoInfoEntity.getTviImgName());
            }
            downloadDto.setSourceFiles(downloadFileList);
        }
        downloadUtil.downloadZip(downloadDto, response);
    }
}
