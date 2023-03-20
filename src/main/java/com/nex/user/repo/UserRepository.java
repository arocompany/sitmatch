package com.nex.user.repo;

import com.nex.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // nativeQuery
    String countByEmail = "SELECT COUNT(*) FROM TB_USER WHERE EMAIL_ID = :emailId AND EMAIL_DOMAIN = :emailDomain";
    String countByPhoneNum = "SELECT COUNT(*) FROM TB_USER WHERE PHONE_NUM_1 = :phoneNum1 AND PHONE_NUM_2 = :phoneNum2 AND PHONE_NUM_3 = :phoneNum3";
    String findAllByEntire = "SELECT * FROM TB_USER WHERE USER_CLF_CD != :userClfCd AND USE_YN = :useYn AND (USER_ID LIKE CONCAT('%',:keyword,'%') OR USER_NM LIKE CONCAT('%',:keyword,'%')) ORDER BY USER_UNO DESC";

    int countByUserId(String userId);    // 중복 아이디 체크
    @Query(value = countByEmail, nativeQuery = true)
    int countByEmail(@Param("emailId") String emailId, @Param("emailDomain") String emailDomain);    // 중복 이메일 체크
    @Query(value = countByPhoneNum, nativeQuery = true)
    int countByPhoneNum(@Param("phoneNum1") String phoneNum1, @Param("phoneNum2") String phoneNum2, @Param("phoneNum3") String phoneNum3);    // 중복 핸드폰 번호 체크
    UserEntity findByUserId(String userId);
    UserEntity findByUserUno(Long userUno);

    // 상담사 관리 사용
    Page<UserEntity> findAllByUserClfCdNotAndUseYnOrderByUserUnoDesc(String userClfCd, String useYn, Pageable pageable);
    Page<UserEntity> findAllByUserClfCdNotAndUseYnAndUserIdContainingOrderByUserUnoDesc(String userClfCd, String useYn, String keyword, Pageable pageable);
    Page<UserEntity> findAllByUserClfCdNotAndUseYnAndUserNmContainingOrderByUserUnoDesc(String userClfCd, String useYn, String keyword, Pageable pageable);
    @Query(value = findAllByEntire, nativeQuery = true)
    Page<UserEntity> findAllByEntire(@Param("userClfCd") String userClfCd, @Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

}