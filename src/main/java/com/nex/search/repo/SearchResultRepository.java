package com.nex.search.repo;

import com.nex.search.entity.DefaultQueryDtoInterface;
import com.nex.search.entity.SearchResultEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchResultRepository extends JpaRepository<SearchResultEntity, Long> {
    Page<SearchResultEntity> findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(String keyword,Pageable pageable);

    Integer countByTrkStatCdNotNullAndTrkStatCd(String trkStatCd);

    @Query(value = "select distinct t.tsrSiteUrl from SearchResultEntity t where t.tsiUno = :tsiUno")
    List<String> findTsrSiteUrlDistinctByTsiUno(Integer tsiUno);

    SearchResultEntity findByTsrUno(Integer TsrUno);
    /*----------------------------------------------------------------------------------------------------------------*/

    String from_2 = " from  tb_search_info TSI " +
            " LEFT JOIN tb_search_result TSR ON TSR.TSR_UNO = TSI.TSR_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            " LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO " +
            " LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO ";

    String defaultQeury = "SELECT TSR.TSR_UNO as tsrUno, tsr.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsr.TSR_SITE_URL as tsrSiteUrl, tsr.TSR_IMG_PATH as tsrImgPath, tsr.TSR_IMG_NAME as tsrImgName, "+
            "tsr.TSR_IMG_EXT as tsrImgExt, tsr.TSR_DOWNLOAD_URL as tsrDownloadUrl, tsr.TSR_IMG_HEIGHT as tsrImgHeight, "+
            "tsr.TSR_IMG_WIDTH as tsrImgWidth, tsr.TSR_IMG_SIZE as tsrImgSize, tsr.TRK_STAT_CD as trkStatCd, " +
            "tsr.TRK_HIST_MEMO as trkHistMemo, tsr.DATA_STAT_CD as tsrDataStatCd, tsr.FST_DML_DT as tsrFstDmlDt, tsr.MST_DML_DT as mstDmlDt, "+
            "tsi.TSI_TYPE as tsiType, tsi.TSI_GOOGLE as tsiGoogle, tsi.TSI_TWITTER as tsiTwitter, " +
            "tsi.TSI_FACEBOOK as tsiFacebook, tsi.TSI_INSTAGRAM as tsiInstagram, tsi.TSI_KEYWORD as tsiKeyword, "+
            "tsi.TSI_IMG_PATH as tsiImgPath, tsi.TSI_IMG_NAME as tsiImgName, tsi.TSI_IMG_EXT as tsiImgExt, "+
            "tsi.TSI_IMG_HEIGHT as tsiImgHeight, tsi.TSI_IMG_WIDTH as tsiImgWidth, tsi.TSI_IMG_SIZE as tsiImgSize, "+
            "tsi.TSI_STAT as tsiStat, tsi.TSI_DNA_PATH as tsiDnaPath, tsi.TSI_DNA_TEXT as tsiDnaText, "+
            "tsi.DATA_STAT_CD as tsiDataStatCd, tsi.FST_DML_DT as tsiFstDmlDt, tsj.TSJ_STATUS as tsjStatus, TSR.MONITORING_CD as monitoringCd, "+
            "ROUND(tmr.TMR_V_SCORE, 2)*100 as tmrVScore, ROUND(tmr.TMR_T_SCORE, 2)*100 as tmrTScore, ROUND(tmr.TMR_A_SCORE, 2)*100 as tmrAScore, " +
            "tmr.TMR_STAT as tmrStat, tmr.TMR_MESSAGE as tmrMessage, tu.USER_ID as tuUserId, " +
            "(select count(*) as re_monitor_cnt from TB_SEARCH_INFO TTSI where TTSI.TSR_UNO = TSR.TSR_UNO) as re_monitor_cnt, "+
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
                "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
                    "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
                    "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
                    "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
//                    "(case when isnull(tmr.TMR_V_SCORE) || tmr.TMR_V_SCORE = 0 then 0 else 1 end + "+
                    "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
//                    "case when isnull(tmr.TMR_A_SCORE) || tmr.TMR_A_SCORE = 0 then 0 else 1 end + "+
                    "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity, "+
//                    "case when isnull(tmr.TMR_T_SCORE) || tmr.TMR_T_SCORE = 0 then 0 else 1 end)) * 100)) as tmrSimilarity, "+
            "pp.progressPercent as progressPercent";
    String from = " FROM TB_SEARCH_RESULT TSR INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";

    // WHERE
    String whereTsiUnoTsrTitleLikeTsrStatusIn = " WHERE TSI.TSI_UNO = :tsiUno AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') " +
            "AND (tsj.TSJ_STATUS = :tsjStatus1 OR tsj.TSJ_STATUS = :tsjStatus2 OR tsj.TSJ_STATUS = :tsjStatus3 OR tsj.TSJ_STATUS = :tsjStatus4)" +
            "AND (tsr.TSR_SNS = :snsStatus01 OR tsr.TSR_SNS = :snsStatus02 OR tsr.TSR_SNS = :snsStatus03 OR tsr.TSR_SNS = :snsStatus04)";
    String whereTsrUno = " WHERE TSR.TSR_UNO = :tsrUno";

    String whereNotice = " WHERE TSI.TSR_UNO is not null ";
    String whereMonitoringCd = " WHERE TSR.MONITORING_CD = :monitoringCd";
    String whereTrkStatCdNotNullAndTsrTitleContaining = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%')";
    String whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike = " WHERE TSR.DATA_STAT_CD = :tsrDataStatCd AND TSR.TRK_STAT_CD != :trkStatCd AND TSR.TRK_STAT_CD LIKE CONCAT('%',:trkStatCd2,'%') AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%')";

    // ORDER BY
    String orderByTmrSimilarityDesc = " ORDER BY tmrVScore desc, tmrAScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_1 = " ORDER BY tmrVScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_2 = " ORDER BY tmrAScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_3 = " ORDER BY tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_4 = " ORDER BY tmrVScore desc, tmrAScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_5 = " ORDER BY tmrVScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_6 = " ORDER BY tmrVScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";

    String orderByTmrSimilarityAsc = " ORDER BY tmrSimilarity asc, tsrUno desc";
    String orderByTsrUnoDesc = " ORDER BY tsrUno desc";

    String orderByTsrUnoDesc_trace = " ORDER BY mstDmlDt desc, tsrUno desc";

    // 검색결과 관련 ORDER BY
    String orderByResult01 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult02 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC";
    String orderByResult03 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_V_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult04 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult05 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_V_SCORE DESC";
    String orderByResult06 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_A_SCORE DESC";
    String orderByResult07 = " ORDER BY TMR.TSR_UNO DESC, TMR.TMR_T_SCORE DESC";
    String orderByResult08 = " ORDER BY tmrSimilarity DESC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult09 = " ORDER BY tmrSimilarity DESC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC";
    String orderByResult10 = " ORDER BY tmrSimilarity DESC, TMR.TMR_V_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult11 = " ORDER BY tmrSimilarity DESC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult12 = " ORDER BY tmrSimilarity DESC, TMR.TMR_V_SCORE DESC";
    String orderByResult13 = " ORDER BY tmrSimilarity DESC, TMR.TMR_A_SCORE DESC";
    String orderByResult14 = " ORDER BY tmrSimilarity DESC, TMR.TMR_T_SCORE DESC";
    String orderByResult15 = " ORDER BY tmrSimilarity ASC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult16 = " ORDER BY tmrSimilarity ASC, TMR.TMR_V_SCORE DESC, TMR.TMR_A_SCORE DESC";
    String orderByResult17 = " ORDER BY tmrSimilarity ASC, TMR.TMR_V_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult18 = " ORDER BY tmrSimilarity ASC, TMR.TMR_A_SCORE DESC, TMR_T_SCORE DESC";
    String orderByResult19 = " ORDER BY tmrSimilarity ASC, TMR.TMR_V_SCORE DESC";
    String orderByResult20 = " ORDER BY tmrSimilarity ASC, TMR.TMR_A_SCORE DESC";
    String orderByResult21 = " ORDER BY tmrSimilarity ASC, TMR.TMR_T_SCORE DESC";




    String countQuery = "SELECT COUNT(TSR.TSR_UNO)";
    String limit4 = " LIMIT 4";

    // 추적 이력
    @Query(value = defaultQeury+from+whereTrkStatCdNotNullAndTsrTitleContaining+orderByTsrUnoDesc_trace, nativeQuery = true, countQuery = countQuery+from+whereTrkStatCdNotNullAndTsrTitleContaining)
    Page<DefaultQueryDtoInterface> getTraceHistoryList(String keyword, Pageable pageable);
    // 검색 결과

    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_1, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_1(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_2, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_2(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_3, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_3(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_4, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_4(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_5, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_5(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_6, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_6(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);

    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityAsc, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityAsc(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                            String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);

    // 검색 결과 팝업
    @Query(value = defaultQeury+from+whereTsrUno, nativeQuery = true, countQuery = countQuery+from+whereTsrUno)
    DefaultQueryDtoInterface getResultInfo(Integer tsrUno);

    // index 추적대상 4개
    @Query(value = defaultQeury+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+orderByTsrUnoDesc_trace+limit4, nativeQuery = true, countQuery = countQuery+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+limit4)
    List<DefaultQueryDtoInterface> getTraceListByHome(String tsrDataStatCd, String trkStatCd, String trkStatCd2, String keyword);

    // 모니터링
    @Query(value = defaultQeury+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+orderByTsrUnoDesc_trace, nativeQuery = true, countQuery = countQuery+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike)
    Page<DefaultQueryDtoInterface> getTraceList(String tsrDataStatCd, String trkStatCd, String trkStatCd2, String keyword, Pageable pageable);

    // 모니터링 팝업
    @Query(value = defaultQeury+from+whereTsrUno, nativeQuery = true, countQuery = countQuery+from+whereTsrUno)
    DefaultQueryDtoInterface getTraceInfo(Integer tsrUno);

    @Query(value = "SELECT TRK_STAT_CD FROM tb_search_result WHERE TSR_UNO = :tsrUno", nativeQuery = true)
    String getTrkStatCd(Integer tsrUno);

    @Query(value = defaultQeury+from_2+" WHERE TSI.TSR_UNO IS NOT null", nativeQuery = true, countQuery = countQuery+from+whereNotice)
    Page<DefaultQueryDtoInterface> getNoticeList(Pageable pageable);

    @Query(value = defaultQeury+from_2+" WHERE TSI.TSR_UNO = :tsrUno ", nativeQuery = true, countQuery = countQuery+from+whereNotice)
    Page<DefaultQueryDtoInterface> getNoticeSelList(Pageable pageable, Integer tsrUno);

    //추적이력 삭제
    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_search_result SET TRK_STAT_CD = null WHERE TSR_UNO = :tsrUno", nativeQuery = true)
    int stat_co_del(@Param("tsrUno") Integer tsrUno);    // 자동추적 키워드 목록

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_search_result SET MST_DML_DT = now(),DATA_STAT_CD = '10', TRK_STAT_CD = '10' WHERE TSR_UNO = :tsrUno", nativeQuery = true)
    int addTrkStat(@Param("tsrUno") Integer tsrUno);    // 자동추적 키워드 목록

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_search_result SET TRK_STAT_CD = null WHERE TSR_UNO = :tsrUno", nativeQuery = true)
    int subTrkStat(@Param("tsrUno") Integer tsrUno);    // 자동추적 키워드 목록

    /**
     * 검색 결과 목록 조회
     *
     * @param  monitoringCd             (24시간 모니터링 코드 (10 : 안함, 20 : 모니터링))
     * @return List<SearchResultEntity> (검색 결과 엔티티 List)
     */
    List<SearchResultEntity> findByMonitoringCd(String monitoringCd);


}


