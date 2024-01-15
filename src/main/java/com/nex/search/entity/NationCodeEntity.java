package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_nation_code", schema = "sittest", catalog = "")
public class NationCodeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "nc_uno", nullable = false)
    private int ncUno;
    @Basic
    @Column(name = "nc_name", nullable = true)
    private String ncName;
    @Basic
    @Column(name = "nc_code", nullable = true)
    private String ncCode;
    @Basic
    @Column(name = "nc_is_active", nullable = true)
    private int ncIsActive;
}
