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
@Table(name = "TB_SEARCH_RESULT_HISTORY", schema = "sittest", catalog = "")
public class SearchResultHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "HIS_RSLT_UNO")
    private Long hisRsltUno;

    @Basic
    @Column(name = "HIS_TSI_UNO")
    private int hisTsiUno;

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
