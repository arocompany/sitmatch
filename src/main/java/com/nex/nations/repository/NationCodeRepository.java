package com.nex.nations.repository;

import com.nex.nations.entity.NationCodeEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface NationCodeRepository extends JpaRepository<NationCodeEntity, Integer> {
//    @Query(value = "SELECT nc_uno AS ncUno, nc_name AS ncName, nc_code AS ncCode, nc_is_active AS ncIsActive FROM tb_nation_code WHERE nc_is_active = :ncIsActive ", nativeQuery = true)
    List<NationCodeEntity> findByNcIsActive(int ncIsActive);
    List<NationCodeEntity> findAll();

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_nation_code SET nc_is_active = :ncIsActive WHERE nc_uno = :ncUno", nativeQuery = true)
    int nationUpdate(int ncUno, int ncIsActive);    // 자동추적 키워드 목록

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_nation_code SET nc_is_active = :ncIsActive", nativeQuery = true)
    int nationAllUpdate(int ncIsActive);    // 자동추적 키워드 목록

}
