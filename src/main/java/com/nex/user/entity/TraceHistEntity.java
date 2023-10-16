package com.nex.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@Table(name = "TB_TRACE_HISTORY", schema = "sittest", catalog = "")
public class TraceHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "HIS_TRC_UNO")
    private Long hisTrcUno;
    @Basic
    @Column(name = "HIST_KEYWORD")
    private String histKeyword;
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
