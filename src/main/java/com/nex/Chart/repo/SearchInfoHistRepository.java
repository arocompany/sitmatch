package com.nex.Chart.repo;

import com.nex.Chart.dto.SearchInfoExcelDto;
import com.nex.Chart.dto.SearchInfoHistDto;
import com.nex.Chart.entity.SearchInfoHistEntity;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchInfoHistRepository extends JpaRepository<SearchInfoHistEntity, Long> {

    String userKeywordCntExcelList= " /* 사용자별 키워드 현황 */" +
                                    " SELECT tu.USER_NM as userNm ,user_id AS userId, COUNT(tsi.tsi_keyword) AS keywordCnt " +
                                    " ,SUM((SELECT COUNT(DISTINCT tsr.tsr_site_url) " +
                                    " from tb_search_result tsr LEFT OUTER JOIN tb_search_info tsi2 " +
                                    " ON tsr.tsi_uno = tsi2.tsi_uno " +
                                    " WHERE tsr.tsi_uno = tsi.tsi_uno)) AS cnt " +
                                    " FROM tb_search_info tsi " +
                                    " LEFT OUTER JOIN tb_user tu " +
                                    " ON tsi.user_uno = tu.user_uno " +
                                    " WHERE tsi.fst_dml_dt BETWEEN :fromDate AND :toDate " +
                                    " GROUP BY tu.user_uno";

    String countByClkSearchInfo = "SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS infoHistDATE " +
                                " ,COUNT(*) AS infoHistcnt " +
                                " FROM tb_search_info_history " +
                                " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate2 " +
                                " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') " +
                                " ORDER BY infoHistDATE ";

    String searchInfoExcelList = "SELECT SIH.USER_ID AS userId, USER.USER_NM AS userNm" +
                                ", DATE_FORMAT(SIH.CLK_DML_DT, '%Y%m%d') AS DATE " +
                                ", COUNT(*) AS cnt " +
                                " FROM TB_SEARCH_INFO_HISTORY SIH " +
                                " INNER JOIN TB_USER USER " +
                                " ON SIH.USER_UNO = USER.USER_UNO " +
                                " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
                                " GROUP BY DATE_FORMAT(SIH.CLK_DML_DT,'%Y%m%d'), SIH.USER_ID, user.user_nm  " +
                                " ORDER BY DATE ";
    String searchHistoryExcelList = " SELECT TSI.tsi_uno AS tsiUno, " +
                                    " USER.USER_ID AS userId, " +
                                    " CASE " +
                                    " WHEN TSI.TSI_TYPE = '11' " +
                                    " THEN '키워드' " +
                                    " WHEN TSI.TSI_TYPE = '13' " +
                                    " THEN '키워드+이미지' " +
                                    " WHEN TSI.TSI_TYPE = '15' " +
                                    " THEN '키워드+영상' " +
                                    " WHEN TSI.TSI_TYPE = '17' " +
                                    " THEN '이미지' " +
                                    " ELSE '영상' " +
                                    " END AS tsiType, " +
                                    " TSI.tsi_keyword AS keyword, " +
                                    " TSI.tsi_user_file AS userFile, " +
                                    " TSI.fst_dml_dt AS fstDmlDt " +
                                    " FROM " +
                                    " TB_SEARCH_INFO TSI " +
                                    " LEFT OUTER JOIN " +
                                    " TB_USER USER " +
                                    " ON TSI.USER_UNO = USER.USER_UNO " +
                                    " WHERE tsi.DATA_STAT_CD = '10' " +
                                    " AND tsi.SEARCH_VALUE = '0' " +
                                    " AND tsi.TSR_UNO IS NULL " +
                                    " and (:searchType = '0' AND tsi.tsi_search_type IN (1,2) OR (:searchType = '1' and tsi.tsi_search_type = 1) OR (:searchType ='2' and tsi.tsi_search_type=2)) " +
                                    " and ((:manageType = '사례번호' and tsi.TSI_USER_FILE LIKE CONCAT('%',:keyword,'%') OR (:keyword = '' AND tsi.tsi_user_file IS NULL )) OR :manageType != '사례번호' )  " +
                                    " and ((:manageType = '검색어' and tsi.TSI_KEYWORD like '%' :keyword '%' ) OR :manageType != '검색어')" +
                                    " ORDER BY TSI.tsi_uno DESC ";
    String resultExcelList =" SELECT TSI.TSI_UNO as tsiUno, " +
                            " TSR.TSR_UNO as tsrUno, " +
                            " tsr.TSR_TITLE as tsrTitle, " +
                            " CASE WHEN TSR_SNS='11' " +
                            " THEN '구글' " +
                            " WHEN TSR_SNS='13' " +
                            " THEN '트위터' " +
                            " WHEN TSR_SNS='15' " +
                            " THEN '인스타그램' " +
                            " ELSE '페이스북' " +
                            "  END AS tsrSns, " +
                            "  tsr.TSR_SITE_URL as tsrSiteUrl, " +
                            "  tsi.TSI_KEYWORD as tsiKeyword, " +
                            "   if(tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, '0', ceiling(((case " +
                            " when isnull(tmr.TMR_V_SCORE) then 0  " +
                            " else tmr.TMR_V_SCORE  " +
                            "  end + case  " +
                            " when isnull(tmr.TMR_A_SCORE) then 0  " +
                            " else tmr.TMR_A_SCORE  " +
                            "  end + case " +
                            "            when isnull(tmr.TMR_T_SCORE) then 0 " +
                            "            else tmr.TMR_T_SCORE " +
                            "        end) / (case " +
                            "            when isnull(tmr.TMR_V_SCORE) then 0 " +
                            "            else 1 " +
                            "        end + case " +
                            "            when isnull(tmr.TMR_A_SCORE) then 0 " +
                            "            else 1 " +
                            "        end + case " +
                            "            when isnull(tmr.TMR_T_SCORE) then 0 " +
                            "            else 1 " +
                            "        end)) * 100)) as tmrSimilarity, " +
                            "                tu.USER_ID as userId " +
                            "    FROM TB_SEARCH_RESULT TSR " +
                            "    INNER JOIN (SELECT MIN(tsr_uno) tsr_uno from tb_search_result WHERE (:tsiUno is null or tsi_uno = :tsiUno) GROUP BY tsr_site_url) tsr2 ON tsr.tsr_uno = tsr2.tsr_uno " +
                            "    INNER JOIN " +
                            "        TB_SEARCH_INFO TSI " +
                            "            ON TSR.TSI_UNO = TSI.TSI_UNO " +
                            "    LEFT OUTER JOIN " +
                            "        TB_SEARCH_JOB TSJ " +
                            "            ON TSR.TSR_UNO = TSJ.TSR_UNO " +
                            "    LEFT OUTER JOIN " +
                            "        TB_MATCH_RESULT TMR " +
                            "            ON TSR.TSR_UNO = TMR.TSR_UNO " +
                            "    LEFT OUTER JOIN " +
                            "        TB_USER TU " +
                            "            ON TSI.USER_UNO = TU.USER_UNO " +
                            "    WHERE " +
                            "        TSI.TSI_UNO = :tsiUno " +
                            " AND (TSR.TSR_TITLE LIKE CONCAT('%',:keyword,'%') or (:keyword = '' and TSR.TSR_TITLE is null) OR TSR.TSR_SITE_URL LIKE CONCAT('%',:keyword, '%')) " +
                            "        AND ( " +
                            "            tsj.TSJ_STATUS = '0' " +
                            "            OR tsj.TSJ_STATUS = '1' " +
                            "            OR tsj.TSJ_STATUS = '10' " +
                            "            OR tsj.TSJ_STATUS = '11' " +
                            "        ) " +
                            "        AND ( " +
                            "            tsr.TSR_SNS = '11' " +
                            "            OR tsr.TSR_SNS = '13' " +
                            "            OR tsr.TSR_SNS = '15' " +
                            "            OR tsr.TSR_SNS = '17' " +
                            "        ) " +
                            "    ORDER BY tsrUno desc ";

    String userSearchInfoHistList = " SELECT USER_ID AS userId, " +
                                    " COUNT(*) AS  infoHistCnt " +
                                    " FROM tb_search_info_history " +
                                    " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                    " GROUP BY userId ";

    @Query(value = countByClkSearchInfo, nativeQuery = true)
    List<SearchInfoHistDto> countByClkSearchInfo(String fromDate, String toDate2);

    @Query(value = searchInfoExcelList, nativeQuery = true)
    List<SearchInfoExcelDto> searchInfoExcelList (String fromDate, String toDate2);

    @Query(value = searchHistoryExcelList, nativeQuery = true)
    List<SearchHistoryExcelDto> searchHistoryExcelList(String searchType, String manageType, String keyword);

    @Query(value = resultExcelList, nativeQuery = true)
    List<ResultListExcelDto> resultExcelList(String tsiUno, String keyword);

    @Query(value = userSearchInfoHistList, nativeQuery = true)
    List<SearchInfoHistDto> userSearchInfoHistList(String toDate);

    @Query(value = userKeywordCntExcelList, nativeQuery = true)
    List<SearchInfoExcelDto> userKeywordCntExcelList(String fromDate, String toDate);


}
