package com.nex.search.repo;

import com.nex.search.entity.SearchResultMonitoringHistoryEntity;
import com.nex.search.entity.dto.SearchResultMonitoringHistoryDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface SearchResultMonitoringRepository extends JpaRepository<SearchResultMonitoringHistoryEntity, Integer> {
    String searchResultMonitoringHistoryList =  " SELECT tsrMh_create_date tsrmhCreateDate" +
                                                " FROM tb_search_result_monitoring_history " +
                                                " WHERE tsr_uno = :tsrUno" +
                                                " ORDER BY tsrMh_create_date DESC";
    @Query(value = searchResultMonitoringHistoryList, nativeQuery = true)
    List<SearchResultMonitoringHistoryDto> searchResultMonitoringHistoryList(int tsrUno);
}