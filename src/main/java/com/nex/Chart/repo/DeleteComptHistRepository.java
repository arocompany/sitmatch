package com.nex.Chart.repo;

import com.nex.Chart.dto.DeleteComptHistDto;
import com.nex.Chart.dto.DeleteComptHistExcelDto;
import com.nex.Chart.entity.DeleteComptHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeleteComptHistRepository extends JpaRepository<DeleteComptHistEntity, Long> {
    String deleteComptHistList = " SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS deleteComptDate, " +
                                 " COUNT(*) AS deleteComptCnt " +
                                 " FROM TB_DELETE_COMPT_HISTORY " +
                                 " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate " +
                                 " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') " +
                                 " ORDER BY deleteComptDate ";

    String userDeleteComptHistList = " SELECT USER_ID AS userId, " +
                                     " COUNT(*) AS  deleteComptCnt " +
                                     " FROM tb_delete_compt_history " +
                                     " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                     " GROUP BY userId ";

    String userDeleteComptExcelList = " SELECT tu.user_nm AS userNm " +
                                      " ,tu.USER_ID AS userId " +
                                      " ,COUNT(*) AS  deleteComptCnt " +
                                      " ,tmh.CLK_DML_DT AS deleteComptDate " +
                                      " FROM tb_delete_compt_history tmh " +
                                      " LEFT OUTER JOIN tb_user tu " +
                                      " ON tu.user_uno = tmh.user_uno " +
                                      " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                      " GROUP BY userId ";
    @Query(value = deleteComptHistList,nativeQuery = true)
    List<DeleteComptHistDto> deleteComptHistList(String fromDate, String toDate);

    @Query(value = userDeleteComptHistList,nativeQuery = true)
    List<DeleteComptHistDto> userDeleteComptHistList(String toDate);
    @Query(value = userDeleteComptExcelList,nativeQuery = true)
    List<DeleteComptHistExcelDto> userDeleteComptExcelList(String toDate);

}
