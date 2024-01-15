package com.nex.search.entity.dto;

public interface UserSearchHistoryDtoInterface {
    Integer getTsiUno();
    String getTsiUserFile();    // 대상자
    String getMonitoringCnt();  // 모니터링 개수
    String getDeleteReqCnt();   // 삭제요청 개수
    String getDeleteConfirmCnt();   // 삭제완료 개수
    String getAllDayMonitoringYn(); // 24시 모니터링 유무
    String getAllTimeCnt();         // 24시 모니터링 횟수
    String getResultMonitoringTime(); // 24시 모니터링 시간
    String getReDsmnCnt();      // 24시 모니터링 재유포 건수
    String getResultCnt();      // 총 검색 결과 수


}