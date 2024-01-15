package com.nex.search.entity;

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
    @Column(name = "NC_NAME", nullable = true)
    private String ncName;
    @Basic
    @Column(name = "NC_CODE", nullable = true)
    private String ncCode;
    @Basic
    @Column(name = "NC_IS_ACTIVE", nullable = true)
    private int ncIsActive;
}
