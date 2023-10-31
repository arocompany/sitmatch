package com.nex.search.repo;

import com.nex.search.entity.DefaultQueryDtoInterface;
import com.nex.search.entity.NewKeywordEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.user.entity.NewKeywordDto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewKeywordRepository extends JpaRepository<NewKeywordEntity, Long> {

    String keywordList = " SELECT IDX AS IDX " +
                         " ,KEYWORD AS KEYWORD " +
                         " FROM TB_NEW_KEYWORD " +
                         " WHERE KEYWORD_STUS = '0' ";

    @Query(value = keywordList, nativeQuery = true)
    List<NewKeywordDto> keywordList();

    NewKeywordEntity findByIdx(Integer idx);
}


