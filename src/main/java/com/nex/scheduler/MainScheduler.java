package com.nex.scheduler;

import com.nex.common.*;
import com.nex.search.entity.MatchResultEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.repo.MatchResultRepository;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainScheduler {
    private final int initialDelay = 1000;
    private final SearchService searchService;
    private final SitProperties sitProperties;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final MatchResultRepository matchResultRepository;

    @Scheduled(fixedDelay = 5 * 1000, initialDelay = initialDelay)
    public void schedule_5() {
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
        if(configData != null && configData.getIsMainScheduler() != null && configData.getIsMainScheduler()) {
            log.debug("schedule_5 --------------- start");
            updateInfoCnt();
            updateResultCnt();
            log.debug("schedule_5 --------------- end");
        }
    }

    @Scheduled(fixedDelay = 10 * 1000, initialDelay = initialDelay)
    public void schedule_10() {
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
        if(configData != null && configData.getIsMainScheduler() != null && configData.getIsMainScheduler()) {
            log.debug("schedule_10 --------------- start");
            searchVideo();
            log.debug("schedule_10 --------------- end");
        }
    }

//    @Scheduled(fixedDelay = 60 * 1000, initialDelay = initialDelay)
//    public void schedule_60() {
//        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
//        if(configData != null && configData.getIsMainScheduler() != null && configData.getIsMainScheduler()) {
//            log.debug("schedule_60 --------------- start");
//            loadSerpApiStatus();
//            log.debug("schedule_60 --------------- end");
//        }
//    }

    private void searchVideo(){
        try {
            List<SearchInfoEntity> list = searchService.getSearchInfoVideoNotReady();
            if(list != null && !list.isEmpty()){
                for(SearchInfoEntity item: list){
                    item.setTsiStat("12");
                    searchInfoRepository.save(item);
                }
            }

            if(list != null && !list.isEmpty()){
                for(SearchInfoEntity item: list){
                    SearchInfoDto dto = new SearchInfoDto();
                    dto.setTsiKeywordHiddenValue(item.getTsiKeyword());
                    searchService.search(item, dto, item.getTsiImgPath().replaceAll(sitProperties.getFileLocation1().replaceAll("\\\\", "/"), "").replaceAll("/", ""));
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSerpApiStatus(){
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("api_key", ConfigDataManager.getInstance().getDefaultConfig().getSerpApiKey());
            SerpApiStatusVOForApi result = CommonStaticHttpUtil.GetHttpSync("https://serpapi.com", "account", params, SerpApiStatusVOForApi.class);
            if(result != null) {
                ConfigDataManager.getInstance().setSerpApiStatus(result);
            }
        }catch(Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateInfoCnt(){
        String tsiStat = "13";
        String dataStatCd = "10";
        List<SearchInfoEntity> list = searchInfoRepository.findTop10ByTsiStatAndDataStatCdOrderByTsiUnoAsc(tsiStat, dataStatCd);

        if(list != null && !list.isEmpty()){
            for(SearchInfoEntity item: list){
                SearchResultEntity sre = searchResultRepository.findTop1ByTsiUnoAndTsrImgPathIsNotNullOrderByTsrUnoDesc(item.getTsiUno());
                if(sre != null) {
                    if(sre.getTsrState().equals(1)) {
                        item.setTsiStat("15");
                        searchInfoRepository.save(item);

                        item.setTsiStat("17");
                    }
                    {
                        Integer cntTsr = searchResultRepository.countResult(item.getTsiUno());
                        Integer cntSimilarity = matchResultRepository.countSimilarity(item.getTsiUno());
                        Integer cntChild = matchResultRepository.countChild(item.getTsiUno());
                        item.setTsiCntTsr(cntTsr);
                        item.setTsiCntSimilarity(cntSimilarity);
                        item.setTsiCntChild(cntChild);

                        searchInfoRepository.save(item);
                    }
                }else{
                    Integer cntTsr = searchResultRepository.countResult(item.getTsiUno());
                    item.setTsiCntTsr(cntTsr);
                    item.setTsiStat("17");
                    searchInfoRepository.save(item);
                }
            }
        }
    }

    private void updateResultCnt(){
        List<SearchResultEntity> list = searchResultRepository.selectResultWithJob();

        if(list != null && !list.isEmpty()){
            for(SearchResultEntity item: list){
                try {
                    MatchResultEntity data = matchResultRepository.selectByTsiUnoAndTsrUno(item.getTsiUno(), item.getTsrUno());

                    if (data != null) {
                        if (data.getTmrVScore() != null) {

                            Integer similarity = (int) ((Double) data.getTmrVScore() * 100);
                            item.setTsrSimilarity(similarity);
                        }

                        if (data.getTmrTotalScore() != null) {
                            Integer child = Integer.valueOf(String.valueOf(((Double) data.getTmrTotalScore()).intValue()));
                            item.setTsrTotalScore(child);
                        }
                    }

                    item.setTsrState(1);
                    searchResultRepository.save(item);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        List<SearchResultEntity> listForChild = searchResultRepository.selectResultWithJobForChild();

        if(listForChild != null && !listForChild.isEmpty()){
            for(SearchResultEntity item: listForChild){
                try {
                    MatchResultEntity data = matchResultRepository.selectByTsiUnoAndTsrUno(item.getTsiUno(), item.getTsrUno());

                    if (data != null) {
                        if (data.getTmrVScore() != null) {

                            Integer similarity = (int) ((Double) data.getTmrVScore() * 100);
                            item.setTsrSimilarity(similarity);
                        }

                        if (data.getTmrTotalScore() != null) {
                            Integer child = Integer.valueOf(String.valueOf(((Double) data.getTmrTotalScore()).intValue()));
                            item.setTsrTotalScore(child);
                        }
                    }

                    item.setTsrState(1);
                    searchResultRepository.save(item);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}