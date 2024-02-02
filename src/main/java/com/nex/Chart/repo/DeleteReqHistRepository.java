package com.nex.Chart.repo;


import com.nex.Chart.dto.DeleteReqHistDto;
import com.nex.Chart.dto.DeleteReqHistExcelDto;
import com.nex.Chart.entity.DeleteReqHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeleteReqHistRepository extends JpaRepository<DeleteReqHistEntity, Long> {
    String deleteReqHistList= " SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS deleteRequestDate, " +
                              " COUNT(*) AS deleteRequestCnt " +
                              " FROM TB_DELETE_REQ_HISTORY " +
                              " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate " +
                              " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') ";

    String userDeleteReqHistList = " SELECT USER_ID AS userId, " +
                                   " COUNT(*) AS  deleteRequestCnt " +
                                   " FROM tb_delete_req_history " +
                                   " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                   " GROUP BY USER_ID ";

    String userDeleteReqExcelList = " SELECT tu.user_nm AS userNm " +
                                    " ,tu.USER_ID AS userId " +
                                    " ,COUNT(*) AS  deleteRequestCnt " +
//                                    " ,tmh.CLK_DML_DT AS deleteRequestDate " +
                                    " FROM tb_delete_req_history tmh " +
                                    " LEFT OUTER JOIN tb_user tu " +
                                    " ON tu.user_uno = tmh.user_uno " +
                                    " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                    " GROUP BY tu.user_uno";
    @Query(value = deleteReqHistList, nativeQuery = true)
    List<DeleteReqHistDto> deleteReqHistList(String fromDate, String toDate);

    @Query(value = userDeleteReqHistList, nativeQuery = true)
    List<DeleteReqHistDto> userDeleteReqHistList(String toDate);

    @Query(value = userDeleteReqExcelList, nativeQuery = true)
    List<DeleteReqHistExcelDto> userDeleteReqExcelList(String toDate);


}
