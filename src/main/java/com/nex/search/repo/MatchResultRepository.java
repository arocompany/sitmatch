package com.nex.search.repo;

import com.nex.search.entity.MatchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MatchResultRepository extends JpaRepository<MatchResultEntity, Long> {
    @Query("select tmrSimilarity from MatchResultEntity where tsrUno = :tsrUno")
    String getTmrSimilarity(int tsrUno);

    // String getTmrSimilarityList = "select TSR_UNO as tsrUno, ceiling(((case when isnull(TMR_V_SCORE) then 0 else TMR_V_SCORE end + case when isnull(TMR_A_SCORE) then 0 else TMR_A_SCORE end + case when isnull(TMR_T_SCORE) then 0 else TMR_T_SCORE end) / (case when isnull(TMR_V_SCORE) then 0 else 1 end + case when isnull(TMR_A_SCORE) then 0 else 1 end + case when isnull(TMR_T_SCORE) then 0 else 1 end))*100) as tmrSimilarity from tb_match_result";

    /*
    @Query(value = getTmrSimilarityList, nativeQuery = true)
    List<TmrSimilarityDtoInterface> getTmrSimilarityList();

    List<MatchResultEntity> findByTsrUnoIn(List<Integer> tsrUnoValues);

    interface TmrSimilarityDtoInterface {
        Integer getTsrUno();
        Integer getTmrSimilarity();
    }
    */

    String countSimilarity = "SELECT COUNT(*) " +
            " FROM tb_search_result tsr_2 " +
            " INNER JOIN ( SELECT MIN(tsr_uno) tsr_uno FROM tb_search_result WHERE tsi_uno = :tsiUno GROUP BY tsr_site_url ) tsr2 ON tsr_2.tsr_uno = tsr2.tsr_uno " +
            " INNER JOIN tb_search_job tsj ON tsr_2.TSI_UNO = tsj.tsi_uno AND tsr_2.tsr_uno = tsj.tsr_uno " +
            " LEFT OUTER JOIN tb_match_result tmr ON tsr_2.TSR_UNO = tmr.tsr_uno " +
            " WHERE tmr.tsi_uno = :tsiUno  " +
            " AND tsr_2.tsi_uno = :tsiUno  " +
            " AND IF( " +
            " tmr.TMR_V_SCORE + tmr.TMR_A_SCORE + tmr.TMR_T_SCORE = 0, " +
            " '0', CEILING( " +
            " ( " +
            " (CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE TMR_V_SCORE END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE TMR_A_SCORE END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE TMR_T_SCORE END " +
            " ) / (CASE WHEN ISNULL(tmr.TMR_V_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_A_SCORE) THEN 0 ELSE 1 END + CASE WHEN ISNULL(tmr.TMR_T_SCORE) THEN 0 ELSE 1 END " +
            " ) " +
            " ) * 100 " +
            " ) " +
            " ) > 1 ";
    @Query(value = countSimilarity, nativeQuery = true)
    Integer countSimilarity(Integer tsiUno);

    String countChild = "SELECT COUNT(*) " +
            " FROM tb_search_result tsr_2 " +
            " INNER JOIN ( " +
            " SELECT MIN(tsr_uno) tsr_uno FROM tb_search_result WHERE tsi_uno = :tsiUno GROUP BY tsr_site_url " +
            " ) tsr2 ON tsr_2.tsr_uno = tsr2.tsr_uno " +
            " INNER JOIN tb_search_job tsj ON tsr_2.TSI_UNO = tsj.tsi_uno AND tsr_2.tsr_uno = tsj.tsr_uno " +
            " LEFT OUTER JOIN tb_match_result tmr ON tsr_2.TSR_UNO = tmr.tsr_uno " +
            " WHERE tmr.tsi_uno = :tsiUno AND tsr_2.tsi_uno = :tsiUno AND tmr.TMR_TOTAL_SCORE > 0";
    @Query(value = countChild, nativeQuery = true)
    Integer countChild(Integer tsiUno);
    
    String selectByTsiUnoAndTsrUno = "SELECT " +
            " tsi_uno, " +
            " tsr_uno, " +
            " tsj_uno, " +
            " MAX(tmr_v_score) tmr_v_score, " +
            " MAX(tmr_a_score) tmr_a_score, " +
            " MAX(tmr_t_score) tmr_t_score, " +
            " MAX(tmr_total_score) tmr_total_score, " +
            " MAX(tmr_age_score) tmr_age_score, " +
            " MAX(tmr_object_score) tmr_object_score, " +
            " MAX(tmr_ocw_score) tmr_ocw_score, " +
            " MAX(tmr_similarity) tmr_similarity, " +
            " MAX(tmr_age) tmr_age, " +
            " MAX(tmr_cnt_object) tmr_cnt_object, " +
            " MAX(tmr_cnt_text) tmr_cnt_text, " +
            " MAX(tmr_stat) tmr_stat, " +
            " NULL fst_dml_dt, " +
            " NULL lst_dml_dt, " +
            " NULL tmr_message, " +
            " 0 tmr_uno " +
            " FROM TB_MATCH_RESULT " +
            " WHERE tsi_uno = :tsiUno " +
            " AND tsr_uno = :tsrUno " +
            " GROUP BY tsi_uno, tsr_uno, tsj_uno";
    @Query(value = selectByTsiUnoAndTsrUno, nativeQuery = true)
    MatchResultEntity selectByTsiUnoAndTsrUno(Integer tsiUno, Integer tsrUno);

}
