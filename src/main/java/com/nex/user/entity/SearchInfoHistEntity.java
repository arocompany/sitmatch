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
@Table(name = "TB_SEARCH_INFO_HISTORY", schema = "sittest", catalog = "")
public class SearchInfoHistEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "HIS_TSI_UNO")
    private Long hisTsiUno;

    @Basic
    @Column(name = "HIS_KEYWORD")
    private String hisKeyword;

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
