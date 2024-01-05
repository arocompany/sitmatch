package com.nex.user.repo;

import com.nex.user.entity.AutoEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutoRepository extends JpaRepository<AutoEntity, Long> {

    String insert_sql = "INSERT tb_auto_keyword SET AUTO_USER_ID = :auto_user_id, AUTO_KEYWORD = :auto_keyword, FST_DML_DT = now()";
    String delete_sql = "DELETE from tb_auto_keyword WHERE AUTO_KEYWORD = :auto_keyword AND AUTO_USER_ID = :auto_user_id";
    // String list_select_sql = "SELECT * FROM tb_auto_keyword WHERE AUTO_USER_ID = :auto_user_id ORDER BY FST_DML_DT";
    @Transactional
    @Modifying
    @Query(value = insert_sql, nativeQuery = true)
    int auto_Insert(@Param("auto_keyword") String auto_keyword, @Param("auto_user_id") String auto_user_id);    // 자동추적 키워드 등록

    @Transactional
    @Modifying
    @Query(value = delete_sql, nativeQuery = true)
    int auto_Delete(@Param("auto_keyword") String auto_keyword, @Param("auto_user_id") String auto_user_id);    // 자동추적 키워드 목록

    /*
    @Query(value = list_select_sql, nativeQuery = true)
    List<AutoKeywordInterface> auto_list_select(@Param("auto_user_id") String auto_user_id);    // 자동추적 키워드 등록
    */

}

