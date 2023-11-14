package com.nex.search.repo;

import com.nex.search.entity.MatchResultEntity;
import com.nex.search.entity.SearchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
    @Query("select tmrSimilarity from MatchResultEntity where tsrUno = :tsrUno")
    String getTmrSimilarity(int tsrUno);

    String getTmrSimilarityList = "select TSR_UNO as tsrUno, ceiling(((case when isnull(TMR_V_SCORE) then 0 else TMR_V_SCORE end + case when isnull(TMR_A_SCORE) then 0 else TMR_A_SCORE end + case when isnull(TMR_T_SCORE) then 0 else TMR_T_SCORE end) / (case when isnull(TMR_V_SCORE) then 0 else 1 end + case when isnull(TMR_A_SCORE) then 0 else 1 end + case when isnull(TMR_T_SCORE) then 0 else 1 end))*100) as tmrSimilarity from tb_match_result";
    @Query(value = getTmrSimilarityList, nativeQuery = true)
    List<TmrSimilarityDtoInterface> getTmrSimilarityList();

    List<MatchResultEntity> findByTsrUnoIn(List<Integer> tsrUnoValues);

    interface TmrSimilarityDtoInterface {
        Integer getTsrUno();
        Integer getTmrSimilarity();
    }
}
