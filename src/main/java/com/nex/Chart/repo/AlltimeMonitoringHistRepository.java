package com.nex.Chart.repo;

import com.nex.Chart.dto.AllTimeCntDto;
import com.nex.Chart.dto.AllTimeCntExcelDto;
import com.nex.Chart.dto.AllTimeMonitoringHistDto;
import com.nex.Chart.entity.AlltimeMonitoringHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlltimeMonitoringHistRepository extends JpaRepository<AlltimeMonitoringHistEntity, Long> {
    String allTimeCntList = " SELECT COUNT(*) AS monitoringCnt, " +
                            " SUM((SELECT COUNT(tsi.tsi_uno) FROM tb_search_info tsi " +
                            " INNER join tb_search_result tsr2 " +
                            " ON tsi.tsr_uno = tsr2.tsr_uno " +
                            " LEFT OUTER JOIN tb_alltime_monitoring_history tam2 " +
                            " ON tsi.tsr_uno = tam2.tsr_uno " +
                            " WHERE tsi.tsr_uno = tam.tsr_uno)) AS resultCnt, user_id AS userId " +
                            " FROM tb_alltime_monitoring_history tam " +
                            " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                            " GROUP BY user_id ";

    String monitoringDateCntList =  " SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS monitoringDate, " +
                                    " COUNT(*) AS monitoringCnt, " +
                                    " SUM((SELECT COUNT(tsi.tsi_uno) FROM tb_search_info tsi " +
                                    " INNER join tb_search_result tsr2 " +
                                    " ON tsi.tsr_uno = tsr2.tsr_uno " +
                                    " LEFT OUTER JOIN tb_alltime_monitoring_history tam2 " +
                                    " ON tsi.tsr_uno = tam2.tsr_uno " +
                                    " WHERE tsi.tsr_uno = tam.tsr_uno " +
                                    " AND clk_dml_dt BETWEEN :fromDate AND :toDate)) AS resultCnt " +
                                    " FROM tb_alltime_monitoring_history tam " +
                                    " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate " +
                                    " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') " +
                                    " ORDER BY monitoringDate ";

    String userAllTimeCntExcelList =" SELECT tu.user_nm AS userName " +
                                    " , tu.user_id AS userId " +
                                    " , COUNT(*) AS monitoringCnt " +
                                    " ,SUM((SELECT COUNT(tsr2.tsr_site_url) FROM tb_search_result tsr2 " +
                                    " INNER join tb_search_info tsi2 " +
                                    " ON tsi2.tsr_uno = tsr2.tsr_uno" +
                                    " LEFT OUTER JOIN tb_alltime_monitoring_history tam2 " +
                                    " ON tsi2.tsr_uno = tam2.tsr_uno " +
                                    " WHERE tsi2.tsr_uno = tam.tsr_uno " +
                                    " AND tsi2.TSR_UNO IS NOT NULL )) AS resultCnt " +
                                    " FROM tb_alltime_monitoring_history tam " +
                                    " LEFT OUTER JOIN tb_user tu " +
                                    " ON tu.user_uno = tam.user_uno " +
                                    " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                    " GROUP BY tu.user_uno ";

    String dateAllTimeCntExcelList =" SELECT tu.user_nm AS userName " +
                                    " , tu.user_id AS userId " +
                                    " , COUNT(*) AS monitoringCnt " +
                                    " , SUM((SELECT COUNT(tsr2.tsr_site_url) FROM tb_search_result tsr2 " +
                                    " INNER join tb_search_info tsi2 " +
                                    " ON tsi2.tsr_uno = tsr2.tsr_uno " +
                                    " LEFT OUTER JOIN tb_alltime_monitoring_history tam2 " +
                                    " ON tsi2.tsr_uno = tam2.tsr_uno " +
                                    " WHERE tsi2.tsr_uno = tam.tsr_uno " +
                                    " AND tsi2.TSR_UNO IS NOT NULL )) AS monitoringResultCnt " +
                                    " , DATE_FORMAT(tam.clk_dml_dt,'%Y%m%d')  AS monitoringDate " +
                                    " FROM tb_alltime_monitoring_history tam " +
                                    " LEFT OUTER JOIN tb_user tu " +
                                    " ON tu.user_uno = tam.user_uno " +
                                    " WHERE clk_dml_dt  BETWEEN :fromDate AND :toDate " +
                                    " GROUP BY monitoringDate " +
                                    " ORDER BY monitoringDate ASC";

    String allTimeMonitoringList =  " SELECT TAM_UNO AS tamUno" +
                                    " ,clk_dml_dt AS clkDmlDt " +
                                    " ,tam_yn AS tamYn " +
                                    " FROM tb_alltime_monitoring_history " +
                                    " WHERE tsr_uno = :tsrUno " +
                                    " ORDER BY tamUno DESC ";

    @Query(value = allTimeCntList, nativeQuery = true)
    List<AllTimeCntDto> allTimeCntList(String toDate);

    @Query(value = monitoringDateCntList, nativeQuery = true)
    List<AllTimeCntDto> monitoringDateCntList(String fromDate, String toDate);
    @Query(value = userAllTimeCntExcelList, nativeQuery = true)
    List<AllTimeCntExcelDto> userAllTimeCntExcelList(String toDate);

    @Query(value = dateAllTimeCntExcelList, nativeQuery = true)
    List<AllTimeCntExcelDto> dateAllTimeCntExcelList(String fromDate, String toDate);

    @Query(value = allTimeMonitoringList, nativeQuery = true)
    List<AllTimeMonitoringHistDto> allTimeMonitoringList(String tsrUno);
}
