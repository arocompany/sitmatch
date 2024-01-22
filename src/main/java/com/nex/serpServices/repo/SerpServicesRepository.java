package com.nex.serpServices.repo;

import com.nex.serpServices.entity.SerpServicesEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SerpServicesRepository extends JpaRepository <SerpServicesEntity, Integer> {
    List<SerpServicesEntity> findAll(Sort sort);
}
