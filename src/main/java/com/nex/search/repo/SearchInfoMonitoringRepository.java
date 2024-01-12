package com.nex.search.repo;

import com.nex.Chart.dto.DateKeywordExcelDto;
import com.nex.Chart.dto.KeywordCntDto;
import com.nex.Chart.dto.userKeywordCntDto;
import com.nex.search.entity.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface SearchInfoMonitoringRepository extends JpaRepository<SearchInfoMonitoringHistoryEntity, Integer> {

}