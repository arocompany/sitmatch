package com.nex.search.repo;

import com.nex.Chart.dto.DateKeywordExcelDto;
import com.nex.Chart.dto.KeywordCntDto;
import com.nex.Chart.dto.SearchKeywordCntDto;
import com.nex.Chart.dto.userKeywordCntDto;
import com.nex.search.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    String dateSearchInfoResultExcelList =  " SELECT tu.user_nm AS userNm " +
                                            " ,tu.user_id AS userId " +
                                            " ,COUNT(tsi.tsi_keyword) keywordCnt " +
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

    String userKeywordCntList = " SELECT COUNT(tsi.tsi_keyword) AS keywordCnt " +
                                " ,user_id AS userId ,SUM((SELECT COUNT(DISTINCT tsr.tsr_site_url) " +
                                " from tb_search_result tsr LEFT OUTER JOIN tb_search_info tsi2 " +
                                " ON tsr.tsi_uno = tsi2.tsi_uno " +
                                " WHERE tsr.tsi_uno = tsi.tsi_uno)) AS resultCnt " +
                                " FROM tb_search_info tsi " +
                                " LEFT OUTER JOIN tb_user tu " +
                                " ON tsi.user_uno = tu.user_uno " +
                                " WHERE tsi.fst_dml_dt LIKE CONCAT(:toDate,'%') " +
                                " GROUP BY tu.user_id " +
                                " ORDER BY tu.user_uno ASC";
    String searchInfoResultCnt =    " SELECT tsi.tsi_uno AS tsiUno, " +
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
                                    " (SELECT COUNT(DISTINCT tsr.tsr_site_url)  FROM tb_search_result tsr " +
                                    " inner JOIN tb_search_info tsi_2 " +
                                    " ON tsr.TSI_UNO = tsi_2.tsi_uno " +
                                    " WHERE tsr.TSI_UNO = tsi.tsi_uno) AS resultCnt, " +
                                    " (SELECT COUNT(DISTINCT(tsr_2.tsr_site_url)) " +
                                    " FROM tb_search_result tsr_2 " +
                                    " LEFT OUTER JOIN tb_match_result tmr " +
                                    " ON tsr_2.TSR_UNO = tmr.tsr_uno " +
                                    " WHERE tmr.tsi_uno = tsi.tsi_uno " +
                                    " AND tsr_2.tsi_uno = tsi.tsi_uno " +
                                    " AND tmr.tmr_stat=11 " +
                                    " AND if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', " +
                                    " ceiling(((case " +
                                    " when isnull(tmr.TMR_V_SCORE) then 0 " +
                                    " ELSE TMR_V_SCORE " +
                                    " end + case " +
                                    " when isnull(tmr.TMR_A_SCORE) then 0 " +
                                    " ELSE TMR_A_SCORE " +
                                    " end + case " +
                                    " when isnull(tmr.TMR_T_SCORE) then 0 " +
                                    " ELSE TMR_T_SCORE " +
                                    " end) / (case " +
                                    " when isnull(tmr.TMR_V_SCORE) then 0 " +
                                    " else 1 " +
                                    " end + case " +
                                    " when isnull(tmr.TMR_A_SCORE) then 0 " +
                                    " else 1 " +
                                    " end + case " +
                                    " when isnull(tmr.TMR_T_SCORE) then 0 " +
                                    " else 1 " +
                                    " end)) * 1000)) > 1) AS tmrSimilarityCnt " +
                                    " from tb_search_info tsi " +
                                    " WHERE tsi.DATA_STAT_CD= :dataStatCd" +
                                    " and tsi.SEARCH_VALUE= :searchValue" +
                                    " and tsi.TSI_KEYWORD like '%' :keyword '%' " +
                                    " and tsi.TSR_UNO is null " +
                                    " order by  tsi.tsi_uno desc ";

    String userSearchInfoList = " SELECT tsi.tsi_uno AS tsiUno, " +
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
                                " (SELECT COUNT(DISTINCT tsr.tsr_site_url)  FROM tb_search_result tsr " +
                                " inner JOIN tb_search_info tsi_2 " +
                                " ON tsr.TSI_UNO = tsi_2.tsi_uno " +
                                " WHERE tsr.TSI_UNO = tsi.tsi_uno) AS resultCnt, " +
                                " (SELECT COUNT(DISTINCT(tsr_2.tsr_site_url)) " +
                                " FROM tb_search_result tsr_2 " +
                                " LEFT OUTER JOIN tb_match_result tmr " +
                                " ON tsr_2.TSR_UNO = tmr.tsr_uno " +
                                " WHERE tmr.tsi_uno = tsi.tsi_uno " +
                                " AND tsr_2.tsi_uno = tsi.tsi_uno " +
                                " AND tmr.tmr_stat=11 " +
                                " AND if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', " +
                                " ceiling(((case " +
                                " when isnull(tmr.TMR_V_SCORE) then 0 " +
                                " ELSE TMR_V_SCORE " +
                                " end + case " +
                                " when isnull(tmr.TMR_A_SCORE) then 0 " +
                                " ELSE TMR_A_SCORE " +
                                " end + case " +
                                " when isnull(tmr.TMR_T_SCORE) then 0 " +
                                " ELSE TMR_T_SCORE " +
                                " end) / (case " +
                                " when isnull(tmr.TMR_V_SCORE) then 0 " +
                                " else 1 " +
                                " end + case " +
                                " when isnull(tmr.TMR_A_SCORE) then 0 " +
                                " else 1 " +
                                " end + case " +
                                " when isnull(tmr.TMR_T_SCORE) then 0 " +
                                " else 1 " +
                                " end)) * 1000)) > 1) AS tmrSimilarityCnt " +
                                " from tb_search_info tsi " +
                                " WHERE tsi.DATA_STAT_CD= :dataStatCd" +
                                " and tsi.SEARCH_VALUE= :searchValue" +
                                " and tsi.TSI_KEYWORD like '%' :keyword '%' " +
                                " and tsi.TSR_UNO is null " +
                                " and tsi.user_uno = :userUno " +
                                " order by  tsi.tsi_uno desc ";
    String searchInfoCount= " select count(s1_0.TSI_UNO) " +
                            " from tb_search_info s1_0 " +
                            " where s1_0.DATA_STAT_CD= :dataStatCd " +
                            " and s1_0.SEARCH_VALUE=:searchValue " +
                            " and s1_0.TSI_KEYWORD like CONCAT('%',:keyword,'%') " +
                            " and s1_0.TSR_UNO is NULL";

    String userSearchInfoCount= " select count(s1_0.TSI_UNO) " +
            " from tb_search_info s1_0 " +
            " where s1_0.DATA_STAT_CD= :dataStatCd " +
            " and s1_0.SEARCH_VALUE=:searchValue " +
            " and s1_0.TSI_KEYWORD like CONCAT('%',:keyword,'%') " +
            " AND s1_0.USER_UNO = :userUno " +
            " and s1_0.TSR_UNO is NULL";

    String searchKeywordDateCnt = "SELECT DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') AS searchDate " +
                                  " FROM tb_search_info tsi " +
                                  " WHERE tsi.tsr_uno IS null " +
                                  " AND fst_dml_dt BETWEEN :fromDate AND :toDate" +
                                  " GROUP BY DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') " +
                                  " ORDER BY tsi.fst_dml_dt asc ";
    String searchKeywordCnt =   " SELECT COUNT(tsi.tsi_keyword) AS searchCnt " +
                                " FROM tb_search_info tsi " +
                                " WHERE tsi.tsr_uno IS null " +
                                " AND fst_dml_dt BETWEEN :fromDate AND :toDate" +
                                " GROUP BY DATE_FORMAT(tsi.fst_dml_dt,'%Y%m%d') " +
                                " ORDER BY tsi.fst_dml_dt asc ";

    // String searchKeywordUserCnt = "";

    List<SearchInfoEntity> findAllByOrderByTsiUnoDesc();
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd,String searchValue, String keyword, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Integer userUno, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String searchValue, String keyword, Integer userUno, Pageable pageable);

    @Query(value = searchInfoResultCnt, nativeQuery = true, countQuery=searchInfoCount)
    Page<ResultCntQueryDtoInterface> getSearchInfoResultCnt(String dataStatCd, String searchValue, String keyword, Pageable pageable);

    @Query(value = userSearchInfoList, nativeQuery = true, countQuery=searchInfoCount)
    Page<ResultCntQueryDtoInterface> getUserSearchInfoList(String dataStatCd, String searchValue, String keyword, Integer userUno, Pageable pageable);


    @Query(value = "SELECT TU.USER_UNO as userUno, TU.USER_ID as userId FROM TB_USER TU", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByUserUno();

    @Query(value = "SELECT TSI.TSI_UNO as tsiUno, TU.USER_ID as userId FROM TB_SEARCH_INFO TSI LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByTsiUno();
    @Query("select concat(TSI.tsiImgPath, TSI.tsiImgName) from SearchInfoEntity TSI where TSI.tsiUno = :tsiUno")
    String getSearchInfoImgUrl(Integer tsiUno);

    @Query(value = "SELECT TSI.TSI_TYPE FROM TB_SEARCH_INFO TSI WHERE TSI.TSI_UNO = :tsiUno", nativeQuery = true)
    String getSearchInfoTsiType(Integer tsiUno);

    SearchInfoEntity findByTsiUno(Integer tsiUno);

    List<SearchInfoEntity> findByTsiUnoIn(List<Integer> tsiUnos);

    List<SearchInfoEntity> findByTsrUnoIn(List<Integer> tsrUnos);

    Optional<SearchInfoEntity> findByTsrUno(Integer tsrUno);

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
}
