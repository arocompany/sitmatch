package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tb_search_info", schema = "sittest", catalog = "")
public class SearchInfoEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSI_UNO")
    private int tsiUno;
    @Basic
    @Column(name = "TSI_TYPE")
    private String tsiType;
    @Basic
    @Column(name = "TSI_GOOGLE")
    private byte tsiGoogle;
    @Basic
    @Column(name = "TSI_TWITTER")
    private byte tsiTwitter;
    @Basic
    @Column(name = "TSI_FACEBOOK")
    private byte tsiFacebook;
    @Basic
    @Column(name = "TSI_INSTAGRAM")
    private byte tsiInstagram;
    @Basic
    @Column(name = "TSI_KEYWORD")
    private String tsiKeyword;
    @Basic
    @Column(name = "TSI_IMG_PATH")
    private String tsiImgPath;
    @Basic
    @Column(name = "TSI_IMG_NAME")
    private String tsiImgName;
    @Basic
    @Column(name = "TSI_IMG_EXT")
    private String tsiImgExt;
    @Basic
    @Column(name = "TSI_STAT")
    private String tsiStat;
    @Basic
    @Column(name = "TSI_DNA_PATH")
    private String tsiDnaPath;
    @Basic
    @Column(name = "TSI_DNA_TEXT")
    private String tsiDnaText;
    @Basic
    @Column(name = "USER_UNO")
    private int userUno;
    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT")
    private Timestamp lstDmlDt;
    @Basic
    @Column(name = "TSI_ALLTIME_DT")
    private Timestamp tsiAlltimeDt;
    @Basic
    @Column(name = "TSI_IMG_REAL_PATH")
    private String tsiImgRealPath;
    @Basic
    @Column(name = "DATA_STAT_CD")
    private String dataStatCd;
    @Basic
    @Column(name = "TSI_IMG_HEIGHT")
    private String tsiImgHeight;
    @Basic
    @Column(name = "TSI_IMG_WIDTH")
    private String tsiImgWidth;
    @Basic
    @Column(name = "TSI_IMG_SIZE")
    private String tsiImgSize;

    @Basic
    @Column(name = "TSR_UNO")
    private Integer tsrUno;

    @Basic
    @Column(name = "SEARCH_VALUE")
    private String searchValue;

    @Basic
    @Column(name = "TSI_ALLTIME_MONITORING")
    private String tsiAlltimeMonitoring;
    @Basic
    @Column(name = "TSI_USER_FILE")
    private String tsiUserFile;

    @Basic
    @Column(name = "TSI_MONITORING_CNT")
    private Integer tsiMonitoringCnt;

    public int getTsiMonitoringCnt() {
        return tsiMonitoringCnt;
    }

    public void setTsiMonitoringCnt(int tsiMonitoringCnt) {
        this.tsiMonitoringCnt = tsiMonitoringCnt;
    }

    public int getTsiUno() {
        return tsiUno;
    }

    public void setTsiUno(int tsiUno) {
        this.tsiUno = tsiUno;
    }

    public String getTsiType() {
        return tsiType;
    }

    public void setTsiType(String tsiType) {
        this.tsiType = tsiType;
    }

    public byte getTsiGoogle() {
        return tsiGoogle;
    }

    public void setTsiGoogle(byte tsiGoogle) {
        this.tsiGoogle = tsiGoogle;
    }

    public byte getTsiTwitter() {
        return tsiTwitter;
    }

    public void setTsiTwitter(byte tsiTwitter) {
        this.tsiTwitter = tsiTwitter;
    }

    public byte getTsiFacebook() {
        return tsiFacebook;
    }

    public void setTsiFacebook(byte tsiFacebook) {
        this.tsiFacebook = tsiFacebook;
    }

    public byte getTsiInstagram() {
        return tsiInstagram;
    }

    public void setTsiInstagram(byte tsiInstagram) {
        this.tsiInstagram = tsiInstagram;
    }

    public String getTsiKeyword() {
        return tsiKeyword;
    }

    public void setTsiKeyword(String tsiKeyword) {
        this.tsiKeyword = tsiKeyword;
    }

    public String getTsiImgPath() {
        return tsiImgPath;
    }

    public void setTsiImgPath(String tsiImgPath) {
        this.tsiImgPath = tsiImgPath;
    }

    public String getTsiImgName() {
        return tsiImgName;
    }

    public void setTsiImgName(String tsiImgName) {
        this.tsiImgName = tsiImgName;
    }

    public String getTsiImgExt() {
        return tsiImgExt;
    }

    public void setTsiImgExt(String tsiImgExt) {
        this.tsiImgExt = tsiImgExt;
    }

    public String getTsiStat() {
        return tsiStat;
    }

    public void setTsiStat(String tsiStat) {
        this.tsiStat = tsiStat;
    }

    public String getTsiDnaPath() {
        return tsiDnaPath;
    }

    public void setTsiDnaPath(String tsiDnaPath) {
        this.tsiDnaPath = tsiDnaPath;
    }

    public String getTsiDnaText() {
        return tsiDnaText;
    }

    public void setTsiDnaText(String tsiDnaText) {
        this.tsiDnaText = tsiDnaText;
    }

    public int getUserUno() {
        return userUno;
    }

    public void setUserUno(int userUno) {
        this.userUno = userUno;
    }

    public Timestamp getFstDmlDt() {
        return fstDmlDt;
    }

    public void setFstDmlDt(Timestamp fstDmlDt) {
        this.fstDmlDt = fstDmlDt;
    }

    public Timestamp getTsiAlltimeDt() {
        return tsiAlltimeDt;
    }

    public void setTsiAlltimeDt(Timestamp tsiAlltimeDt) {
        this.tsiAlltimeDt = tsiAlltimeDt;
    }

    public Timestamp getLstDmlDt() {
        return lstDmlDt;
    }

    public void setLstDmlDt(Timestamp lstDmlDt) {
        this.lstDmlDt = lstDmlDt;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getTsiAlltimeMonitoring(){ return tsiAlltimeMonitoring;}

    public void setTsiAlltimeMonitoring(String tsiAlltimeMonitoring) {this.tsiAlltimeMonitoring = tsiAlltimeMonitoring;}

    public String getTsiUserFile(){return tsiUserFile;}
    public void setTsiUserFile(String tsiUserFile){this.tsiUserFile=tsiUserFile;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchInfoEntity that = (SearchInfoEntity) o;
        return tsiUno == that.tsiUno && tsiGoogle == that.tsiGoogle && tsiTwitter == that.tsiTwitter && tsiFacebook == that.tsiFacebook && tsiInstagram == that.tsiInstagram && userUno == that.userUno && Objects.equals(tsiType, that.tsiType) && Objects.equals(tsiKeyword, that.tsiKeyword) && Objects.equals(tsiImgPath, that.tsiImgPath) && Objects.equals(tsiImgName, that.tsiImgName) && Objects.equals(tsiImgExt, that.tsiImgExt) && Objects.equals(tsiStat, that.tsiStat) && Objects.equals(tsiDnaPath, that.tsiDnaPath) && Objects.equals(tsiDnaText, that.tsiDnaText) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt)&& Objects.equals(tsiUserFile, that.tsiUserFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tsiUno, tsiType, tsiGoogle, tsiTwitter, tsiFacebook, tsiInstagram, tsiKeyword, tsiImgPath, tsiImgName, tsiImgExt, tsiStat, tsiDnaPath, tsiDnaText, userUno, fstDmlDt, lstDmlDt);
    }

    public String getTsiImgRealPath() {
        return tsiImgRealPath;
    }

    public void setTsiImgRealPath(String tsiImgRealPath) {
        this.tsiImgRealPath = tsiImgRealPath;
    }

    public String getDataStatCd() {
        return dataStatCd;
    }

    public void setDataStatCd(String dataStatCd) {
        this.dataStatCd = dataStatCd;
    }

    public String getTsiImgHeight() {
        return tsiImgHeight;
    }

    public void setTsiImgHeight(String tsiImgHeight) {
        this.tsiImgHeight = tsiImgHeight;
    }

    public String getTsiImgWidth() {
        return tsiImgWidth;
    }

    public void setTsiImgWidth(String tsiImgWidth) {
        this.tsiImgWidth = tsiImgWidth;
    }

    public String getTsiImgSize() {
        return tsiImgSize;
    }

    public void setTsiImgSize(String tsiImgSize) {
        this.tsiImgSize = tsiImgSize;
    }

    public Integer getTsrUno() {
        return tsrUno;
    }

    public void setTsrUno(Integer tsrUno) {
        this.tsrUno = tsrUno;
    }

}