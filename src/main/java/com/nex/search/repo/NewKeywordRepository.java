package com.nex.search.repo;

import com.nex.search.entity.NewKeywordEntity;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SearchKeywordDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewKeywordRepository extends JpaRepository<NewKeywordEntity, Long> {

    String keywordList = " SELECT IDX AS IDX " +
                         " ,KEYWORD AS KEYWORD " +
                         " FROM TB_NEW_KEYWORD " +
                         " WHERE KEYWORD_STUS = '0' ";

    String searchKeywordList = " SELECT KEYWORD AS KEYWORD " +
                               " FROM TB_NEW_KEYWORD " +
                               " WHERE KEYWORD_STUS = '0' ";

    @Query(value = keywordList, nativeQuery = true)
    List<NewKeywordDto> keywordList();
    @Query(value = searchKeywordList, nativeQuery = true)
    List<SearchKeywordDto> searchKeywordList();

    NewKeywordEntity findByIdx(Integer idx);
}


