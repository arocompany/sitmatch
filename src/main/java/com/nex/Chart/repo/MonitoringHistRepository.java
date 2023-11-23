package com.nex.Chart.repo;

import com.nex.Chart.dto.DeleteComptHistExcelDto;
import com.nex.Chart.dto.DeleteReqHistExcelDto;
import com.nex.Chart.dto.MonitoringHistDto;
import com.nex.Chart.dto.MonitoringHistExcelDto;
import com.nex.Chart.entity.MonitoringHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringHistRepository extends JpaRepository<MonitoringHistEntity, Long> {

    String dateMonitoringExcelList =" SELECT USER.USER_NM AS userNm " +
                                    " , tmh.USER_ID AS userId " +
                                    " , COUNT(*) AS monitoringCnt " +
                                    " , DATE_FORMAT(tmh.CLK_DML_DT, '%Y%m%d') AS monitoringDate " +
                                    " FROM tb_monitoring_history tmh " +
                                    " INNER JOIN TB_USER USER " +
                                    " ON tmh.USER_UNO = USER.USER_UNO " +
                                    " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate " +
                                    " GROUP BY DATE_FORMAT(tmh.CLK_DML_DT,'%Y%m%d'), tmh.USER_ID, user.user_nm " +
                                    " ORDER BY monitoringDate ";

    String dateMonitoringReqExcelList = " SELECT USER.USER_NM AS userNm " +
                                        " , tmh.USER_ID AS userId " +
                                        " , COUNT(*) AS deleteRequestCnt " +
                                        " , DATE_FORMAT(tmh.CLK_DML_DT, '%Y%m%d') AS deleteRequestDate " +
                                        " FROM tb_delete_req_history tmh " +
                                        " INNER JOIN TB_USER USER " +
                                        " ON tmh.USER_UNO = USER.USER_UNO " +
                                        " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate " +
                                        " GROUP BY DATE_FORMAT(tmh.CLK_DML_DT,'%Y%m%d'), tmh.USER_ID, user.user_nm " +
                                        " ORDER BY deleteRequestDate ";
    String dateMonitoringComptExcelList = " SELECT USER.USER_NM AS userNm " +
                                          " , tmh.USER_ID AS userId " +
                                          " , COUNT(*) AS deleteComptCnt " +
                                          " , DATE_FORMAT(tmh.CLK_DML_DT, '%Y%m%d') AS deleteComptDate " +
                                          " FROM tb_delete_compt_history tmh " +
                                          " INNER JOIN TB_USER USER " +
                                          " ON tmh.USER_UNO = USER.USER_UNO " +
                                          " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate " +
                                          " GROUP BY DATE_FORMAT(tmh.CLK_DML_DT,'%Y%m%d'), tmh.USER_ID, user.user_nm " +
                                          " ORDER BY deleteComptDate ";
    String monitoringHistList = " SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS monitoringDate, " +
                                " COUNT(*) AS monitoringCnt " +
                                " FROM TB_MONITORING_HISTORY " +
                                " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate " +
                                " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') " +
                                " ORDER BY monitoringDate ";
    String userMonitoringHistList = " SELECT USER_ID AS userId, " +
                                    " COUNT(*) AS  monitoringCnt " +
                                    " FROM TB_MONITORING_HISTORY " +
                                    " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                    " GROUP BY userId";

    String userMonitoringExcelList =" SELECT tu.user_nm AS userNm " +
                                    " ,tu.USER_ID AS userId " +
                                    " ,COUNT(*) AS  monitoringCnt " +
                                    " ,tmh.CLK_DML_DT AS clkDmlDt " +
                                    " FROM tb_monitoring_history tmh" +
                                    " LEFT OUTER JOIN tb_user tu " +
                                    " ON tu.user_uno = tmh.user_uno " +
                                    " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                    " GROUP BY userId ";


    @Query(value = monitoringHistList, nativeQuery = true)
    List<MonitoringHistDto> monitoringHistList(String fromDate, String toDate);
    @Query(value = userMonitoringHistList, nativeQuery = true)
    List<MonitoringHistDto> userMonitoringHistList(String toDate);

    @Query(value = userMonitoringExcelList, nativeQuery = true)
    List<MonitoringHistExcelDto> userMonitoringExcelList(String toDate);
    @Query(value = dateMonitoringExcelList, nativeQuery = true)
    List<MonitoringHistExcelDto> dateMonitoringExcelList(String fromDate, String toDate);

    @Query(value = dateMonitoringReqExcelList, nativeQuery = true)
    List<DeleteReqHistExcelDto> dateMonitoringReqExcelList(String fromDate, String toDate);

    @Query(value = dateMonitoringComptExcelList, nativeQuery = true)
    List<DeleteComptHistExcelDto> dateMonitoringComptExcelList(String fromDate, String toDate);
}
