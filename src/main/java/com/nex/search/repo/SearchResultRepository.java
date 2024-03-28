package com.nex.search.repo;

import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchResultRepository extends JpaRepository<SearchResultEntity, Long> {
    List<SearchResultEntity> findByTsrIsBatch(int activeBatch); // 활성화된 배치 쿼리
    // Page<SearchResultEntity> findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(String keyword,Pageable pageable);
    Integer countByTrkStatCdNotNullAndTrkStatCd(String trkStatCd);

//    Integer countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(String trkStatCd, Integer tsiSearchType);

    @Query(value = "select count(*) from SearchResultEntity t LEFT OUTER JOIN SearchInfoEntity i ON t.tsiUno = i.tsiUno where t.trkStatCd is not null AND (t.trkStatCd = :trkStatCd or :trkStatCd is null) AND (i.tsiSearchType = :tsiSearchType  OR :tsiSearchType = 0 )")
    Integer countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(String trkStatCd, Integer tsiSearchType);
    // Integer countByMonitoringCd(String monitoringCd);

    Integer countByTrkStatCdNotNull();

    @Query(value = allTimeMonitoringCnt, nativeQuery = true, countQuery=allTimeMonitoringCnt)
    String allTimeMonitoringCnt(Integer tsiSearchType);


    @Query(value = "select distinct t.tsrSiteUrl from SearchResultEntity t where t.tsiUno = :tsiUno")
    List<String> findTsrSiteUrlDistinctByTsiUno(Integer tsiUno);

    SearchResultEntity findByTsrUno(Integer TsrUno);

    Integer countByTsrSiteUrl(String url);

    List<SearchResultEntity> findByTsrUnoIn(List<Integer> tsrUnoValues);
    /*----------------------------------------------------------------------------------------------------------------*/

    String allTimeMonitoringCnt =   " SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR " +
            " LEFT OUTER JOIN " +
            " TB_SEARCH_INFO TSI3 " +
            " ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            " LEFT OUTER JOIN " +
            " TB_SEARCH_JOB TSJ " +
            " ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR " +
            " ON TSR.TSR_UNO = TMR.TSR_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_INFO TSI " +
            " ON TSI.TSI_UNO = TSR.TSI_UNO " +
            " LEFT OUTER JOIN " +
            " (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS " +
            " WHEN '11' THEN 1 " +
            " WHEN '10' THEN 1 " +
            " ELSE 0 " +
            " END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            " FROM TB_SEARCH_JOB TSJ " +
            " GROUP BY TSJ.TSI_UNO) PP " +
            " ON TSR.TSI_UNO = PP.TSI_UNO " +
            " LEFT OUTER JOIN TB_USER TU " +
            " ON TSI.USER_UNO = TU.USER_UNO " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL " +
            " AND TRK_STAT_CD IS NOT NULL " +
            " AND (TSR.TSR_TITLE LIKE CONCAT('%','','%') OR TSR.TSR_TITLE IS NULL)" +
            " AND TSR.MONITORING_CD = '20' " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
            " ORDER BY tsr.MST_DML_DT DESC ";

/*
    String keywordResultCntList = " SELECT COUNT(*) AS resultCnt " +
            " FROM tb_search_result tsr " +
            " LEFT OUTER JOIN tb_search_info tsi " +
            " ON tsr.tsi_uno = tsi.tsi_uno " +
            " WHERE tsr.fst_dml_dt BETWEEN :fromDate AND :toDate " +
            " GROUP BY DATE_FORMAT(tsr.fst_dml_dt,'%Y%m%d') " +
            " ORDER BY tsr.fst_dml_dt asc";
*/

    String from_5 = " from  tb_search_info TSI " +
            " INNER JOIN tb_search_result TSR ON TSR.TSI_UNO = TSI.TSI_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            " LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO " +
            " WHERE TSI.TSI_UNO = :tsiUno AND TSI.TSI_KEYWORD LIKE CONCAT('%',:tsiKeyword,'%')  " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";

    String from_6 = " from  tb_search_info TSI " +
            " INNER JOIN tb_search_result TSR ON TSR.TSI_UNO = TSI.TSI_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            " LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO " +
            " WHERE TSI.TSI_UNO = :tsiUno "+
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";
    String from_2 = " from  tb_search_info TSI " +
            " INNER JOIN tb_search_result TSR ON TSR.TSI_UNO = TSI.TSI_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            " LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO ";

/*
    String from_3 = " FROM TB_SEARCH_RESULT TSR " +
            "INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR.TSI_UNO = TSI_2.TSI_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  ";
*/

/*
    String from_4 = " FROM TB_SEARCH_RESULT TSR " +
            "INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR.TSI_UNO = TSI_2.TSI_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  ";
*/


    String defaultQeury_2 = "SELECT TSR.TSR_UNO as tsrUno, TSI.TSI_SEARCH_TYPE, TSI.TSI_SEARCH_TYPE AS tsiSearchType, TSI.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
//                    "(case when isnull(tmr.TMR_V_SCORE) || tmr.TMR_V_SCORE = 0 then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
//                    "case when isnull(tmr.TMR_A_SCORE) || tmr.TMR_A_SCORE = 0 then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", tsr.TSR_NATION_CODE as tsrNationCode "+
            ", tsr.TSR_ENGINE as tsrEngine, " +
            " ROUND(tmr.TMR_AGE_SCORE, 2)* 100 AS tmrAgeScore, " +
            " ROUND(tmr.TMR_OBJECT_SCORE, 2)* 100 AS tmrObjectScore, " +
            " ROUND(tmr.TMR_OCW_SCORE, 2)* 100 AS tmrOcwScore " ;

    /*String defaultQeury_4 = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end nn+ "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity";*/

    // String defaultQeury_3 = defaultQeury_2 + ", (SELECT count(*) from  tb_search_info TSI LEFT JOIN tb_search_result TSR ON TSR.TSI_UNO = TSI.TSI_UNO WHERE TSI.TSI_UNO = :tsiuno  ) as re_monitor_cnt ";
    String defaultQeury_3 = defaultQeury_2 + ", (SELECT count(*) from  tb_search_info TSI LEFT JOIN tb_search_result TSR ON TSR.TSI_UNO = TSI.TSI_UNO WHERE TSI.TSI_UNO = :tsiUno  ) as re_monitor_cnt ";
    String defaultQeury = defaultQeury_2 + ", pp.progressPercent as progressPercent ";

    // String defaultQeury2 = defaultQeury_4 + ", pp.progressPercent as progressPercent ";


    String defaultQeury_5 = "SELECT TSR.TSR_UNO as tsrUno, TSI.TSI_SEARCH_TYPE AS tsiSearchType, TSI.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsr.TSR_SITE_URL as tsrSiteUrl, tsr.TSR_IMG_PATH as tsrImgPath, tsr.TSR_IMG_NAME as tsrImgName, tsr.TRK_STAT_CD as trkStatCd,"+
            "tsi.TSI_KEYWORD as tsiKeyword, tsj.TSJ_STATUS as tsjStatus, tu.USER_ID as tuUserId, tsi.TSI_TYPE as tsiType, tsr.TSR_IMG_EXT as tsrImgExt," +
            "ROUND(tmr.TMR_V_SCORE, 2)*100 as tmrVScore, ROUND(tmr.TMR_T_SCORE, 2)*100 as tmrTScore, ROUND(tmr.TMR_A_SCORE, 2)*100 as tmrAScore, " +
            "CONVERT(if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
//                    "(case when isnull(tmr.TMR_V_SCORE) || tmr.TMR_V_SCORE = 0 then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
//                    "case when isnull(tmr.TMR_A_SCORE) || tmr.TMR_A_SCORE = 0 then 0 else 1 end + "+
            " case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)),SIGNED) as tmrSimilarity, " +
            " (SELECT " +
            " CONVERT(MAX(if(TMR_V_SCORE + TMR_A_SCORE + TMR_T_SCORE = 0, '0', ceiling(((case " +
            " when isnull(TMR_V_SCORE) then 0 " +
            " else TMR_V_SCORE " +
            " end + case " +
            " when isnull(TMR_A_SCORE) then 0 " +
            " else TMR_A_SCORE " +
            " end + case " +
            " when isnull(TMR_T_SCORE) then 0 " +
            " else TMR_T_SCORE " +
            " end) / (case " +
            " when isnull(TMR_V_SCORE) then 0 " +
            " else 1 " +
            " end + case " +
            " when isnull(TMR_A_SCORE) then 0 " +
            " else 1 " +
            " end + case " +
            " when isnull(TMR_T_SCORE) then 0 " +
            " else 1 end)) * 100))),SIGNED)" +
            " FROM TB_MATCH_RESULT " +
            " WHERE tsr.tsi_uno = tsi_uno " +
            ") AS maxSimilarity" +
            ", tsr.TSR_NATION_CODE as tsrNationCode "+
            ", tsr.TSR_ENGINE as tsrEngine "+
            ",ROUND(tmr.TMR_AGE_SCORE, 2)* 100 AS tmrAgeScore" +
            ",ROUND(tmr.TMR_OBJECT_SCORE, 2)* 100 AS tmrObjectScore" +
            ",ROUND(tmr.TMR_OCW_SCORE, 2)* 100 AS tmrOcwScore ";


    // String defaultQeury_6 = "SELECT tsr.TSR_SITE_URL as tsrSiteUrl";


/*
    String defaultQeury_0 = defaultQeury2 +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR.TSI_UNO = TSI_2.TSI_UNO " +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO" +
            " WHERE " +
            " TSR.TSI_UNO = TSI.TSI_UNO " +
            " AND " +
            " if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(tmr.TMR_V_SCORE) then 0 " +
            "            else tmr.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(tmr.TMR_A_SCORE) then 0 " +
            "            else tmr.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(tmr.TMR_T_SCORE) then 0 " +
            "            else tmr.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(tmr.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(tmr.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(tmr.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt ";
*/

    String defaultTraceHistoryQuery = " SELECT TSR.TSR_UNO as tsrUno, TSI.TSI_SEARCH_TYPE as tsiSearchType, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
                                      " tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
                                      " tsr.TSR_SITE_URL as tsrSiteUrl, tsr.TSR_IMG_PATH as tsrImgPath, tsr.TSR_IMG_NAME as tsrImgName, "+
                                      " tsr.TSR_IMG_EXT as tsrImgExt, tsr.TSR_DOWNLOAD_URL as tsrDownloadUrl, tsr.TSR_IMG_HEIGHT as tsrImgHeight, "+
                                      " tsr.TSR_IMG_WIDTH as tsrImgWidth, tsr.TSR_IMG_SIZE as tsrImgSize, tsr.TRK_STAT_CD as trkStatCd, " +
                                      " tsr.TRK_HIST_MEMO as trkHistMemo, tsr.DATA_STAT_CD as tsrDataStatCd, tsr.FST_DML_DT as tsrFstDmlDt, tsr.MST_DML_DT as mstDmlDt, "+
                                      " tsi.TSI_TYPE as tsiType, tsi.TSI_GOOGLE as tsiGoogle, tsi.TSI_TWITTER as tsiTwitter, " +
                                      " tsi.TSI_FACEBOOK as tsiFacebook, tsi.TSI_INSTAGRAM as tsiInstagram, tsi.TSI_KEYWORD as tsiKeyword, "+
                                      " tsi.TSI_IMG_PATH as tsiImgPath, tsi.TSI_IMG_NAME as tsiImgName, tsi.TSI_IMG_EXT as tsiImgExt, "+
                                      " tsi.TSI_IMG_HEIGHT as tsiImgHeight, tsi.TSI_IMG_WIDTH as tsiImgWidth, tsi.TSI_IMG_SIZE as tsiImgSize, "+
                                      " tsi.TSI_STAT as tsiStat, tsi.TSI_DNA_PATH as tsiDnaPath, tsi.TSI_DNA_TEXT as tsiDnaText, "+
                                      " tsi.DATA_STAT_CD as tsiDataStatCd, tsi.FST_DML_DT as tsiFstDmlDt, tsj.TSJ_STATUS as tsjStatus, TSR.MONITORING_CD as monitoringCd, "+
                                      " ROUND(tmr.TMR_V_SCORE, 2)*100 as tmrVScore, ROUND(tmr.TMR_T_SCORE, 2)*100 as tmrTScore, ROUND(tmr.TMR_A_SCORE, 2)*100 as tmrAScore, " +
                                      " tmr.TMR_STAT as tmrStat, tmr.TMR_MESSAGE as tmrMessage, tu.USER_ID as tuUserId, " +
                                      " if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
                                      " ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
                                      " case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
                                      " case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
                                      " (case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
                                      " case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
                                      " case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity " +
                                      " , (SELECT CEILING(SUM(CASE TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ_STATUS) * 100) AS PROGRESSPERCENT  FROM tb_search_job WHERE tsi_uno = tsr.tsi_uno GROUP BY tsi_uno) progressPercent " +
                                      " , (SELECT COUNT(*) " +
                                      " FROM TB_SEARCH_RESULT TSR2 " +
                                      " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO " +
                                      " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO " +
                                      " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO " +
                                      " WHERE TSR2.TSI_UNO = TSI3.TSI_UNO " +
                                      " AND if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
                                      "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
                                      "            else TMR2.TMR_V_SCORE " +
                                      "        end + case " +
                                      "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
                                      "            else TMR2.TMR_A_SCORE " +
                                      "        end + case " +
                                      "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
                                      "            else TMR2.TMR_T_SCORE " +
                                      "        end) / (case " +
                                      "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
                                      "            else 1 " +
                                      "        end + case " +
                                      "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
                                      "           else 1 " +
                                      "        end + case " +
                                      "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
                                      "            else 1 " +
                                      "        end)) * 100)) > 0" +
                                      "         ) as re_monitor_cnt, " +
                                      " (SELECT MAX(clkDmlDt) " +
                                      " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
                                      " FROM tb_search_result_monitoring_history TMH " +
                                      " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
                                      " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
                                      " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
                                      " TSI.TSI_USER_FILE AS tsiUserFile "+
                                      " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
                                      ", TSR.tsr_nation_code as tsrNationCode"  ;
    String fromTraceHistoryQuery =  " FROM TB_SEARCH_RESULT TSR " +
                                    " LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
                                    " LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
                                    " LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
                                    " LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
                                    " LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " ;

    // 검색어(타이틀) 검색
    String whereTraceHistoryList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') OR TSR.TSR_TITLE IS NULL) AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceMonitoringList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '10' AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') OR TSR.TSR_TITLE IS NULL) AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceMonitoringDeleteRequestList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '20' AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceMonitoringDeleteComptList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '30' AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceAllTimeMonitoringChkList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD IS NOT NULL AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND TSR.MONITORING_CD = '20' AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    
    // 대상자 검색
    String whereTraceHistoryUserFileList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%')  AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 )" ;
    String whereTraceMonitoringUserFileList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceMonitoringDeleteRequestUserFileList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceMonitoringDeleteComptUserFileList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;
    String whereTraceAllTimeMonitoringUserFileChkList = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " ;

    String orderByTraceHistory = " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String traceUserFileList = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, "+
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSI3.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') " +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String defaultQeury_14 = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile "+
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch "+
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '10' AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') OR TSR.TSR_TITLE IS NULL)  AND TSR.TSI_UNO = :tsiUno " +
            "  AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 )" +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";


    String traceHistoryMonitoringTsiUnoUserFileList = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '10' AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";


    String defaultQeury_15 = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +
            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '20' AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String traceHistoryDeleteReqTsiUnoUserFileList =  "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +

            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '20' AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) "+
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String defaultQeury_16 = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +

            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '30' AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) "+
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String traceHistoryDeleteComptTsiUnoUserFileList = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +

            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD = '30' AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) "+
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String defaultQeury_17 = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +

            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD IS NOT NULL AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND TSR.MONITORING_CD = '20' AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String allTimeMonitoringTsiUnoUserFileList = "SELECT TSR.TSR_UNO as tsrUno, TSR.TSI_UNO as tsiUno, tsr.TSR_TITLE as tsrTitle, tsr.TSR_SNS as tsrSns, "+
            "tsi3.tsi_uno as tsi3tsiuno, tsi3.tsi_keyword as tsi3keyword, "+
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
            "if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', "+
            "ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / "+
            "(case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + "+
            "case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) as tmrSimilarity" +
            ", pp.progressPercent as progressPercent " +
            ", (SELECT COUNT(*) " +
            " FROM TB_SEARCH_RESULT TSR2 " +
            " INNER JOIN TB_SEARCH_INFO TSI_2 ON TSR2.TSI_UNO = TSI_2.TSI_UNO" +
            " LEFT OUTER JOIN TB_SEARCH_JOB TSJ2 ON TSR2.TSR_UNO = TSJ2.TSR_UNO" +
            " LEFT OUTER JOIN TB_MATCH_RESULT TMR2 ON TSR2.TSR_UNO = TMR2.TSR_UNO" +
            " WHERE " +
            " TSR2.TSI_UNO = TSI3.TSI_UNO " +
            " AND " +
            " if(TMR2.TMR_V_SCORE + TMR2.TMR_A_SCORE + TMR2.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else TMR2.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "            else TMR2.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else TMR2.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR2.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_A_SCORE) then 0 " +
            "           else 1 " +
            "        end + case " +
            "            when isnull(TMR2.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > 0" +
            "         ) as re_monitor_cnt, " +
            " (SELECT MAX(clkDmlDt) " +
            " FROM (SELECT TMH.TSRMH_CREATE_DATE AS clkDmlDt " +
            " FROM tb_search_result_monitoring_history TMH " +
            " LEFT OUTER JOIN tb_search_result TSR9 ON TMH.TSR_UNO = TSR9.TSR_UNO " +
            " WHERE TMH.TSR_UNO = TSR.TSR_UNO " +
            " ORDER BY clkDmlDt DESC) AS clkDmlDtResult) AS lastAlltimeHist, " +
            " TSI3.TSI_USER_FILE AS tsiUserFile " +
            " , TSR.tsr_is_batch tsrIsBatch, TSR.tsr_cycle_batch tsrCycleBatch " +
            " , TSR.tsr_nation_code tsrNationCode " +
            " FROM TB_SEARCH_RESULT TSR " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI3 ON TSI3.TSR_UNO = TSR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO " +
            "LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO " +
            "LEFT OUTER JOIN TB_SEARCH_INFO TSI ON TSI.TSI_UNO = TSR.TSI_UNO " +
            "LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, " +
            "CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT " +

            "FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO  " +
            " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TRK_STAT_CD IS NOT NULL AND TSI.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') AND TSR.MONITORING_CD = '20' AND TSR.TSI_UNO = :tsiUno " +
            " AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
            " ORDER BY tsr.MST_DML_DT desc, TSR.TSR_UNO desc";

    String from = " FROM TB_SEARCH_RESULT TSR INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";
    String from2 = " FROM TB_SEARCH_RESULT TSR INNER JOIN (SELECT MIN(tsr_uno) tsr_uno from tb_search_result WHERE (:tsiUno is null or tsi_uno = :tsiUno) GROUP BY tsr_site_url) tsr2 ON tsr.tsr_uno = tsr2.tsr_uno INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";
    String from3 = " FROM TB_SEARCH_RESULT TSR INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";

    String fromForMonitoring = " FROM TB_SEARCH_RESULT TSR INNER JOIN (SELECT MIN(tsr_uno) tsr_uno from tb_search_result WHERE DATA_STAT_CD = '10' AND TRK_STAT_CD != '30' GROUP BY tsr_site_url) tsr2 ON tsr.tsr_uno = tsr2.tsr_uno INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN (SELECT TSJ.TSI_UNO AS TSI_UNO, CEILING(SUM(CASE TSJ.TSJ_STATUS WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END) / COUNT(TSJ.TSJ_STATUS) * 100) AS PROGRESSPERCENT FROM TB_SEARCH_JOB TSJ GROUP BY TSJ.TSI_UNO) PP ON TSR.TSI_UNO = PP.TSI_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";
    String from2ForMonitoring = " FROM TB_SEARCH_RESULT TSR INNER JOIN (SELECT MIN(tsr_uno) tsr_uno from tb_Asearch_result WHERE DATA_STAT_CD = '10' AND TRK_STAT_CD != '30' GROUP BY tsr_site_url) tsr2 ON tsr.tsr_uno = tsr2.tsr_uno INNER JOIN TB_SEARCH_INFO TSI ON TSR.TSI_UNO = TSI.TSI_UNO LEFT OUTER JOIN TB_SEARCH_JOB TSJ ON TSR.TSR_UNO = TSJ.TSR_UNO LEFT OUTER JOIN TB_MATCH_RESULT TMR ON TSR.TSR_UNO = TMR.TSR_UNO LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO";

    // WHERE
    String whereTsiUnoTsrTitleLikeTsrStatusIn = " WHERE TSI.TSI_UNO = :tsiUno AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') or (:keyword = '' and TSR.TSR_TITLE is null)) " +
            "AND (tsj.TSJ_STATUS = :tsjStatus1 OR tsj.TSJ_STATUS = :tsjStatus2 OR tsj.TSJ_STATUS = :tsjStatus3 OR tsj.TSJ_STATUS = :tsjStatus4)" +
            " AND ((tsr.TSR_IMG_NAME IS NOT NULL AND :isImage = '1') OR :isImage != '1') "+
            "AND (tsr.TSR_SNS = :snsStatus01 OR tsr.TSR_SNS = :snsStatus02 OR tsr.TSR_SNS = :snsStatus03 OR tsr.TSR_SNS = :snsStatus04)" +
            " AND (tsr.TSR_NATION_CODE IN (:nationCode)) ";

    String whereTsiUnoTsrTitleLikeTsrStatusIn2 =" WHERE TSI.TSI_UNO = :tsiUno AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') or (:keyword = '' and TSR.TSR_TITLE is null)) " +
            " AND (tsj.TSJ_STATUS = :tsjStatus1 OR tsj.TSJ_STATUS = :tsjStatus2 OR tsj.TSJ_STATUS = :tsjStatus3 OR tsj.TSJ_STATUS = :tsjStatus4)" +
            " AND ((tsr.TSR_IMG_NAME IS NOT NULL AND :isImage = 'on') OR :isImage != 'on') "+
            " AND (tsr.TSR_SNS = :snsStatus01 OR tsr.TSR_SNS = :snsStatus02 OR tsr.TSR_SNS = :snsStatus03 OR tsr.TSR_SNS = :snsStatus04)" +
            " AND (tsr.TSR_NATION_CODE IN (:nationCode)) ";


    // String whereTsiUnoTsrTitleLikeTsrStatusIn3 =" WHERE TSI.TSI_UNO = :tsiUno AND TSR.TSR_IMG_PATH IS NULL ";

    String whereTsrUno = " WHERE TSR.TSR_UNO = :tsrUno";

    String whereNotice = " WHERE TSI.TSR_UNO is not null ";
    // String whereMonitoringCd = " WHERE TSR.MONITORING_CD = :monitoringCd";
    String whereTrkStatCdNotNullAndTsrTitleContaining = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";
    String setTrkStatCd20 =  " AND TSR.TRK_STAT_CD = '20' ";
    String setTrkStatCd30 =  " AND TSR.TRK_STAT_CD = '30' ";
    String setMonitoringCd20 = " AND TSR.MONITORING_CD = '20' ";
    String whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno = " WHERE TSR.TRK_STAT_CD IS NOT NULL AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND TSR.TSI_UNO = :tsiUno AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";
    String whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike = " WHERE TSR.DATA_STAT_CD = :tsrDataStatCd AND TSR.TRK_STAT_CD != :trkStatCd AND TSR.TRK_STAT_CD LIKE CONCAT('%',:trkStatCd2,'%') AND TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";
    // String whereSimilarity =" AND if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', ceiling(((case when isnull(tmr.TMR_V_SCORE) then 0 else tmr.TMR_V_SCORE end + case when isnull(tmr.TMR_A_SCORE) then 0 else tmr.TMR_A_SCORE end + case when isnull(tmr.TMR_T_SCORE) then 0 else tmr.TMR_T_SCORE end) / (case when isnull(tmr.TMR_V_SCORE) then 0 else 1 end + case when isnull(tmr.TMR_A_SCORE) then 0 else 1 end + case when isnull(tmr.TMR_T_SCORE) then 0 else 1 end)) * 100)) >= :percent";
    String whereSimilarity_2 = " AND " +
            "  if(TMR.TMR_V_SCORE + TMR.TMR_A_SCORE + TMR.TMR_T_SCORE = 0, '0', ceiling(((case " +
            "            when isnull(TMR.TMR_V_SCORE) then 0 " +
            "            else TMR.TMR_V_SCORE " +
            "        end + case " +
            "            when isnull(TMR.TMR_A_SCORE) then 0 " +
            "            else TMR.TMR_A_SCORE " +
            "        end + case " +
            "            when isnull(TMR.TMR_T_SCORE) then 0 " +
            "            else TMR.TMR_T_SCORE " +
            "        end) / (case " +
            "            when isnull(TMR.TMR_V_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR.TMR_A_SCORE) then 0 " +
            "            else 1 " +
            "        end + case " +
            "            when isnull(TMR.TMR_T_SCORE) then 0 " +
            "            else 1 " +
            "        end)) * 100)) > :percent" +
            "       ORDER BY tsr.MST_DML_DT, tsr.tsr_uno desc";

    String whereSimilarity_3 = " ORDER BY tsr.MST_DML_DT, tsr.tsr_uno desc";


    // ORDER BY
    // String orderByTmrSimilarityDesc = "  GROUP BY tsrSiteUrl  ORDER BY tmrVScore desc, tmrAScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc = "  ORDER BY tsrImgPath DESC, tsjStatus DESC, tmrSimilarity DESC";
    String orderByTmrSimilarityDesc_1 = " ORDER BY tsrImgPath DESC, tmrVScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_2 = " ORDER BY tsrImgPath DESC, tmrAScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_3 = " ORDER BY tsrImgPath DESC, tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_4 = " ORDER BY tsrImgPath DESC, tmrVScore desc, tmrAScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_5 = " ORDER BY tsrImgPath DESC, tmrVScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";
    String orderByTmrSimilarityDesc_6 = " ORDER BY tsrImgPath DESC, tmrVScore desc, tmrTScore desc, tmrSimilarity desc, tsrUno desc";

    // String orderByTmrSimilarityAsc = " ORDER BY tmrSimilarity asc, tsrUno desc";
    // String orderByTsrUnoDesc = " ORDER BY tsrUno desc";

    // String orderByTsrUnoDesc_trace = " ORDER BY tsiFstDmlDt desc, tsrUno desc";
    String orderByTsrUnoDesc2_trace = " ORDER BY mstDmlDt desc, tsrUno desc";

/*
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
*/

    String countQuery = "SELECT COUNT(TSR.TSR_UNO)";
    String countQuery2 = "SELECT DISTINCT(COUNT(TSR.TSR_UNO))";

    String countQuery3 = "SELECT COUNT(*) ";
    String limit4 = " LIMIT 4 ";

    String InfoImgSrc = " select " +
            " s1_0.tsi_uno AS tsiUno, " +
            " s1_0.data_stat_cd AS dataStateCd, " +
            " s1_0.tsi_dna_path AS tsiDnaPath, " +
            " s1_0.tsi_dna_text AS tsiDanText, " +
            " s1_0.tsi_img_ext AS tsiImgExt, " +
            " s1_0.tsi_img_height AS tsiImgHeight, " +
            " s1_0.tsi_img_name AS tsiImgName, " +
            " s1_0.tsi_img_path AS tsiImgPath, " +
            " s1_0.tsi_img_real_path AS tsiImgRealPath, " +
            " s1_0.tsi_img_size AS tsiImgSize, " +
            " s1_0.tsi_img_width AS tsiImgWidh, " +
            " s1_0.tsi_type AS tsiType, " +
            " s1_0.tsr_uno AS tsrUno, " +
            " s1_0.user_uno AS userUno " +
            " from tb_search_info s1_0 " +
            " where s1_0.tsi_uno = :tsiUno ";

    String tsrUnoisNotNUll = " WHERE TSI.TSR_UNO IS NOT NULL AND (TSI.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) ";
    String setTmrStat11 = " ";
//    String setTmrStat11 = " AND TMR.TMR_STAT = '11' ";

    // 추적이력 (검색어(타이틀) 검색)
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceHistoryList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining)
    Page<DefaultQueryDtoInterface> getTraceHistoryList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining)
    Page<DefaultQueryDtoInterface> getTraceHistoryMonitoringList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringDeleteRequestList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setTrkStatCd20 )
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteReqList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringDeleteComptList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setTrkStatCd30)
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteComptList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceAllTimeMonitoringChkList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setMonitoringCd20)
    Page<DefaultQueryDtoInterface> allTimeMonitoringList(String keyword, Integer tsiSearchType, Pageable pageable);

    // 추적이력 (대상자 검색)
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceHistoryUserFileList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining)
    Page<DefaultQueryDtoInterface> getTraceUserFileList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceAllTimeMonitoringUserFileChkList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setMonitoringCd20)
    Page<DefaultQueryDtoInterface> allTimeMonitoringUserFileList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringDeleteRequestUserFileList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setTrkStatCd20 )
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteReqUserFileList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringDeleteComptUserFileList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContaining + setTrkStatCd30)
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteComptUserFileList(String keyword, Integer tsiSearchType, Pageable pageable);
    @Query(value = defaultTraceHistoryQuery+fromTraceHistoryQuery+whereTraceMonitoringUserFileList+orderByTraceHistory, nativeQuery = true, countQuery = countQuery2+fromTraceHistoryQuery+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno)
    Page<DefaultQueryDtoInterface> getTraceHistoryMonitoringUserFileList(String keyword, Integer tsiSearchType, Pageable pageable);


    @Query(value = defaultQeury_14, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno)
    Page<DefaultQueryDtoInterface> getTraceHistoryMonitoringTsiUnoList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = traceHistoryMonitoringTsiUnoUserFileList, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno)
    Page<DefaultQueryDtoInterface> getTraceHistoryMonitoringTsiUnoUserFileList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = defaultQeury_15, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setTrkStatCd20 )
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteReqTsiUnoList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = traceHistoryDeleteReqTsiUnoUserFileList, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setTrkStatCd20)
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteReqTsiUnoUserFileList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = defaultQeury_16, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setTrkStatCd30)
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteComptTsiUnoList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = traceHistoryDeleteComptTsiUnoUserFileList, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setTrkStatCd30)
    Page<DefaultQueryDtoInterface> getTraceHistoryDeleteComptTsiUnoUserFileList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = defaultQeury_17, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setMonitoringCd20)
    Page<DefaultQueryDtoInterface> allTimeMonitoringTsiUnoList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);
    @Query(value = allTimeMonitoringTsiUnoUserFileList, nativeQuery = true, countQuery = countQuery2+from+whereTrkStatCdNotNullAndTsrTitleContainingANDTsiUno + setMonitoringCd20)
    Page<DefaultQueryDtoInterface> allTimeMonitoringTsiUnoUserFileList(String keyword, Integer tsiSearchType, Pageable pageable, Integer tsiUno);

    // 검색 결과 검색 이력
    @Query(value = defaultQeury_5+from2+whereTsiUnoTsrTitleLikeTsrStatusIn2+orderByTmrSimilarityDesc, nativeQuery = true, countQuery = countQuery3+from2+whereTsiUnoTsrTitleLikeTsrStatusIn2)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_1, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_1(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_2, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_2(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_3, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_3(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_4, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_4(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_5, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_5(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4, String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);
    @Query(value = defaultQeury+from2+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityDesc_6, nativeQuery = true, countQuery = countQuery+from2+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityDesc_6(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                               String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, List<String> nationCode, Pageable pageable);

/*
    @Query(value = defaultQeury+from+whereTsiUnoTsrTitleLikeTsrStatusIn+orderByTmrSimilarityAsc, nativeQuery = true, countQuery = countQuery+from+whereTsiUnoTsrTitleLikeTsrStatusIn)
    Page<DefaultQueryDtoInterface> getResultInfoListOrderByTmrSimilarityAsc(Integer tsiUno, String keyword, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                                            String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, Pageable pageable);
*/

/*

 */
    // 검색 결과 팝업
    @Query(value = defaultQeury_2+from3+whereTsrUno, nativeQuery = true, countQuery = countQuery+from3+whereTsrUno)
    DefaultQueryDtoInterface getResultInfo(Integer tsrUno);


    @Query(value = InfoImgSrc, nativeQuery = true)
    DefaultQueryDtoInterface getInfoList(Integer tsiUno);

/*
    // index 추적대상 4개
    @Query(value = defaultQeury+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+orderByTsrUnoDesc_trace+limit4, nativeQuery = true, countQuery = countQuery+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+limit4)
    List<DefaultQueryDtoInterface> getTraceListByHome(String tsrDataStatCd, String trkStatCd, String trkStatCd2, String keyword);
*/

    // 모니터링
    @Query(value = defaultQeury+fromForMonitoring+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+orderByTsrUnoDesc2_trace, nativeQuery = true, countQuery = countQuery+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike)
    Page<DefaultQueryDtoInterface> getTraceList(String tsrDataStatCd, String trkStatCd, String trkStatCd2, String keyword, Integer tsiSearchType, Pageable pageable);
/*   @Query(value = defaultQeury+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike+orderByTsrUnoDesc_trace, nativeQuery = true, countQuery = countQuery+from+whereDataStatCdAndTrkStatCdNotAndTrkStatCdTsrTitleLike)
    Page<DefaultQueryDtoInterface> getTraceList(String tsrDataStatCd, String trkStatCd, String trkStatCd2, String keyword, Pageable pageable); */

    // 모니터링 팝업
    @Query(value = defaultQeury+from3+whereTsrUno, nativeQuery = true, countQuery = countQuery+from3+whereTsrUno)
    DefaultQueryDtoInterface getTraceInfo(Integer tsrUno);

    @Query(value = "SELECT TRK_STAT_CD FROM tb_search_result WHERE TSR_UNO = :tsrUno", nativeQuery = true)
    String getTrkStatCd(Integer tsrUno);

    // AND TSJ.TSJ_STATUS = '11'
    @Query(value = defaultQeury_2+from_2+tsrUnoisNotNUll+setTmrStat11+whereSimilarity_3 , nativeQuery = true, countQuery = countQuery+from_2+whereNotice+setTmrStat11+whereSimilarity_3)
    Page<DefaultQueryDtoInterface> getNoticeList(Integer tsiSearchType, Pageable pageable);

    @Query(value = defaultQeury_2+from_2+tsrUnoisNotNUll+setTmrStat11+whereSimilarity_2+limit4, nativeQuery = true)
    List<DefaultQueryDtoInterface> getNoticeListMain(Integer percent, Integer tsiSearchType);

    /*@Query(value = defaultQeury_3+from_5+" WHERE TSI.TSI_UNO = :tsiuno AND TSI.TSI_KEYWORD like :keyword AND TMR.TMR_STAT = '11' "+whereSimilarity_2 , nativeQuery = true, countQuery = countQuery+from_2+" WHERE TSI.TSI_UNO = :tsiuno AND TSI.TSI_KEYWORD=:keyword AND TMR.TMR_STAT = '11'"+whereSimilarity_2)*/
    @Query(value = defaultQeury_3+from_5+setTmrStat11+whereSimilarity_3 , nativeQuery = true, countQuery = countQuery+from_5+setTmrStat11+whereSimilarity_3)
    Page<DefaultQueryDtoInterface> getNoticeSelList(Pageable pageable, Integer tsiUno, String tsiKeyword, Integer tsiSearchType);

    @Query(value = defaultQeury_3+from_6+setTmrStat11+whereSimilarity_3 , nativeQuery = true, countQuery = countQuery+from_6+setTmrStat11+whereSimilarity_3)
    Page<DefaultQueryDtoInterface> getNoticeSelListEmptyKeyword(Pageable pageable, Integer tsiUno, Integer tsiSearchType);
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

    /*
    @Query(value =
            """
            select sr
            from   SearchResultEntity sr
            inner  join SearchInfoEntity si
                   on   si.tsiUno = sr.tsiUno
                   and  si.tsrUno is not null
            where  not exists (
                              select 1
                              from   SearchJobEntity sj
                              where  sj.tsrUno = sr.tsrUno
                              )
            """
    )
    SearchResultEntity findAllBy();
    */

/*
    @Query(value = keywordResultCntList, nativeQuery = true)
    List<String> keywordResultCntList(String fromDate, String toDate);
*/

}


