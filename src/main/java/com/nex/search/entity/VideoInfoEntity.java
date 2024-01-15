package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
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
