package com.nex.user.repo;

import com.nex.Chart.dto.LoginExcelDto;
import com.nex.Chart.dto.LoginHistDto;
import com.nex.user.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    String countByEmail = "SELECT COUNT(*) FROM TB_USER WHERE EMAIL_ID = :emailId AND EMAIL_DOMAIN = :emailDomain";
    String countByPhoneNum = "SELECT COUNT(*) FROM TB_USER WHERE PHONE_NUM_1 = :phoneNum1 AND PHONE_NUM_2 = :phoneNum2 AND PHONE_NUM_3 = :phoneNum3";
    String findAllByEntire = "SELECT * FROM TB_USER WHERE USER_CLF_CD != :userClfCd AND USE_YN = :useYn AND (USER_ID LIKE CONCAT('%',:keyword,'%') OR USER_NM LIKE CONCAT('%',:keyword,'%')) ORDER BY USER_UNO DESC";
    String userHistCnt = " SELECT USER_ID AS userId, " +
                         " COUNT(*) AS  cnt " +
                         " FROM tb_login_history " +
                         " WHERE clk_dml_dt LIKE CONCAT(:toDate,'%') " +
                         " GROUP BY USER_ID ";

    String userHistCnt_2 = " SELECT DATE_FORMAT(clk_dml_dt,'%Y%m%d') AS DATE, " +
                           " COUNT(*) AS cnt " +
                           " FROM tb_login_history " +
                           " WHERE clk_dml_dt BETWEEN :fromDate AND :toDate2"+
                           " GROUP BY DATE_FORMAT(clk_dml_dt,'%Y%m%d') ";

    String userHistExcel =  " SELECT TLH.USER_ID AS userId, " +
                            " user.user_nm AS userNm, " +
                            " DATE_FORMAT(TLH.CLK_DML_DT,'%Y%m%d') AS date, " +
                            " COUNT(*) AS cnt " +
                            " FROM TB_LOGIN_HISTORY TLH " +
                            " INNER JOIN TB_USER USER " +
                            " ON TLH.USER_UNO = user.user_uno " +
                            " WHERE CLK_DML_DT BETWEEN :fromDate AND :toDate2 " +
                            " GROUP BY DATE_FORMAT(TLH.CLK_DML_DT,'%Y%m%d'), TLH.USER_ID, user.user_nm ";

    int countByUserId(String userId);
    @Query(value = countByEmail, nativeQuery = true)
    int countByEmail(@Param("emailId") String emailId, @Param("emailDomain") String emailDomain);
    @Query(value = countByPhoneNum, nativeQuery = true)
    int countByPhoneNum(@Param("phoneNum1") String phoneNum1, @Param("phoneNum2") String phoneNum2, @Param("phoneNum3") String phoneNum3);
    UserEntity findByUserId(String userId);
    UserEntity findByUserUno(Long userUno);
    @Query(value = userHistCnt_2, nativeQuery = true)
    List<LoginHistDto> userHistCntList(@Param("fromDate") String fromDate, @Param("toDate2") String toDate);

    @Query(value = userHistCnt, nativeQuery = true)
    List<LoginHistDto> userHistList(@Param("toDate") String toDate);

    @Query(value = userHistExcel, nativeQuery = true)
    List<LoginExcelDto> userHistExcel(String fromDate, String toDate2);

    // 상담사 관리 사용
    Page<UserEntity> findAllByUserClfCdNotAndUseYnOrderByUserUnoDesc(String userClfCd, String useYn, Pageable pageable);
    Page<UserEntity> findAllByUserClfCdNotAndUseYnAndUserIdContainingOrderByUserUnoDesc(String userClfCd, String useYn, String keyword, Pageable pageable);
    Page<UserEntity> findAllByUserClfCdNotAndUseYnAndUserNmContainingOrderByUserUnoDesc(String userClfCd, String useYn, String keyword, Pageable pageable);
    @Query(value = findAllByEntire, nativeQuery = true)
    Page<UserEntity> findAllByEntire(@Param("userClfCd") String userClfCd, @Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_user SET CRAWLING_LIMIT = :getCrawling_limit, PERCENT_LIMIT = :getPercent_limit WHERE USER_UNO = :tsrUno", nativeQuery = true)
    int ajax_con_limit_update(Long tsrUno, String getCrawling_limit, int getPercent_limit);


}