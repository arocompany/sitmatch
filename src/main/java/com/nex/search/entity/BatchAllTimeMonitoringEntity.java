package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
@Entity
@Data
@Table(name = "batch_alltime_monitoring_history", schema = "sittest", catalog = "")
public class BatchAllTimeMonitoringEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "BAM_UNO")
    private int bamUno;

    @Basic
    @Column(name = "TSI_UNO")
    private int tsiUno;

    @Basic
    @Column(name = "TSR_UNO")
    private int tsrUno;

    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;

}
