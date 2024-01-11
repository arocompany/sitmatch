package com.nex.Chart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Table(name = "TB_ALLTIME_MONITORING_HISTORY", schema = "sittest", catalog = "")
public class AlltimeMonitoringHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TAM_UNO")
    private Long tamUno;

    @Basic
    @Column(name = "TSR_UNO")
    private int tsrUno;

    @Basic
    @Column(name = "CLK_DML_DT")
    private Timestamp clkDmlDt;

    @Basic
    @Column(name = "USER_UNO")
    private int userUno;

    @Basic
    @Column(name = "USER_ID")
    private String userId;

    @Basic
    @Column(name = "TAM_YN")
    private String tamYn;

    // 필요한것
    // tsiUno, 시간,

}
