package com.nex.search.repo;

import com.nex.search.entity.SearchInfoParamsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SearchInfoParamsRepository extends JpaRepository<SearchInfoParamsEntity, Integer> {
    SearchInfoParamsEntity findByTsiUno(int tsiUno);
}