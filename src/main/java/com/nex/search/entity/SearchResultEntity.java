package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
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

    @Basic
    @Column(name = "TSR_SEARCH_VALUE")
    private String tsrSearchValue;

    @Basic
    @Column(name = "TSR_IS_BATCH")
    private int tsrIsBatch;

    @Basic
    @Column(name = "TSR_CYCLE_BATCH")
    private int tsrCycleBatch;

    @Basic
    @Column(name = "TSR_NATION_CODE")
    private String tsrNationCode;

    @Basic
    @Column(name = "TSR_ENGINE")
    private String tsrEngine;

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
}
