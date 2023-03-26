package com.nex.search.repo;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.UserIdDtoInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchInfoRepository extends JpaRepository<SearchInfoEntity, Integer> {
    List<SearchInfoEntity> findAllByOrderByTsiUnoDesc();
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Pageable pageable);
    Page<SearchInfoEntity> findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc(String dataStatCd, String keyword, Integer userUno, Pageable pageable);

    @Query(value = "SELECT TU.USER_UNO as userUno, TU.USER_ID as userId FROM TB_USER TU", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByUserUno();

    @Query(value = "SELECT TSI.TSI_UNO as tsiUno, TU.USER_ID as userId FROM TB_SEARCH_INFO TSI LEFT OUTER JOIN TB_USER TU ON TSI.USER_UNO = TU.USER_UNO", nativeQuery = true)
    List<UserIdDtoInterface> getUserIdByTsiUno();
    @Query("select concat(TSI.tsiImgPath, TSI.tsiImgName) from SearchInfoEntity TSI where TSI.tsiUno = :tsiUno")
    String getSearchInfoImgUrl(Integer tsiUno);

    @Query(value = "SELECT TSI.TSI_TYPE FROM TB_SEARCH_INFO TSI WHERE TSI.TSI_UNO = :tsiUno", nativeQuery = true)
    String getSearchInfoTsiType(Integer tsiUno);

    SearchInfoEntity findByTsiUno(Integer tsiUno);

    List<SearchInfoEntity> findByTsiUnoIn(List<Integer> tsiUnos);

    List<SearchInfoEntity> findByTsrUnoIn(List<Integer> tsrUnos);

    Optional<SearchInfoEntity> findByTsrUno(Integer tsrUno);
}
