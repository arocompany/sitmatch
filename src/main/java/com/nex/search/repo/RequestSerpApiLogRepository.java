package com.nex.search.repo;

import com.nex.search.entity.RequestSerpApiLogEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface RequestSerpApiLogRepository extends JpaRepository<RequestSerpApiLogEntity, Integer> {
    RequestSerpApiLogEntity findById(int uno);
}