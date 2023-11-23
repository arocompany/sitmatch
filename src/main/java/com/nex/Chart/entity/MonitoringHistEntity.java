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
@Table(name = "TB_MONITORING_HISTORY", schema = "sittest", catalog = "")
public class MonitoringHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TMH_UNO")
    private Long tmhUno;

    @Basic
    @Column(name = "TMH_TSR_UNO")
    private int tmhTsrUno;

    @Basic
    @Column(name="CLK_DML_DT")
    private Timestamp clkDmlDt;

    @Basic
    @Column(name = "USER_UNO")
    private int userUno;

    @Basic
    @Column(name = "USER_ID")
    private String userId;


}
