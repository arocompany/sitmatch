package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
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
}