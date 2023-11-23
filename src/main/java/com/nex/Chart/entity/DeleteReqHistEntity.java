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
@Table(name = "TB_DELETE_REQ_HISTORY", schema = "sittest", catalog = "")
public class DeleteReqHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TDR_UNO")
    private Long tdrUno;

    @Basic
    @Column(name = "TDR_TSR_UNO")
    private int tdrTsrUno;

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
