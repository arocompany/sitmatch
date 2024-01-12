package com.nex.search.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_search_result_monitoring_history", schema = "sittest", catalog = "")
public class SearchResultMonitoringHistoryEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSRMH_UNO")
    private int tsrmhUno;
    @Basic
    @Column(name = "TSR_UNO")
    private int tsrUno;
    @Basic
    @Column(name = "TSRMH_CREATE_DATE")
    private Timestamp tsrmhCreateDate;

    public int getTsrmhUno() {
        return tsrmhUno;
    }

    public void setTsrmhUno(int tsrmhUno) {
        this.tsrmhUno = tsrmhUno;
    }

    public int getTsrUno() {
        return tsrUno;
    }

    public void setTsrUno(int tsrUno) {
        this.tsrUno = tsrUno;
    }

    public Timestamp getTsrmhCreateDate() {
        return tsrmhCreateDate;
    }

    public void setTsrmhCreateDate(Timestamp tsrmhCreateDate) {
        this.tsrmhCreateDate = tsrmhCreateDate;
    }
}