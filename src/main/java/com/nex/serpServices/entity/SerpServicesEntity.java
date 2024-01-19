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
    @Id
    @Column(name = "ss_icon_path", nullable = true)
    private String ssIconPath;
    @Id
    @Column(name = "ss_name", nullable = true)
    private String ssName;
    @Id
    @Column(name = "ss_is_active", nullable = true)
    private int ssIsActive;
}
