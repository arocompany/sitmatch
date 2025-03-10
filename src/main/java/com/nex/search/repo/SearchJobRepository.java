package com.nex.search.repo;

import com.nex.search.entity.SearchJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchJobRepository extends JpaRepository<SearchJobEntity, Long> {
    @Query("select CEILING(count(TMR.tsjUno)/count(TSJ.tsjUno)*100) from SearchJobEntity AS TSJ LEFT OUTER JOIN MatchResultEntity AS TMR ON TSJ.tsjUno = TMR.tsjUno WHERE TSJ.tsiUno = :tsiUno")
    Integer progressPercent(int tsiUno);

    int countByTsiUno(int tsiUno);

    interface ProgressPercentDtoInterface {
        Integer getTsiUno();
        Integer getProgressPercent();
    }

//    @Query("select TSJ.tsiUno AS tsiUno, CEILING(count(TMR.tsjUno)/count(TSJ.tsjUno)*100) AS progressPercent from SearchJobEntity AS TSJ LEFT OUTER JOIN MatchResultEntity AS TMR ON TSJ.tsjUno = TMR.tsjUno GROUP BY TSJ.tsiUno")
//    @Query("select TSJ.tsiUno AS tsiUno, CEILING((sum(case TSJ.tsjStatus when TSJ.tsjStatus is null then 0 when '01' then 0 when '10' then 0 else 1 end) / count(TSJ.tsjStatus))*100) AS progressPercent from SearchJobEntity AS TSJ GROUP BY TSJ.tsiUno")
//    @Query("select tsj.tsiUno as tsiUno, ceiling(sum(case tsj.tsjStatus when '11' then 1 when '10' then 1 else 0 end) / count(tsj.tsjStatus) * 100) as progressPercent from SearchJobEntity tsj where tsiUno in :tsiUnos group by tsj.tsiUno")
String progressPercentQuery = " SELECT tsj.tsi_Uno AS tsiUno, " +
        " IF((SELECT tsi_search_type FROM tb_search_info WHERE tsi_uno = tsj.tsi_uno) = 1 , (CEILING( SUM( CASE tsj.tsj_Status WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END ) / COUNT(tsj.tsj_Status) * 100 ) ), CEILING( SUM( CASE tsj.tsj_Status_child WHEN '11' THEN 1 WHEN '10' THEN 1 ELSE 0 END ) / COUNT(tsj.tsj_Status_child) * 100 ) ) AS progressPercent " +
        " FROM tb_Search_Job tsj " +
        " WHERE tsi_Uno IN :tsiUnos GROUP BY tsj.tsi_Uno ";
    @Query(value = progressPercentQuery, nativeQuery = true)
    List<ProgressPercentDtoInterface> progressPercentByAll(List<Integer> tsiUnos);

    SearchJobEntity findByTsrUno(Integer tsrUno);
}
