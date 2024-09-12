package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@Table(name = "tb_search_info", schema = "sittest", catalog = "")
public class SearchInfoEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSI_UNO")
    private int tsiUno;
    @Basic
    @Column(name="TSI_SEARCH_TYPE")
    private int tsiSearchType;
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
    /*@Basic
    @Column(name = "TSI_ALLTIME_DT")
    private Timestamp tsiAlltimeDt;
    */
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
    @Column(name = "TSI_USER_FILE")
    private String tsiUserFile;

    @Basic
    @Column(name = "TSI_IS_DEPLOY")
    private Integer tsiIsDeploy;

    @Basic
    @Column(name = "TSI_MONITORING_CNT")
    private Integer tsiMonitoringCnt;
    @Basic
    @Column(name = "TSI_CNT_TSR")
    private Integer tsiCntTsr;
    @Basic
    @Column(name = "TSI_CNT_SIMILARITY")
    private Integer tsiCntSimilarity;
    @Basic
    @Column(name = "TSI_CNT_CHILD")
    private Integer tsiCntChild;
    @Basic
    @Column(name = "TSUF_UNO")
    private Integer tsufUno;


    @Basic
    @Column(name = "TSI_CNT_TSR")
    private Integer tsiCntTsr;

    @Basic
    @Column(name = "TSI_CNT_SIMILARITY")
    private Integer tsiCntSimilarity;

    @Basic
    @Column(name = "TSI_CNT_CHILD")
    private Integer tsiCntChild;

    @Basic
    @Column(name = "TSUF_UNO")
    private Integer tsufUno;

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
}