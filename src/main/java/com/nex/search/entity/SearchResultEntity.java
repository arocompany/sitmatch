package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tb_search_result", schema = "sittest", catalog = "")
public class SearchResultEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSR_UNO")
    private int tsrUno;
    @Basic
    @Column(name = "TSI_UNO")
    private int tsiUno;
    @Basic
    @Column(name = "TSR_JSON")
    private String tsrJson;
    @Basic
    @Column(name = "TSR_IMG_PATH")
    private String tsrImgPath;
    @Basic
    @Column(name = "TSR_IMG_NAME")
    private String tsrImgName;
    @Basic
    @Column(name = "TSR_IMG_EXT")
    private String tsrImgExt;
    @Basic
    @Column(name = "TSR_DOWNLOAD_URL")
    private String tsrDownloadUrl;
    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT")
    private Timestamp lstDmlDt;
    @Basic
    @Column(name = "MST_DML_DT")
    private Timestamp mstDmlDt;
    @Basic
    @Column(name = "TRK_STAT_CD")
    private String trkStatCd;
    @Basic
    @Column(name = "DATA_STAT_CD")
    private String dataStatCd;
    @Basic
    @Column(name = "MONITORING_CD")
    private String monitoringCd;
    @Basic
    @Column(name = "TRK_HIST_MEMO")
    private String trkHistMemo;
    @Basic
    @Column(name = "TSR_TITLE")
    private String tsrTitle;
    @Basic
    @Column(name = "TSR_SNS")
    private String tsrSns;
    @Basic
    @Column(name = "TSR_SITE_URL")
    private String tsrSiteUrl;
    @Basic
    @Column(name = "TSR_IMG_HEIGHT")
    private String tsrImgHeight;
    @Basic
    @Column(name = "TSR_IMG_WIDTH")
    private String tsrImgWidth;
    @Basic
    @Column(name = "TSR_IMG_SIZE")
    private String tsrImgSize;

    public int getTsrUno() {
        return tsrUno;
    }

    public void setTsrUno(int tsrUno) {
        this.tsrUno = tsrUno;
    }

    public int getTsiUno() {
        return tsiUno;
    }

    public void setTsiUno(int tsiUno) {
        this.tsiUno = tsiUno;
    }

    public String getTsrJson() {
        return tsrJson;
    }

    public void setTsrJson(String tsrJson) {
        this.tsrJson = tsrJson;
    }

    public String getTsrImgPath() {
        return tsrImgPath;
    }

    public void setTsrImgPath(String tsrImgPath) {
        this.tsrImgPath = tsrImgPath;
    }

    public String getTsrImgName() {
        return tsrImgName;
    }

    public void setTsrImgName(String tsrImgName) {
        this.tsrImgName = tsrImgName;
    }

    public String getTsrImgExt() {
        return tsrImgExt;
    }

    public void setTsrImgExt(String tsrImgExt) {
        this.tsrImgExt = tsrImgExt;
    }

    public String getTsrDownloadUrl() {
        return tsrDownloadUrl;
    }

    public void setTsrDownloadUrl(String tsrDownloadUrl) {
        this.tsrDownloadUrl = tsrDownloadUrl;
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

    public Timestamp getMstDmlDt() {
        return mstDmlDt;
    }

    public void setMstDmlDt(Timestamp mstDmlDt) {
        this.mstDmlDt = mstDmlDt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchResultEntity that = (SearchResultEntity) o;
        return tsrUno == that.tsrUno && tsiUno == that.tsiUno && Objects.equals(tsrJson, that.tsrJson) && Objects.equals(tsrImgPath, that.tsrImgPath) && Objects.equals(tsrImgName, that.tsrImgName) && Objects.equals(tsrImgExt, that.tsrImgExt) && Objects.equals(tsrDownloadUrl, that.tsrDownloadUrl) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tsrUno, tsiUno, tsrJson, tsrImgPath, tsrImgName, tsrImgExt, tsrDownloadUrl, fstDmlDt, lstDmlDt);
    }

    public String getTrkStatCd() {
        return trkStatCd;
    }

    public void setTrkStatCd(String trkStatCd) {
        this.trkStatCd = trkStatCd;
    }

    public String getDataStatCd() {
        return dataStatCd;
    }

    public void setDataStatCd(String dataStatCd) {
        this.dataStatCd = dataStatCd;
    }

    public String getMonitoringCd() {
        return monitoringCd;
    }

    public void setMonitoringCd(String monitoringCd) {
        this.monitoringCd = monitoringCd;
    }

    public String getTrkHistMemo() {
        return trkHistMemo;
    }

    public void setTrkHistMemo(String trkHistMemo) {
        this.trkHistMemo = trkHistMemo;
    }

    public String getTsrTitle() {
        return tsrTitle;
    }

    public void setTsrTitle(String tsrTitle) {
        this.tsrTitle = tsrTitle;
    }

    public String getTsrSns() {
        return tsrSns;
    }

    public void setTsrSns(String tsrSns) {
        this.tsrSns = tsrSns;
    }

    public String getTsrSiteUrl() {
        return tsrSiteUrl;
    }

    public void setTsrSiteUrl(String tsrSiteUrl) {
        this.tsrSiteUrl = tsrSiteUrl;
    }

    public String getTsrImgHeight() {
        return tsrImgHeight;
    }

    public void setTsrImgHeight(String tsrImgHeight) {
        this.tsrImgHeight = tsrImgHeight;
    }

    public String getTsrImgWidth() {
        return tsrImgWidth;
    }

    public void setTsrImgWidth(String tsrImgWidth) {
        this.tsrImgWidth = tsrImgWidth;
    }

    public String getTsrImgSize() {
        return tsrImgSize;
    }

    public void setTsrImgSize(String tsrImgSize) {
        this.tsrImgSize = tsrImgSize;
    }
}
