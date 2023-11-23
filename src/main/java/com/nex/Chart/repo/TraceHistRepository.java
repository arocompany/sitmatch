package com.nex.Chart.repo;

import com.nex.Chart.dto.TraceHistDto;
import com.nex.Chart.entity.TraceHistEntity;
import com.nex.Chart.dto.TraceHistExcelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceHistRepository extends JpaRepository<TraceHistEntity, Long> {
    String countByClk = "SELECT COUNT(*) FROM TB_TRACE_HISTORY";
    String countByClkTrace = "SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS traceDate," +
                             " COUNT(*) AS traceCnt " +
                             " FROM tb_trace_history " +
                             " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate2 " +
                             " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') " +
                             " ORDER BY traceDate ";

    String traceExcelList = "SELECT TTH.USER_ID AS userId, USER.USER_NM AS userNm" +
                            ", DATE_FORMAT(TTH.CLK_DML_DT, '%Y%m%d') AS DATE " +
                            ", COUNT(*) AS cnt " +
                            " FROM TB_TRACE_HISTORY TTH " +
                            " INNER JOIN TB_USER USER " +
                            " ON TTH.USER_UNO = USER.USER_UNO " +
                            " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
                            " GROUP BY DATE_FORMAT(TTH.CLK_DML_DT,'%Y%m%d'), TTH.USER_ID, user.user_nm  " +
                            " ORDER BY DATE ";

    String userTraceHistList =  " SELECT USER_ID AS userId, " +
                                " COUNT(*) AS  traceCnt " +
                                " FROM tb_trace_history " +
                                " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                " GROUP BY userId ";

    @Query(value = countByClk, nativeQuery = true)
    int countByClk();

    @Query(value = countByClkTrace, nativeQuery = true)
    List<TraceHistDto> countByClkTrace(String fromDate, String toDate2);

    @Query(value = traceExcelList, nativeQuery = true)
    List<TraceHistExcelDto> traceExcelList(String fromDate, String toDate2);

    @Query(value = userTraceHistList, nativeQuery = true)
    List<TraceHistDto> userTraceHistList(String toDate);


}
