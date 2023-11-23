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
@Table(name = "TB_NOTICE_HISTORY", schema = "sittest", catalog = "")
public class NoticeHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "HIS_NTC_UNO")
    private Long hisNtcUno;

    @Basic
    @Column(name = "CLK_DML_DT")
    private Timestamp clkDmlDt;

    @Basic
    @Column(name = "USER_UNO")
    private int userUno;

    @Basic
    @Column(name = "USER_ID")
    private String userId;

}
