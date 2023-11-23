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
@Table(name = "TB_DELETE_COMPT_HISTORY", schema = "sittest", catalog = "")
public class DeleteComptHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TDC_UNO")
    private Long tdcUno;

    @Basic
    @Column(name = "TDC_TSR_UNO")
    private int tdcTsrUno;

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
