package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_request_serp_api_log", schema = "sittest")
@Data
public class RequestSerpApiLogEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "rsl_uno")
    private int rslUno;

    @Basic
    @Column(name = "tsi_uno")
    private int tsiUno;

    @Basic
    @Column(name = "rsl_query")
    private String rslQuery;

    @Basic
    @Column(name = "rsl_nation")
    private String rslNation;

    @Basic
    @Column(name = "rsl_engine")
    private String rslEngine;

    @Basic
    @Column(name = "rsl_keyword")
    private String rslKeyword;

    @Basic
    @Column(name = "rsl_page_no")
    private int rslPageNo;

    @Basic
    @Column(name = "rsl_api_token")
    private String rslApiToken;

    @Basic
    @Column(name = "rsl_page_token")
    private String rslPageToken;

    @Basic
    @Column(name = "rsl_image_url")
    private String rslImageUrl;

    @Basic
    @Column(name = "rsl_status")
    private int rslStatus;

    @Basic
    @Column(name = "rsl_dt_insert")
    private Timestamp rslDtInsert;

    @Basic
    @Column(name = "rsl_dt_update")
    private Timestamp rslDtUpdate;

    @Basic
    @Column(name = "rsl_fail_reason")
    private String rslFailReason;

}
