package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tb_newkeyword_result", schema = "sittest")
@Data
public class NewKeywordResultEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "NKR_UNO")
    private int nkrUno;
    @Basic
    @Column(name = "IDX")
    private int idx;
    @Basic
    @Column(name = "NKR_JSON")
    private String nkrJson;
    @Basic
    @Column(name = "NKR_IMG_PATH")
    private String nkrImgPath;
    @Basic
    @Column(name = "NKR_IMG_NAME")
    private String nkrImgName;
    @Basic
    @Column(name = "NKR_IMG_EXT")
    private String nkrImgExt;
    @Basic
    @Column(name = "NKR_DOWNLOAD_URL")
    private String nkrDownloadUrl;
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
    @Column(name = "NKR_TITLE")
    private String nkrTitle;
    @Basic
    @Column(name = "NKR_SNS")
    private String nkrSns;
    @Basic
    @Column(name = "NKR_SITE_URL")
    private String nkrSiteUrl;
    @Basic
    @Column(name = "NKR_IMG_HEIGHT")
    private String nkrImgHeight;
    @Basic
    @Column(name = "NKR_IMG_WIDTH")
    private String nkrImgWidth;
    @Basic
    @Column(name = "NKR_IMG_SIZE")
    private String nkrImgSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewKeywordResultEntity that = (NewKeywordResultEntity) o;
        return nkrUno == that.nkrUno && idx == that.idx && Objects.equals(nkrJson, that.nkrJson) && Objects.equals(nkrImgPath, that.nkrImgPath) && Objects.equals(nkrImgName, that.nkrImgName) && Objects.equals(nkrImgExt, that.nkrImgExt) && Objects.equals(nkrDownloadUrl, that.nkrDownloadUrl) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nkrUno, idx, nkrJson, nkrImgPath, nkrImgName, nkrImgExt, nkrDownloadUrl, fstDmlDt, lstDmlDt);
    }

}
