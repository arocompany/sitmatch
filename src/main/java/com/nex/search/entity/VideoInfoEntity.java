package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tb_video_info", schema = "sittest", catalog = "")
public class VideoInfoEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TVI_UNO")
    private int tviUno;
    @Basic
    @Column(name = "TSI_UNO")
    private int tsiUno;
    @Basic
    @Column(name = "TVI_IMG_REAL_PATH")
    private String tviImgRealPath;
    @Basic
    @Column(name = "TVI_IMG_NAME")
    private String tviImgName;
    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT")
    private Timestamp lstDmlDt;

    public int getTviUno() {
        return tviUno;
    }

    public void setTviUno(int tviUno) {
        this.tviUno = tviUno;
    }

    public int getTsiUno() {
        return tsiUno;
    }

    public void setTsiUno(int tsiUno) {
        this.tsiUno = tsiUno;
    }

    public String getTviImgRealPath() {
        return tviImgRealPath;
    }

    public void setTviImgRealPath(String tviImgRealPath) {
        this.tviImgRealPath = tviImgRealPath;
    }

    public String getTviImgName() {
        return tviImgName;
    }

    public void setTviImgName(String tviImgName) {
        this.tviImgName = tviImgName;
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
        VideoInfoEntity that = (VideoInfoEntity) o;
        return tviUno == that.tviUno && tsiUno == that.tsiUno && Objects.equals(tviImgRealPath, that.tviImgRealPath) && Objects.equals(tviImgName, that.tviImgName) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tviUno, tsiUno, tviImgRealPath, tviImgName, fstDmlDt, lstDmlDt);
    }
}
