package com.nex.nations.repository;

import com.nex.nations.entity.NationCodeEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface NationCodeRepository extends JpaRepository<NationCodeEntity, Integer> {

//    String findBySearch = " SELECT " +
//            " nc_uno AS ncUno, " +
//            " nc_name AS ncName, " +
//            " nc_code AS ncCode, " +
//            " nc_is_active AS ncIsActive " +
//            " FROM tb_nation_code " +
//            " WHERE nc_is_active  = :isActive ";

//    @Query(value = "SELECT nc_uno AS ncUno, nc_name AS ncName, nc_code AS ncCode, nc_is_active AS ncIsActive FROM tb_nation_code WHERE nc_is_active = :ncIsActive ", nativeQuery = true)
    List<NationCodeEntity> findByNcIsActive(int ncIsActive);
    List<NationCodeEntity> findAll();
}
