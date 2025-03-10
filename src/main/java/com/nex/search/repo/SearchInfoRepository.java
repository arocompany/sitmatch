package com.nex.search.repo;

import com.nex.Chart.dto.DateKeywordExcelDto;
import com.nex.Chart.dto.KeywordCntDto;
import com.nex.Chart.dto.StatisticsDto;
import com.nex.Chart.dto.userKeywordCntDto;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.dto.ResultCntQueryDtoInterface;
import com.nex.search.entity.dto.UserIdDtoInterface;
import com.nex.search.entity.dto.UserSearchHistoryDtoInterface;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface SearchInfoRepository extends JpaRepository<SearchInfoEntity, Integer> {

    String dateKeywordResultCntList=" SELECT COUNT(*) AS keywordCnt " +
            " ,SUM((SELECT COUNT(*) " +
            " from tb_search_result tsr " +
            " LEFT OUTER JOIN tb_search_info tsi3 " +
            " ON tsr.tsi_uno = tsi3.tsi_uno " +
            " WHERE  tsr.tsi_uno = tsi.tsi_uno )) AS resultCnt " +
            " ,DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') AS date " +
            " FROM tb_search_info tsi " +
            " WHERE tsi.fst_dml_dt BETWEEN :fromDate AND :toDate " +
            " GROUP BY DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') ";
    String dateSearchInfoResultExcelList =  " SELECT " +
//                                            " tu.user_nm AS userNm " +
//                                            " ,tu.user_id AS userId " +
                                            " COUNT(tsi.tsi_keyword) keywordCnt " +
                                            " ,SUM((SELECT COUNT(DISTINCT tsr.tsr_site_url) " +
                                            " from tb_search_result tsr " +
                                            " LEFT OUTER JOIN tb_search_info tsi2 " +
                                            " ON tsr.tsi_uno = tsi2.tsi_uno " +
                                            " WHERE tsr.tsi_uno = tsi.tsi_uno)) AS resultCnt " +
                                            " ,tsi.fst_dml_dt AS resultDate " +
                                            " FROM tb_search_info tsi " +
                                            " LEFT OUTER JOIN tb_user tu " +
                                            " ON tsi.user_uno = tu.user_uno " +
                                            " WHERE tsi.fst_dml_dt BETWEEN :fromDate AND :toDate " +
                                            " GROUP BY tsi.fst_dml_dt " +
                                            " ORDER BY tsi.fst_dml_dt ASC";

    String userKeywordCntList = " SELECT COUNT(*) AS keywordCnt, tu.user_uno AS userUno, tu.user_id AS userId, "+
                                " SUM((SELECT COUNT(DISTINCT tsr.tsr_site_url) " +
                                " from tb_search_result tsr LEFT OUTER JOIN tb_search_info tsi2 " +
                                " ON tsr.tsi_uno = tsi2.tsi_uno " +
                                " WHERE tsr.tsi_uno = tsi.tsi_uno)) AS resultCnt " +
                                " FROM tb_search_info tsi " +
                                " LEFT OUTER JOIN tb_user tu " +
                                " ON tsi.user_uno = tu.user_uno " +
                                " WHERE tsi.fst_dml_dt LIKE CONCAT(:toDate,'%') " +
                                " GROUP BY tu.user_uno ";
    String searchInfoResultCnt =    " SELECT tsi.tsi_uno AS tsiUno, " +
                                    " tsi.tsi_is_deploy AS tsiIsDeploy, " +
                                    " tsi.tsi_search_type AS tsiSearchType, " +
                                    " tsi.data_stat_cd AS tsiDataStatCd, " +
                                    " tsi.fst_dml_dt AS tsiFstDmlDt, " +
                                    " tsi.lst_dml_dt AS tsiLstDmlDt, " +
                                    " tsi.tsi_dna_path AS tsiDnaPath, " +
                                    " tsi.tsi_dna_text AS tsiDnaText, " +
                                    " tsi.tsi_facebook AS tsiFacebook, " +
                                    " tsi.tsi_google AS tsiGoogle, " +
                                    " tsi.tsi_img_ext AS tsiImgExt, " +
                                    " tsi.tsi_img_height AS tsiImgHeight, " +
                                    " tsi.tsi_img_name AS tsiImgName, " +
                                    " tsi.tsi_img_path AS tsiImgPath, " +
                                    " tsi.tsi_img_real_path AS tsiImgRealPath, " +
                                    " tsi.tsi_img_size AS tsiImgSize, " +
                                    " tsi.tsi_img_width AS tsiImgWidth, " +
                                    " tsi.tsi_instagram AS tsiInstagram, " +
                                    " tsi.tsi_keyword AS tsiKeyword, " +
                                    " tsi.tsi_stat AS tsiStat, " +
                                    " tsi.tsi_twitter AS tsiTwitter, " +
                                    " tsi.tsi_type AS tsiType, " +
                                    " tsi.tsr_uno AS tsrUno, " +
                                    " tsi.user_uno AS tsiUserUno, " +
                                    " tsi.tsi_cnt_tsr resultCnt, " +
                                    " tsi.tsi_cnt_similarity AS tmrSimilarityCnt, " +
                                    " tsi.tsi_cnt_child AS tmrChildCnt" +
                                    ", (SELECT tsj_status FROM tb_search_job WHERE tsi_uno = tsi.tsi_uno AND tsr_img_path IS NOT null ORDER BY tsj_uno DESC LIMIT 1) AS tsjStatus " +
                                    ", (SELECT tsj_status_child FROM tb_search_job WHERE tsi_uno = tsi.tsi_uno AND tsr_img_path IS NOT null ORDER BY tsj_uno DESC LIMIT 1) AS tsjStatusChild " +
                                    ", coalesce(params.tsi_is_nation_kr, 0) tsiIsNationKr " +
                                    ", coalesce(params.tsi_is_nation_us, 0) tsiIsNationUs " +
                                    ", coalesce(params.tsi_is_nation_cn, 0) tsiIsNationCn " +
                                    ", coalesce(params.tsi_is_nation_nl, 0) tsiIsNationNl " +
                                    ", coalesce(params.tsi_is_nation_th, 0) tsiIsNationTh " +
                                    ", coalesce(params.tsi_is_nation_ru, 0) tsiIsNationRu " +
                                    ", coalesce(params.tsi_is_nation_vn, 0) tsiIsNationVn " +
                                    ", coalesce(params.tsi_is_engine_google, 0) tsiIsEngineGoogle " +
                                    ", coalesce(params.tsi_is_engine_youtube, 0) tsiIsEngineYoutube " +
                                    ", coalesce(params.tsi_is_engine_google_reverse_image, 0) tsiIsEngineGoogleReverseImage " +
                                    ", coalesce(params.tsi_is_engine_google_lens, 0) tsiIsEngineGoogleLens " +
                                    ", coalesce(params.tsi_is_engine_baidu, 0) tsiIsEngineBaidu " +
                                    ", coalesce(params.tsi_is_engine_bing, 0) tsiIsEngineBing " +
                                    ", coalesce(params.tsi_is_engine_duckduckgo, 0) tsiIsEngineDuckduckgo " +
                                    ", coalesce(params.tsi_is_engine_yahoo, 0) tsiIsEngineYahoo " +
                                    ", coalesce(params.tsi_is_engine_yandex, 0) tsiIsEngineYandex " +
                                    ", coalesce(params.tsi_is_engine_yandex_image, 0) tsiIsEngineYandexImage " +
                                    ", coalesce(params.tsi_is_engine_naver, 0) tsiIsEngineNaver " +
                                    ", tsi.TSI_USER_FILE as tsiUserFile " +
                                    ", (SELECT CONCAT(MIN(tvi.TVI_IMG_REAL_PATH), '', MIN(tvi.TVI_IMG_NAME)) FROM tb_video_info tvi WHERE tvi.tsi_uno = tsi.tsi_uno) tviImagePath "+
                                    " from tb_search_info tsi " +
                                    " LEFT OUTER JOIN TB_SEARCH_INFO_PARAMS params ON tsi.TSI_UNO = params.TSI_UNO "+
                                    " WHERE tsi.DATA_STAT_CD= :dataStatCd" +
                                    " and tsi.SEARCH_VALUE= :searchValue" +
                                    " and ((:searchUserFile IS NOT NULL AND tsi.TSI_USER_FILE = :searchUserFile ) OR :searchUserFile = '' )" +
                                    " and ((:manageType = '사례번호' and tsi.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') OR (:keyword = '' AND tsi.tsi_user_file IS NULL )) OR :manageType != '사례번호' )  " +
                                    " and ((:manageType = '검색어' and tsi.TSI_KEYWORD like '%' :keyword '%' ) OR :manageType != '검색어')" +
                                    " and tsi.TSR_UNO is null " +
                                    "  AND (tsi.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) "+
                                    "  AND (tsi.TSI_IS_DEPLOY = :tsiIsDeployType OR :tsiIsDeployType = 0 ) "+
                                    " order by  tsi.tsi_uno desc ";

    String userSearchInfoList = " SELECT tsi.tsi_uno AS tsiUno, " +
                                " tsi.tsi_is_deploy AS tsiIsDeploy, " +
                                " tsi.data_stat_cd AS tsiDataStatCd, " +
                                " tsi.tsi_search_type AS tsiSearchType, " +
                                " tsi.fst_dml_dt AS tsiFstDmlDt, " +
                                " tsi.lst_dml_dt AS tsiLstDmlDt, " +
                                " tsi.tsi_dna_path AS tsiDnaPath, " +
                                " tsi.tsi_dna_text AS tsiDnaText, " +
                                " tsi.tsi_facebook AS tsiFacebook, " +
                                " tsi.tsi_google AS tsiGoogle, " +
                                " tsi.tsi_img_ext AS tsiImgExt, " +
                                " tsi.tsi_img_height AS tsiImgHeight, " +
                                " tsi.tsi_img_name AS tsiImgName, " +
                                " tsi.tsi_img_path AS tsiImgPath, " +
                                " tsi.tsi_img_real_path AS tsiImgRealPath, " +
                                " tsi.tsi_img_size AS tsiImgSize, " +
                                " tsi.tsi_img_width AS tsiImgWidth, " +
                                " tsi.tsi_instagram AS tsiInstagram, " +
                                " tsi.tsi_keyword AS tsiKeyword, " +
                                " tsi.tsi_stat AS tsiStat, " +
                                " tsi.tsi_twitter AS tsiTwitter, " +
                                " tsi.tsi_type AS tsiType, " +
                                " tsi.tsr_uno AS tsrUno, " +
                                " tsi.user_uno AS tsiUserUno, " +
                                " tsi.tsi_cnt_tsr AS resultCnt, " +
                                " tsi.tsi_cnt_similarity AS tmrSimilarityCnt, " +
                                " tsi.tsi_cnt_child AS tmrChildCnt, " +
                                " (SELECT tsj_status FROM tb_search_job WHERE tsi_uno = tsi.tsi_uno AND tsr_img_path IS NOT null ORDER BY tsj_uno DESC LIMIT 1) AS tsjStatus " +
                                ", (SELECT tsj_status_child FROM tb_search_job WHERE tsi_uno = tsi.tsi_uno AND tsr_img_path IS NOT null ORDER BY tsj_uno DESC LIMIT 1) AS tsjStatusChild " +
                                ", coalesce(params.tsi_is_nation_kr, 0) tsiIsNationKr " +
                                ", coalesce(params.tsi_is_nation_us, 0) tsiIsNationUs " +
                                ", coalesce(params.tsi_is_nation_cn, 0) tsiIsNationCn " +
                                ", coalesce(params.tsi_is_nation_nl, 0) tsiIsNationNl " +
                                ", coalesce(params.tsi_is_nation_th, 0) tsiIsNationTh " +
                                ", coalesce(params.tsi_is_nation_ru, 0) tsiIsNationRu " +
                                ", coalesce(params.tsi_is_nation_vn, 0) tsiIsNationVn " +
                                ", coalesce(params.tsi_is_engine_google, 0) tsiIsEngineGoogle " +
                                ", coalesce(params.tsi_is_engine_youtube, 0) tsiIsEngineYoutube " +
                                ", coalesce(params.tsi_is_engine_google_reverse_image, 0) tsiIsEngineGoogleReverseImage " +
                                ", coalesce(params.tsi_is_engine_google_lens, 0) tsiIsEngineGoogleLens " +
                                ", coalesce(params.tsi_is_engine_baidu, 0) tsiIsEngineBaidu " +
                                ", coalesce(params.tsi_is_engine_bing, 0) tsiIsEngineBing " +
                                ", coalesce(params.tsi_is_engine_duckduckgo, 0) tsiIsEngineDuckduckgo " +
                                ", coalesce(params.tsi_is_engine_yahoo, 0) tsiIsEngineYahoo " +
                                ", coalesce(params.tsi_is_engine_yandex, 0) tsiIsEngineYandex " +
                                ", coalesce(params.tsi_is_engine_yandex_image, 0) tsiIsEngineYandexImage " +
                                ", coalesce(params.tsi_is_engine_naver, 0) tsiIsEngineNaver " +
                                " , tsi.tsi_user_file as tsiUserFile, " +
                                " (SELECT CONCAT(MIN(tvi.TVI_IMG_REAL_PATH), '', MIN(tvi.TVI_IMG_NAME)) FROM tb_video_info tvi WHERE tvi.tsi_uno = tsi.tsi_uno) tviImagePath "+
                                " from tb_search_info tsi " +
                                " LEFT OUTER JOIN TB_SEARCH_INFO_PARAMS params ON tsi.TSI_UNO = params.TSI_UNO "+
                                " WHERE tsi.DATA_STAT_CD= :dataStatCd" +
                                " and tsi.SEARCH_VALUE= :searchValue" +
                                " and ((:searchUserFile IS NOT NULL AND tsi.TSI_USER_FILE = :searchUserFile ) OR :searchUserFile = '' )" +
                                " and ((:manageType = '사례번호' and tsi.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') OR (:keyword = '' AND tsi.tsi_user_file IS NULL )) OR :manageType != '사례번호' )  " +
                                " and ((:manageType = '검색어' and tsi.TSI_KEYWORD like '%' :keyword '%' ) OR :manageType != '검색어')" +
                                " and tsi.TSR_UNO is null " +
                                " and tsi.user_uno = :userUno " +
                                "  AND (tsi.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
                                "  AND (tsi.TSI_IS_DEPLOY = :tsiIsDeployType OR :tsiIsDeployType = 0 ) " +
                                " order by  tsi.tsi_uno desc ";
    String searchInfoCount= " select count(tsi.TSI_UNO) " +
                            " from tb_search_info tsi " +
                            " where tsi.DATA_STAT_CD= :dataStatCd " +
                            " and tsi.SEARCH_VALUE=:searchValue " +
                            " and ((:searchUserFile IS NOT NULL AND tsi.TSI_USER_FILE = :searchUserFile ) OR :searchUserFile = '' )" +
                            " and ((:manageType = '사례번호' and tsi.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') OR (:keyword = '' AND tsi.tsi_user_file IS NULL )) OR :manageType != '사례번호' )  " +
                            " and ((:manageType = '검색어' and tsi.TSI_KEYWORD like '%' :keyword '%' ) OR :manageType != '검색어')" +
                            " and tsi.TSR_UNO is NULL"+
                            "  AND (tsi.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
                            "  AND (tsi.TSI_IS_DEPLOY = :tsiIsDeployType OR :tsiIsDeployType = 0 ) " ;

    String userSearchInfoCount= " select count(tsi.TSI_UNO) " +
                                " from tb_search_info tsi " +
                                " where tsi.DATA_STAT_CD= :dataStatCd " +
                                " and tsi.SEARCH_VALUE=:searchValue " +
                                " and ((:searchUserFile IS NOT NULL AND tsi.TSI_USER_FILE = :searchUserFile ) OR :searchUserFile = '' )" +
                                " and ((:manageType = '사례번호' and tsi.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') OR (:keyword = '' AND tsi.tsi_user_file IS NULL )) OR :manageType != '사례번호' )  " +
                                " and ((:manageType = '검색어' and tsi.TSI_KEYWORD like '%' :keyword '%' ) OR :manageType != '검색어')" +
                                " AND tsi.USER_UNO = :userUno " +
                                " and tsi.TSR_UNO is NULL" +
                                "  AND (tsi.TSI_SEARCH_TYPE = :tsiSearchType OR :tsiSearchType = 0 ) " +
                                "  AND (tsi.TSI_IS_DEPLOY = :tsiIsDeployType OR :tsiIsDeployType = 0 ) " ;

    String searchKeywordDateCnt = "SELECT DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') AS searchDate " +
                                " FROM tb_search_info tsi " +
                                " WHERE tsi.tsr_uno IS null " +
                                " AND fst_dml_dt BETWEEN :fromDate AND :toDate" +
                                " GROUP BY DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') " +
                                " ORDER BY searchDate asc ";
    String searchKeywordCnt =   " SELECT COUNT(tsi.tsi_keyword) AS searchCnt " +
                                " FROM tb_search_info tsi " +
                                " WHERE tsi.tsr_uno IS null " +
                                " AND fst_dml_dt BETWEEN :fromDate AND :toDate" +
                                " GROUP BY DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') ";

    String userSearchHistoryCount = " SELECT COUNT(*) FROM tb_search_info tsi WHERE tsi_user_file IS NOT NULL and tsi_user_file LIKE CONCAT('%',:searchKeyword,'%') AND SEARCH_VALUE = '0' AND DATA_STAT_CD = '10' GROUP BY tsi_user_file ";

    String allUserSearchHistoryList =  "SELECT" +
            " tsuf_user_file as tsiUserFile, " +
            " (SELECT GROUP_CONCAT(TSIMH_CREATE_DATE ORDER BY TSIMH_CREATE_DATE DESC SEPARATOR '   ') FROM tb_search_info_monitoring_history tsimh INNER JOIN tb_search_info tsi2 ON tsimh.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10') tsimhCreateDate," +
            " (SELECT COUNT(*) FROM tb_search_result tsr INNER JOIN tb_search_info tsi2 ON tsr.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10' AND tsr.TRK_STAT_CD = '10' ) AS monitoringCnt," +
            " (SELECT COUNT(*) FROM tb_search_result tsr INNER JOIN tb_search_info tsi2 ON tsr.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10' AND tsr.TRK_STAT_CD = '20' ) AS deleteReqCnt," +
            " (SELECT COUNT(*) FROM tb_search_result tsr INNER JOIN tb_search_info tsi2 ON tsr.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10' AND tsr.TRK_STAT_CD = '30' ) AS deleteConfirmCnt," +
            " if((SELECT COUNT(*) FROM tb_search_result tsr INNER JOIN tb_search_info tsi2 ON tsr.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10' AND tsr.MONITORING_CD = '20' )>0, 'Y', 'N') AS allDayMonitoringYn," +
            " (SELECT COUNT(*) FROM tb_search_result tsr INNER JOIN tb_search_info tsi2 ON tsr.TSI_UNO = tsi2.tsi_uno WHERE tsi2.tsuf_uno = tsuf.tsuf_uno AND tsi2.SEARCH_VALUE = '0' AND tsi2.DATA_STAT_CD = '10' AND tsr.MONITORING_CD = '20' ) AS reDsmnCnt," +
            " (SELECT sum(tsi_cnt_tsr) FROM tb_search_info WHERE tsuf_uno = tsuf.tsuf_uno) resultCnt " +
            " , (SELECT sum(tsi_monitoring_cnt) FROM tb_search_info WHERE tsuf_uno = tsuf.tsuf_uno) allTimeCnt" +
            " FROM tb_search_user_file tsuf" +
            " WHERE tsuf_user_file IS NOT NULL" +
            " and tsuf_user_file LIKE CONCAT('%',:searchKeyword,'%')  " +
            " ORDER BY LAST_DML_DT DESC ";
    String userSearchHistoryList =  " SELECT " +
                                    " tsi.tsi_uno AS tsiUno, " +
                                    " tsi.tsi_user_file AS tsiUserFile, " +
                                    " (SELECT group_concat(TSIMH_CREATE_DATE ORDER BY TSIMH_CREATE_DATE DESC SEPARATOR '   ') FROM tb_search_info_monitoring_history WHERE tsi_uno = tsi.tsi_uno) tsimhCreateDate, " +
                                    " (SELECT COUNT(tsr3.tsr_uno) " +
                                    " FROM tb_search_result tsr3 " +
                                    " inner JOIN tb_search_info tsi3 " +
                                    " ON tsr3.TSI_UNO = tsi3.tsi_uno " +
                                    " WHERE tsr3.TSI_UNO = tsi.tsi_uno " +
                                    " AND tsr3.TRK_STAT_CD = '10') AS monitoringCnt, " +
                                    " (SELECT COUNT(tsr4.tsr_uno) " +
                                    " FROM tb_search_result tsr4 " +
                                    " inner JOIN tb_search_info tsi4 " +
                                    " ON tsr4.TSI_UNO = tsi4.tsi_uno " +
                                    " WHERE tsr4.TSI_UNO = tsi.tsi_uno " +
                                    " AND tsr4.trk_stat_cd = '20') AS deleteReqCnt, " +
                                    " (SELECT COUNT(tsr5.tsr_uno) " +
                                    " FROM tb_search_result tsr5 " +
                                    " inner JOIN tb_search_info tsi5 " +
                                    " ON tsr5.TSI_UNO = tsi5.tsi_uno " +
                                    " WHERE tsr5.TSI_UNO = tsi.tsi_uno " +
                                    " AND tsr5.trk_stat_cd = '30') AS deleteConfirmCnt, " +
                                    " (CASE " +
                                    " WHEN ((SELECT COUNT(tsr6.tsr_uno) " +
                                    " FROM tb_search_result tsr6 " +
                                    " inner JOIN tb_search_info tsi6 " +
                                    " ON tsr6.TSI_UNO = tsi6.tsi_uno " +
                                    " WHERE tsr6.TSI_UNO = tsi.tsi_uno " +
                                    " AND tsr6.monitoring_cd='20') > 0 ) THEN 'Y' " +
                                    " ELSE 'N' " +
                                    " END ) AS allDayMonitoringYn, " +
                                    " tsi.tsi_monitoring_cnt AS allTimeCnt,  " +
                                    " (SELECT COUNT(tsr8.tsr_uno) " +
                                    " FROM  tb_search_result tsr8 " +
                                    " WHERE tsr8.tsi_uno = tsi.tsi_uno " +
                                    " AND tsr8.monitoring_cd='20') AS reDsmnCnt, " +
                                    " (SELECT COUNT(DISTINCT tsr.tsr_site_url) " +
                                    " FROM tb_search_result tsr " +
                                    " inner JOIN tb_search_info tsi_2 " +
                                    " ON tsr.TSI_UNO = tsi_2.tsi_uno " +
                                    " WHERE tsr.TSI_UNO = tsi.tsi_uno) AS resultCnt " +
                                    " FROM  tb_search_info tsi " +
                                    " WHERE tsi.tsi_user_file LIKE CONCAT('%',:searchKeyword,'%')  " +
                                    " AND SEARCH_VALUE = '0' "+
                                    " AND TSI.DATA_STAT_CD = '10' "+
                                    " AND TSI.USER_UNO = :userUno"+
                                    " ORDER BY tsi.tsi_uno DESC   ";
    String userSearchHistoryCount2 = " SELECT COUNT(*) FROM tb_search_info tsi WHERE tsi_user_file IS NOT NULL and tsi_user_file LIKE CONCAT('%',:searchKeyword,'%') AND SEARCH_VALUE = '0' AND DATA_STAT_CD = '10' GROUP BY tsi_user_file AND USER_UNO = :userUno ";

    String statisticsSearchInfoByTsiType = "SELECT NUMBER tsiType, coalesce(tsi.cnt, 0) cnt FROM (  SELECT 11 AS NUMBER UNION ALL SELECT 13 UNION ALL SELECT 15 UNION ALL SELECT 17 UNION ALL SELECT 19 ) base LEFT OUTER JOIN " +
            " ( SELECT tsi_type, COUNT(*) cnt " +
            "FROM tb_search_info " +
            "WHERE tsr_uno IS null " +
            "AND data_stat_cd = 10 " +
            "AND search_value = 0 " +
            "AND FST_DML_DT >= :searchStartDate AND :searchEndDate >= FST_DML_DT " +
            "AND TSI_SEARCH_TYPE = :tsiSearchType " +
            "GROUP BY tsi_type ) tsi ON base.number = tsi.tsi_type";

    String statisticsSearchInfoByTsiTypeForDeploy = "SELECT NUMBER tsiType, coalesce(tsi.cnt, 0) cnt FROM (  SELECT 11 AS NUMBER UNION ALL SELECT 13 UNION ALL SELECT 15 UNION ALL SELECT 17 UNION ALL SELECT 19 ) base LEFT OUTER JOIN " +
            " ( SELECT tsi_type, COUNT(*) cnt " +
            "FROM tb_search_info " +
            "WHERE tsr_uno IS null " +
            "AND data_stat_cd = 10 " +
            "AND search_value = 0 " +
            "AND tsi_is_deploy = 1 " +
            "AND FST_DML_DT >= :searchStartDate AND :searchEndDate >= FST_DML_DT " +
            "AND TSI_SEARCH_TYPE = :tsiSearchType " +
            "GROUP BY tsi_type ) tsi ON base.number = tsi.tsi_type";

    String statisticsSearchResultByTsiType = "SELECT NUMBER tsiType, coalesce(tsi.cnt, 0) cnt FROM (  SELECT 11 AS NUMBER UNION ALL SELECT 13 UNION ALL SELECT 15 UNION ALL SELECT 17 UNION ALL SELECT 19 ) base LEFT OUTER JOIN " +
            " ( SELECT tsi_type, SUM(b) cnt FROM ( " +
            " SELECT tsi.tsi_uno, tsi_type, COUNT(DISTINCT tsr_site_url) b " +
            " FROM tb_search_info tsi " +
            " INNER JOIN tb_search_result tsr ON tsi.tsi_uno = tsr.tsi_uno " +
            " INNER JOIN tb_search_job tsj ON tsr.tsi_uno = tsj.tsi_uno AND tsr.tsr_uno = tsj.tsr_uno " +
//            " INNER JOIN (SELECT MIN(tsr_uno) tsr_uno, MIN(tsi_uno) tsi_uno from tb_search_result GROUP BY tsr_site_url) tsr2 ON tsr.tsr_uno = tsr2.tsr_uno AND tsr.tsi_uno = tsr2.tsi_uno " +
//            " LEFT OUTER JOIN tb_match_result tmr ON tsr.TSR_UNO = tmr.tsr_uno" +
//            " where IF( tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', CEILING( ( ( CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE TMR_V_SCORE END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE TMR_A_SCORE END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE TMR_T_SCORE END ) /" +
//            " ( CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE 1 END ) ) * 100 ) ) > 1" +
            " WHERE tsi.tsr_uno IS null " +
            " AND tsi.data_stat_cd = 10 " +
            " AND tsi.search_value = 0 " +
            " AND tsr.TSR_UNO IS NOT null " +
            " AND tsi.FST_DML_DT >= :searchStartDate AND :searchEndDate >= tsi.FST_DML_DT " +
            " AND tsi.TSI_SEARCH_TYPE = :tsiSearchType " +
            " GROUP BY tsi.tsi_uno,tsi_type " +
            " ) a " +
            " GROUP BY tsi_type ) tsi ON base.number = tsi.tsi_type ";

    String statisticsSearchInfoMonitoringByTsiType = "SELECT NUMBER tsiType, coalesce(tsi.cnt, 0) cnt FROM (  SELECT 11 AS NUMBER UNION ALL SELECT 13 UNION ALL SELECT 15 UNION ALL SELECT 17 UNION ALL SELECT 19 ) base LEFT OUTER JOIN " +
            " ( SELECT tsi_type, COUNT(*) cnt " +
            "FROM tb_search_info tsi " +
            "WHERE tsr_uno IS NOT NULL " +
            "AND data_stat_cd = 10 " +
            "AND search_value = 0 " +
            "AND FST_DML_DT >= :searchStartDate AND :searchEndDate >= FST_DML_DT " +
            "AND TSI_SEARCH_TYPE = :tsiSearchType " +
            "GROUP BY tsi_type ) tsi ON base.number = tsi.tsi_type";
    String statisticsSearchResultMonitoringByTsiType = "SELECT NUMBER tsiType, coalesce(tsi.cnt, 0) cnt FROM (  SELECT 11 AS NUMBER UNION ALL SELECT 13 UNION ALL SELECT 15 UNION ALL SELECT 17 UNION ALL SELECT 19 ) base LEFT OUTER JOIN " +
            " ( SELECT tsi_type, SUM(b) cnt FROM ( " +
            " SELECT tsi.tsi_uno, tsi_type, COUNT(DISTINCT tsr_site_url) b " +
            " FROM tb_search_info tsi " +
            " INNER JOIN tb_search_result tsr ON tsi.tsi_uno = tsr.tsi_uno " +
            " INNER JOIN tb_search_job tsj ON tsr.tsi_uno = tsj.tsi_uno AND tsr.tsr_uno = tsj.tsr_uno " +
//            "where IF(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0,'0',CEILING((( " +
//            "CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE TMR_V_SCORE END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE TMR_A_SCORE END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE TMR_T_SCORE END) " +
//            "/ (CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE 1 END)) * 100)) > 1 " +
            "WHERE tsi.tsr_uno IS not null " +
            "AND search_value = 0 " +
            "AND tsi.FST_DML_DT >= :searchStartDate AND :searchEndDate >= tsi.FST_DML_DT " +
            "AND TSI_SEARCH_TYPE = :tsiSearchType " +
            " GROUP BY tsi.tsi_uno,tsi_type " +
            " ) a " +
            "GROUP BY tsi_type " +
            "order BY tsi_type ) tsi ON base.number = tsi.tsi_type";
    String statisticsSearchInfoMonitoringByTsiTypeAndUser = "SELECT tsi.user_uno userUno, SUM(cnt) cnt, " +
            "SUM(if(tsi.tsi_type = 11, cnt, 0)) cntKeyword, " +
            "SUM(if(tsi.tsi_type = 13, cnt, 0)) cntKeywordImg, " +
            "SUM(if(tsi.tsi_type = 15, cnt, 0)) cntKeywordMov, " +
            "SUM(if(tsi.tsi_type = 17, cnt, 0)) cntImg, " +
            "SUM(if(tsi.tsi_type = 19, cnt, 0)) cntMov, " +
            " tu.USER_ID userId, tu.USER_NM userNm " +
            "FROM ( SELECT  user_uno ,tsi_type ,COUNT(*) cnt " +
            "FROM tb_search_info " +
            "WHERE data_stat_cd = 10 " +
            "AND search_value = 0 " +
            "AND tsr_uno IS NULL " +
            "AND FST_DML_DT >= :searchStartDate AND :searchEndDate >= FST_DML_DT " +
            "AND TSI_SEARCH_TYPE = :tsiSearchType " +
            "GROUP BY user_uno, tsi_type " +
            ") tsi " +
            "LEFT OUTER JOIN tb_user tu " +
            "ON tsi.user_uno = tu.user_uno " +
            "GROUP BY tsi.user_uno";
    List<SearchInfoEntity> findTop10ByTsiTypeInAndTsiStatOrderByTsiUnoDesc(List<String> tsiType, String tsiStat);
    List<SearchInfoEntity> findAllByOrderByTsiUnoDesc();
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd,String searchValue, String keyword, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Integer userUno, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String searchValue, String keyword, Integer userUno, Pageable pageable);

    @Query(value = searchInfoResultCnt, nativeQuery = true, countQuery=searchInfoCount)
    Page<ResultCntQueryDtoInterface> getSearchInfoResultCnt(String dataStatCd, String searchValue, String keyword, Integer tsiSearchType, String manageType, String searchUserFile, Integer tsiIsDeployType, Pageable pageable);

    @Query(value = userSearchInfoList, nativeQuery = true, countQuery=userSearchInfoCount)
    Page<ResultCntQueryDtoInterface> getUserSearchInfoList(String dataStatCd, String searchValue, String keyword, Integer userUno, Integer tsiSearchType, String manageType, String searchUserFile, Integer tsiIsDeployType, Pageable pageable);

    @Query(value = allUserSearchHistoryList, nativeQuery = true, countQuery=userSearchHistoryCount)
    Page<UserSearchHistoryDtoInterface> getAllUserSearchHistoryList(Pageable pageable, String searchKeyword);

    @Query(value = userSearchHistoryList, nativeQuery = true, countQuery=userSearchHistoryCount2)
    Page<UserSearchHistoryDtoInterface> getUserSearchHistoryList(Pageable pageable, String searchKeyword, int userUno);

    @Query(value = "SELECT TU.USER_UNO as userUno, TU.USER_ID as userId FROM TB_USER TU", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByUserUno();

    @Query(value = "SELECT TSI.TSI_UNO as tsiUno, TU.USER_ID as userId FROM TB_SEARCH_INFO TSI LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByTsiUno();

    @Query("select concat(TSI.tsiImgPath, TSI.tsiImgName) from SearchInfoEntity TSI where TSI.tsiUno = :tsiUno")
    String getSearchInfoImgUrl(Integer tsiUno);

    @Query(value = "SELECT TSI.TSI_TYPE FROM TB_SEARCH_INFO TSI WHERE TSI.TSI_UNO = :tsiUno", nativeQuery = true)
    String getSearchInfoTsiType(Integer tsiUno);

    @Query(value = "SELECT TSI.TSI_IS_DEPLOY FROM TB_SEARCH_INFO TSI WHERE TSI.TSI_UNO = :tsiUno", nativeQuery = true)
    Integer getSearchInfoTsiIsDeploy(Integer tsiUno);

    SearchInfoEntity findByTsiUno(Integer tsiUno);

    @Query(value = "SELECT tsi_search_type FROM TB_SEARCH_INFO WHERE TSI_UNO = :tsiUno", nativeQuery = true)
    String getTsiSearchType(Integer tsiUno);

    List<SearchInfoEntity> findByTsiUnoIn(List<Integer> tsiUnos);

    List<SearchInfoEntity> findByTsrUnoIn(List<Integer> tsrUnos);

//    Optional<SearchInfoEntity> findByTsrUno(Integer tsrUno);

    SearchInfoEntity findByTsrUno(Integer tsrUno);

    @Query(value = searchKeywordDateCnt, nativeQuery = true)
    List<String> searchKeywordDateCnt(String fromDate, String toDate);

    @Query(value = searchKeywordCnt, nativeQuery = true)
    List<String> searchKeywordCnt(String fromDate, String toDate);

    @Query(value = userKeywordCntList, nativeQuery = true)
    List<userKeywordCntDto> userKeywordCntList(String toDate);

    @Query(value = dateSearchInfoResultExcelList, nativeQuery = true)
    List<DateKeywordExcelDto> dateSearchInfoResultExcelList(String fromDate, String toDate);

    @Query(value = dateKeywordResultCntList, nativeQuery = true)
    List<KeywordCntDto> dateKeywordResultCntList(String fromDate, String toDate);

    @Query(value = statisticsSearchInfoByTsiType, nativeQuery = true)
    List<StatisticsDto> statisticsSearchInfoByTsiType(String searchStartDate, String searchEndDate, Integer tsiSearchType);
    @Query(value = statisticsSearchInfoByTsiTypeForDeploy, nativeQuery = true)
    List<StatisticsDto> statisticsSearchInfoByTsiTypeForDeploy(String searchStartDate, String searchEndDate, Integer tsiSearchType);

    @Query(value = statisticsSearchResultByTsiType, nativeQuery = true)
    List<StatisticsDto> statisticsSearchResultByTsiType(String searchStartDate, String searchEndDate, Integer tsiSearchType);

    @Query(value = statisticsSearchInfoMonitoringByTsiType, nativeQuery = true)
    List<StatisticsDto> statisticsSearchInfoMonitoringByTsiType(String searchStartDate, String searchEndDate, Integer tsiSearchType);

    @Query(value = statisticsSearchResultMonitoringByTsiType, nativeQuery = true)
    List<StatisticsDto> statisticsSearchResultMonitoringByTsiType(String searchStartDate, String searchEndDate, Integer tsiSearchType);

    @Query(value = statisticsSearchInfoMonitoringByTsiTypeAndUser, nativeQuery = true)
    List<StatisticsDto> statisticsSearchInfoMonitoringByTsiTypeAndUser(String searchStartDate, String searchEndDate, Integer tsiSearchType);

    List<SearchInfoEntity> findTop10ByTsiStatAndDataStatCdOrderByTsiUnoAsc(String tsiStat, String dataStatCd);
    List<SearchInfoEntity> findTop10ByTsiStatAndDataStatCdAndTsiSearchTypeOrderByTsiUnoAsc(String tsiStat, String dataStatCd, String tsiSearchType);
}