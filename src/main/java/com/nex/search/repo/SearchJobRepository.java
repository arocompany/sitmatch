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
    @Query("select tsj.tsiUno as tsiUno, ceiling(sum(case tsj.tsjStatus when '11' then 1 when '10' then 1 else 0 end) / count(tsj.tsjStatus) * 100) as progressPercent from SearchJobEntity tsj group by tsj.tsiUno")
    List<ProgressPercentDtoInterface> progressPercentByAll();

    SearchJobEntity findByTsrUno(Integer tsrUno);
}
