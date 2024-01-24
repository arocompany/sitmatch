package com.nex.serpServices.repo;

import com.nex.serpServices.entity.SerpServicesEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SerpServicesRepository extends JpaRepository <SerpServicesEntity, Integer> {
    List<SerpServicesEntity> findAll();
    List<SerpServicesEntity> findBySsIsActive(int viewActive);

    @Transactional
    @Modifying
    @Query(value = "UPDATE tb_serp_services SET ss_is_active = :ssIsActive WHERE ss_uno = :ssUno", nativeQuery = true)
    int serviceCodeUpdate(int ssUno, int ssIsActive);
}
