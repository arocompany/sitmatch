package com.nex.search.repo;

import com.nex.search.entity.SearchUserFileEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface SearchUserFileRepository  extends JpaRepository<SearchUserFileEntity, Integer> {
    SearchUserFileEntity findByTsufUserFile(String tsufUserFile);
}
