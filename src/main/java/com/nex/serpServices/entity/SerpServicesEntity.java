package com.nex.serpServices.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "tb_serp_services", schema = "sittest", catalog = "")
public class SerpServicesEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "ss_uno", nullable = false)
    private int ssUno;

    @Basic
    @Column(name = "ss_icon_path", nullable = false)
    private String ssIconPath;

    @Basic
    @Column(name = "ss_name", nullable = false)
    private String ssName;

    @Basic
    @Column(name = "ss_is_active", nullable = false)
    private int ssIsActive;

    @Basic
    @Column(name = "ss_is_view_active", nullable = false)
    private int ssIsViewActive;
}
