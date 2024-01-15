package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "tb_search_info_monitoring_history", schema = "sittest", catalog = "")
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
}