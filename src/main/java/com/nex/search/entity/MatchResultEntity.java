package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;
@Data
@Entity
@Table(name = "tb_match_result", schema = "sittest", catalog = "")
public class MatchResultEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TMR_UNO", nullable = false)
    private int tmrUno;
    @Basic
    @Column(name = "TSI_UNO", nullable = true)
    private int tsiUno;
    @Basic
    @Column(name = "TSR_UNO", nullable = true)
    private int tsrUno;
    @Basic
    @Column(name = "TSJ_UNO", nullable = true)
    private int tsjUno;
    @Basic
    @Column(name = "TMR_SIMILARITY", nullable = true, precision = 0)
    private Object tmrSimilarity;
    @Basic
    @Column(name = "TMR_STAT", nullable = true, length = 2)
    private String tmrStat;
    @Basic
    @Column(name = "TMR_MESSAGE", nullable = true, length = 2000)
    private String tmrMessage;
    @Basic
    @Column(name = "FST_DML_DT", nullable = true)
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT", nullable = true)
    private Timestamp lstDmlDt;
    @Basic
    @Column(name = "TMR_V_SCORE", nullable = true, precision = 0)
    private Object tmrVScore;
    @Basic
    @Column(name = "TMR_A_SCORE", nullable = true, precision = 0)
    private Object tmrAScore;
    @Basic
    @Column(name = "TMR_T_SCORE", nullable = true, precision = 0)
    private Object tmrTScore;

    @Basic
    @Column(name = "TMR_TOTAL_SCORE", nullable = true, precision = 0)
    private Object tmrTotalScore;
    @Basic
    @Column(name = "TMR_AGE_SCORE", nullable = true, precision = 0)
    private Object tmrAgeScore;
    @Basic
    @Column(name = "TMR_OBJECT_SCORE", nullable = true, precision = 0)
    private Object tmrObjectScore;
    @Basic
    @Column(name = "TMR_OCW_SCORE", nullable = true, precision = 0)
    private Object tmrOcwScore;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchResultEntity that = (MatchResultEntity) o;
        return tmrUno == that.tmrUno && tsiUno == that.tsiUno && tsrUno == that.tsrUno && tsjUno == that.tsjUno && Objects.equals(tmrSimilarity, that.tmrSimilarity) && Objects.equals(tmrStat, that.tmrStat) && Objects.equals(tmrMessage, that.tmrMessage) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tmrUno, tsiUno, tsrUno, tsjUno, tmrSimilarity, tmrStat, tmrMessage, fstDmlDt, lstDmlDt);
    }
}
