package com.nex.Chart.repo;


import com.nex.Chart.dto.SearchResultExcelDto;
import com.nex.Chart.dto.SearchResultHistDto;
import com.nex.Chart.entity.SearchResultHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchResultHistRepository extends JpaRepository<SearchResultHistEntity, Long> {

    String countByClkSearchResult = " SELECT DATE_FORMAT(CLK_DML_DT,'%Y%m%d') AS rsltDate, COUNT(*) AS rsltCnt " +
            " FROM TB_SEARCH_RESULT_HISTORY " +
            " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
            " GROUP BY DATE_FORMAT(CLK_DML_DT,'%Y%m%d') " +
            " ORDER BY rsltDate ";

    String searchResultExcelList = "SELECT SRH.USER_ID AS userId, USER.USER_NM AS userNm" +
                                    ", DATE_FORMAT(SRH.CLK_DML_DT, '%Y%m%d') AS DATE " +
                                    ", COUNT(*) AS cnt " +
                                    " FROM TB_SEARCH_RESULT_HISTORY SRH " +
                                    " INNER JOIN TB_USER USER " +
                                    " ON SRH.USER_UNO = USER.USER_UNO " +
                                    " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
                                    " GROUP BY DATE_FORMAT(SRH.CLK_DML_DT,'%Y%m%d'), SRH.USER_ID, user.user_nm  " +
                                    " ORDER BY DATE ";

    String searchResultHistList = " SELECT USER_ID AS userId, " +
                                  " COUNT(*) AS  rsltCnt " +
                                  " FROM tb_search_result_history " +
                                  " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                  " GROUP BY USER_ID ";

    @Query(value = countByClkSearchResult, nativeQuery = true)
    List<SearchResultHistDto> countByClkSearchResult(String fromDate, String toDate2);

    @Query(value = searchResultExcelList, nativeQuery = true)
    List<SearchResultExcelDto> searchResultExcelList(String fromDate, String toDate2);
    @Query(value = searchResultHistList, nativeQuery = true)
    List<SearchResultHistDto> searchResultHistList(String toDate);

}
