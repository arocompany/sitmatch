package com.nex.newKeyword.service;

import com.nex.common.Consts;
import com.nex.common.SitProperties;
import com.nex.search.entity.NewKeywordEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.repo.NewKeywordRepository;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.service.ImageService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewKeywordService {
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final ImageService imageService;
    private final NewKeywordRepository newKeywordRepository;
    private final SitProperties sitProperties;
    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public NewKeywordEntity addNewKeyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,String addNewKeyword) {
        NewKeywordEntity nke = new NewKeywordEntity();
        nke.setUserId(sessionInfoDto.getUserId());
        nke.setKeyword(addNewKeyword);
        nke.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        nke.setKeywordStus("0");

        return newKeywordRepository.save(nke);
    }

    public SearchInfoEntity saveNewKeywordSearchInfo(SearchInfoEntity sie) {
        setNewKeywordInfoDefault(sie);
        return searchInfoRepository.save(sie);
    }

    public static void setNewKeywordInfoDefault(SearchInfoEntity sie) {
        sie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setDataStatCd("10");
        sie.setSearchValue("1");
    }

    // admin 일때
    public Map<String, Object> getNewKeywordInfoList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<SearchInfoEntity> newKeywordInfoList = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10","1", keyword, pageRequest);

        outMap.put("newKeywordInfoList", newKeywordInfoList);
        outMap.put("totalPages", newKeywordInfoList.getTotalPages());
        outMap.put("number", newKeywordInfoList.getNumber());
        outMap.put("totalElements", newKeywordInfoList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    // admin 아닐때
    public Map<String, Object> getNewKeywordInfoList(Integer page, String keyword, int userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<SearchInfoEntity> newKeywordInfoList = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10", "1", keyword, userUno, pageRequest);

        outMap.put("newKeywordInfoList", newKeywordInfoList);
        outMap.put("totalPages", newKeywordInfoList.getTotalPages());
        outMap.put("number", newKeywordInfoList.getNumber());
        outMap.put("totalElements", newKeywordInfoList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

}
