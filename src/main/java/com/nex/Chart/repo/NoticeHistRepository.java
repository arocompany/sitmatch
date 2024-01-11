package com.nex.Chart.repo;

import com.nex.Chart.dto.NoticeHistDto;
import com.nex.Chart.dto.NoticeListExcelDto;
import com.nex.Chart.entity.NoticeHistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeHistRepository extends JpaRepository<NoticeHistEntity, Long> {
    String countByClkNotice = " SELECT DATE_FORMAT(CLK_DML_DT,'%Y%m%d') AS noticeDate, COUNT(*) AS noticeCnt " +
            " FROM TB_NOTICE_HISTORY " +
            " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
            " GROUP BY DATE_FORMAT(CLK_DML_DT,'%Y%m%d') " +
            " ORDER BY noticeDate ";

    String noticeExcelList = "SELECT TNH.USER_ID AS userId, USER.USER_NM AS userNm" +
            ", DATE_FORMAT(TNH.CLK_DML_DT, '%Y%m%d') AS DATE " +
            ", COUNT(*) AS cnt " +
            " FROM TB_NOTICE_HISTORY TNH " +
            " INNER JOIN TB_USER USER " +
            " ON TNH.USER_UNO = USER.USER_UNO " +
            " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
            " GROUP BY DATE_FORMAT(TNH.CLK_DML_DT,'%Y%m%d'), TNH.USER_ID, user.user_nm  " +
            " ORDER BY DATE ";

    String userNoticeHistList = " SELECT USER_ID AS userId, " +
                                " COUNT(*) AS  noticeCnt " +
                                " FROM tb_notice_history " +
                                " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                                " GROUP BY userId ";

    @Query(value = countByClkNotice, nativeQuery = true)
    List<NoticeHistDto> countByClkNotice(String fromDate, String toDate2);

    @Query(value = noticeExcelList, nativeQuery = true)
    List<NoticeListExcelDto> noticeExcelList(String fromDate, String toDate2);

    @Query(value = userNoticeHistList, nativeQuery = true)
    List<NoticeHistDto> userNoticeHistList(String toDate);


}
