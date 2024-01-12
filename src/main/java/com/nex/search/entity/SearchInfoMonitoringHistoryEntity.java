package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_search_info_monitoring_histroy", schema = "sittest", catalog = "")
public class SearchInfoMonitoringHistoryEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSIMH_UNO")
    private int tsimhUno;
    @Basic
    @Column(name = "TSI_UNO")
    private int tsiUno;
    @Basic
    @Column(name = "TSIMH_CREATE_DATE")
    private Timestamp tsimhCreateDate;

    public int getTsimhUno() {
        return tsimhUno;
    }

    public int getTsiUno() {
        return tsiUno;
    }

    public Timestamp getTsimhCreateDate() {
        return tsimhCreateDate;
    }

    public void setTsimhUno(int tsimhUno) {
        this.tsimhUno = tsimhUno;
    }

    public void setTsiUno(int tsiUno) {
        this.tsiUno = tsiUno;
    }

    public void setTsimhCreateDate(Timestamp tsimhCreateDate) {
        this.tsimhCreateDate = tsimhCreateDate;
    }
}