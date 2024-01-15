package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@Table(name = "tb_search_job", schema = "sittest", catalog = "")
public class SearchJobEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSJ_UNO", nullable = false)
    private int tsjUno;
    @Basic
    @Column(name = "TSI_UNO", nullable = false)
    private int tsiUno;
    @Basic
    @Column(name = "TSR_UNO", nullable = false)
    private int tsrUno;
    @Basic
    @Column(name = "TSR_IMG_PATH", length = 200)
    private String tsrImgPath;
    @Basic
    @Column(name = "TSR_IMG_NAME", length = 200)
    private String tsrImgName;
    @Basic
    @Column(name = "TSR_IMG_EXT", length = 10)
    private String tsrImgExt;
    @Basic
    @Column(name = "FST_DML_DT", nullable = false)
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT", nullable = false)
    private Timestamp lstDmlDt;
    @Basic
    @Column(name = "TSJ_STATUS", nullable = false, length = 10)
    private String tsjStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchJobEntity that = (SearchJobEntity) o;
        return tsjUno == that.tsjUno && tsiUno == that.tsiUno && tsrUno == that.tsrUno && Objects.equals(tsrImgPath, that.tsrImgPath) && Objects.equals(tsrImgName, that.tsrImgName) && Objects.equals(tsrImgExt, that.tsrImgExt) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tsjUno, tsiUno, tsrUno, tsrImgPath, tsrImgName, tsrImgExt, fstDmlDt, lstDmlDt);
    }
}
