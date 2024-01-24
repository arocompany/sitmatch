package com.nex.nations.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_nation_code", schema = "sittest", catalog = "")
public class NationCodeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "NC_UNO", nullable = false)
    private int ncUno;
    @Basic
    @Column(name = "NC_NAME", nullable = false)
    private String ncName;
    @Basic
    @Column(name = "NC_CODE", nullable = false)
    private String ncCode;
    @Basic
    @Column(name = "NC_IS_ACTIVE", nullable = false)
    private int ncIsActive;
}
