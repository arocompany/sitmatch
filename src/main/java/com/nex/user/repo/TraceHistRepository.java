package com.nex.user.repo;

import com.nex.user.entity.SearchInfoHistDto;
import com.nex.user.entity.TraceHistDto;
import com.nex.user.entity.TraceHistEntity;
import com.nex.user.entity.TraceHistExcelDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

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

    @Query(value = countByClk, nativeQuery = true)
    int countByClk();

    @Query(value = countByClkTrace, nativeQuery = true)
    List<TraceHistDto> countByClkTrace(String fromDate, String toDate2);

    @Query(value = traceExcelList, nativeQuery = true)
    List<TraceHistExcelDto> traceExcelList(String fromDate, String toDate2);



}
