package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

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
    @Column(name = "TMR_IMG_PATH")
    private String tmrImgPath;
    @Basic
    @Column(name = "TMR_IMG_NAME")
    private String tmrImgName;
    @Basic
    @Column(name = "TMR_IMG_EXT")
    private String tmrImgExt;
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

    public int getTmrUno() {
        return tmrUno;
    }

    public void setTmrUno(int tmrUno) {
        this.tmrUno = tmrUno;
    }

    public int getTsiUno() {
        return tsiUno;
    }

    public void setTsiUno(Integer tsiUno) {
        this.tsiUno = tsiUno;
    }

    public void setTsiUno(int tsiUno) {
        this.tsiUno = tsiUno;
    }

    public int getTsrUno() {
        return tsrUno;
    }

    public void setTsrUno(Integer tsrUno) {
        this.tsrUno = tsrUno;
    }

    public void setTsrUno(int tsrUno) {
        this.tsrUno = tsrUno;
    }

    public int getTsjUno() {
        return tsjUno;
    }

    public void setTsjUno(Integer tsjUno) {
        this.tsjUno = tsjUno;
    }

    public void setTsjUno(int tsjUno) {
        this.tsjUno = tsjUno;
    }

    public String getTmrImgPath() {
        return tmrImgPath;
    }

    public void setTmrImgPath(String tmrImgPath) {
        this.tmrImgPath = tmrImgPath;
    }

    public String getTmrImgName() {
        return tmrImgName;
    }

    public void setTmrImgName(String tmrImgName) {
        this.tmrImgName = tmrImgName;
    }

    public String getTmrImgExt() {
        return tmrImgExt;
    }

    public void setTmrImgExt(String tmrImgExt) {
        this.tmrImgExt = tmrImgExt;
    }

    public void setTmrSimilarity(Object tmrSimilarity) {
        this.tmrSimilarity = tmrSimilarity;
    }

    public void setTmrSimilarity(String tmrSimilarity) {
        this.tmrSimilarity = tmrSimilarity;
    }

    public String getTmrStat() {
        return tmrStat;
    }

    public void setTmrStat(String tmrStat) {
        this.tmrStat = tmrStat;
    }

    public String getTmrMessage() {
        return tmrMessage;
    }

    public void setTmrMessage(String tmrMessage) {
        this.tmrMessage = tmrMessage;
    }

    public Timestamp getFstDmlDt() {
        return fstDmlDt;
    }

    public void setFstDmlDt(Timestamp fstDmlDt) {
        this.fstDmlDt = fstDmlDt;
    }

    public Timestamp getLstDmlDt() {
        return lstDmlDt;
    }

    public void setLstDmlDt(Timestamp lstDmlDt) {
        this.lstDmlDt = lstDmlDt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchResultEntity that = (MatchResultEntity) o;
        return tmrUno == that.tmrUno && tsiUno == that.tsiUno && tsrUno == that.tsrUno && tsjUno == that.tsjUno && Objects.equals(tmrImgPath, that.tmrImgPath) && Objects.equals(tmrImgName, that.tmrImgName) && Objects.equals(tmrImgExt, that.tmrImgExt) && Objects.equals(tmrSimilarity, that.tmrSimilarity) && Objects.equals(tmrStat, that.tmrStat) && Objects.equals(tmrMessage, that.tmrMessage) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tmrUno, tsiUno, tsrUno, tsjUno, tmrImgPath, tmrImgName, tmrImgExt, tmrSimilarity, tmrStat, tmrMessage, fstDmlDt, lstDmlDt);
    }

    public Object getTmrVScore() {
        return tmrVScore;
    }

    public void setTmrVScore(Object tmrVScore) {
        this.tmrVScore = tmrVScore;
    }

    public Object getTmrAScore() {
        return tmrAScore;
    }

    public void setTmrAScore(Object tmrAScore) {
        this.tmrAScore = tmrAScore;
    }

    public Object getTmrTScore() {
        return tmrTScore;
    }

    public void setTmrTScore(Object tmrTScore) {
        this.tmrTScore = tmrTScore;
    }
}
