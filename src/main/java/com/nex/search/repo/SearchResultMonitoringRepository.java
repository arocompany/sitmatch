package com.nex.search.repo;

import com.nex.search.entity.SearchResultMonitoringHistoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SearchResultMonitoringRepository extends JpaRepository<SearchResultMonitoringHistoryEntity, Integer> {

}