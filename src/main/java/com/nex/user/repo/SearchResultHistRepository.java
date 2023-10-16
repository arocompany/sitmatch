package com.nex.user.repo;


import com.nex.user.entity.SearchResultExcelDto;
import com.nex.user.entity.SearchResultHistDto;
import com.nex.user.entity.SearchResultHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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

    @Query(value = countByClkSearchResult, nativeQuery = true)
    List<SearchResultHistDto> countByClkSearchResult(String fromDate, String toDate2);

    @Query(value = searchResultExcelList, nativeQuery = true)
    List<SearchResultExcelDto> searchResultExcelList(String fromDate, String toDate2);

}
