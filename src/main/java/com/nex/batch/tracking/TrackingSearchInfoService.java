package com.nex.batch.tracking;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingSearchInfoService {

    private final SearchService searchService;

    @Value("${file.location1}")
    private String fileLocation1;


    /**
     * 검색 이력 엔티티 추출
     *
     * @param  searchInfoEntityByTsiUno (검색 이력 엔티티)
     * @param  searchResultEntity       (검색 결과 엔티티)
     * @return SearchInfoEntity         (검색 정보 엔티티)
     */
    public SearchInfoEntity getSearchInfoEntity(SearchInfoEntity searchInfoEntityByTsiUno, SearchResultEntity searchResultEntity) {
        log.info("getSearchInfoEntity 진입");
        SearchInfoEntity searchInfoEntity = new SearchInfoEntity();

        searchInfoEntity.setUserUno(1);
        searchInfoEntity.setTsiStat("11");

        //기존 searchInfo 값 세팅 (기본 정보)
        //배치는 11, 17 제외 13
        //11:키워드 이거나 17:이미지 이면 그대로
        if ("11".equals(searchInfoEntityByTsiUno.getTsiType()) || "17".equals(searchInfoEntityByTsiUno.getTsiType())) {
            searchInfoEntity.setTsiType(searchInfoEntityByTsiUno.getTsiType());         //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
        }
        //나머지는 13:키워드+이미지
        else {
            searchInfoEntity.setTsiType("13");                                          //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
        }
        searchInfoEntity.setTsiGoogle(searchInfoEntityByTsiUno.getTsiGoogle());         //구글 검색 여부
        searchInfoEntity.setTsiTwitter(searchInfoEntityByTsiUno.getTsiTwitter());       //트위터 검색 여부
        searchInfoEntity.setTsiFacebook(searchInfoEntityByTsiUno.getTsiFacebook());     //페이스북 검색 여부
        searchInfoEntity.setTsiInstagram(searchInfoEntityByTsiUno.getTsiInstagram());   //인스타그램 검색 여부
        searchInfoEntity.setTsiKeyword(searchInfoEntityByTsiUno.getTsiKeyword());       //검색어
        searchInfoEntity.setTsiMonitoringCnt((searchInfoEntityByTsiUno.getTsiMonitoringCnt())+1);

        log.info("searchInfoEntity.getTsiUno: "+searchInfoEntity.getTsiUno());

        //기존 searchResult 이미지 파일 복사
        String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filePath = fileLocation1 + folder;
        String uuid = UUID.randomUUID().toString();
        String extension = searchResultEntity.getTsrImgExt();
        File destdir = new File(filePath);
        if (!destdir.exists()) {
            destdir.mkdirs();
        }

        File srcFile  = new File(searchResultEntity.getTsrImgPath() + searchResultEntity.getTsrImgName());
        File destFile = new File(destdir + File.separator + uuid + "." + extension);

        try {
            FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        //기존 searchResult 값 세팅 (이미지 정보)
        searchInfoEntity.setTsiImgPath((destdir + File.separator).replaceAll("\\\\", "/"));
        searchInfoEntity.setTsiImgName(uuid + "." + extension);
        searchInfoEntity.setTsiImgExt(searchResultEntity.getTsrImgExt());
        searchInfoEntity.setTsiImgHeight(searchResultEntity.getTsrImgHeight());
        searchInfoEntity.setTsiImgWidth(searchResultEntity.getTsrImgWidth());
        searchInfoEntity.setTsiImgSize(searchResultEntity.getTsrImgSize());
        searchInfoEntity.setTsrUno(searchResultEntity.getTsrUno());
        searchInfoEntity.setTsiAlltimeMonitoring(Timestamp.valueOf(LocalDateTime.now())+"  ");

        //검색 정보 엔티티 기본값 세팅
        searchService.setSearchInfoDefault(searchInfoEntity);

        return searchInfoEntity;
    }

/*
    public BatchAllTimeMonitoringEntity getBatchAllTimeMonitoringEntity(SearchInfoEntity searchInfoEntityByTsiUno, SearchResultEntity searchResultEntity) {
        log.info("getSearchInfoEntity 진입");

        BatchAllTimeMonitoringEntity batchAllTimeMonitoringEntity = new BatchAllTimeMonitoringEntity();
        batchAllTimeMonitoringEntity.setTsiUno(searchResultEntity.getTsiUno());
        batchAllTimeMonitoringEntity.setTsrUno(searchResultEntity.getTsrUno());
        batchAllTimeMonitoringEntity.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));

        return batchAllTimeMonitoringEntity;
    }
*/

}