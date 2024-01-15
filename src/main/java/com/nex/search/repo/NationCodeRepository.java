package com.nex.search.repo;

import com.nex.search.entity.NationCodeEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface NationCodeRepository extends JpaRepository<NationCodeEntity, Integer> {
}
