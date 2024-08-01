package com.nex.scheduler;

import com.nex.common.ConfigData;
import com.nex.common.ConfigDataManager;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MainScheduler {
    private final int initialDelay = 1000;
    private final SearchService searchService;
    private final SearchInfoRepository searchInfoRepository;

    @Scheduled(fixedDelay = 10 * 1000, initialDelay = initialDelay)
    public void schedule_10() {
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
        if(configData != null && configData.getIsMainScheduler() != null && configData.getIsMainScheduler()) {
            log.debug("schedule_10 --------------- start");
            searchVideo();
            log.debug("schedule_10 --------------- end");
        }
    }

    private void searchVideo(){
        try {
            List<SearchInfoEntity> list = searchService.getSearchInfoVideoNotReady();
            if(list != null && !list.isEmpty()){
                for(SearchInfoEntity item: list){
                    item.setTsiStat("12");
                    searchInfoRepository.save(item);
                }
            }


            List<SearchInfoEntity> list2 = searchService.getSearchInfoVideoReady();
            if(list != null && !list.isEmpty()){
                for(SearchInfoEntity item: list){
                    SearchInfoDto dto = new SearchInfoDto();
                    dto.setTsiKeywordHiddenValue(item.getTsiKeyword());
                    searchService.search(item, dto, item.getTsiImgPath().substring(7));
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}