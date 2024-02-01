package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_request_serp_api_log", schema = "sittest")
@Data
public class RequestSerpApiLogEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id @Column(name = "rsl_uno") private Integer rslUno;
    @Basic @Column(name = "tsi_uno") private Integer tsiUno;
    @Nullable @Basic @Column(name = "rsl_url") private String rslUrl;
    @Nullable @Basic @Column(name = "rsl_nation") private String rslNation;
    @Nullable @Basic @Column(name = "rsl_engine") private String rslEngine;
    @Nullable @Basic @Column(name = "rsl_keyword") private String rslKeyword;
    @Nullable @Basic @Column(name = "rsl_page_no") private Integer rslPageNo;
    @Nullable @Basic @Column(name = "rsl_api_token") private String rslApiToken;
    @Nullable @Basic @Column(name = "rsl_page_token") private String rslPageToken;
    @Nullable @Basic @Column(name = "rsl_image_url") private String rslImageUrl;
    @Basic @Column(name = "rsl_status") private Integer rslStatus;
    @Nullable @Basic @Column(name = "rsl_dt_insert") private Timestamp rslDtInsert;
    @Nullable @Basic @Column(name = "rsl_dt_update") private Timestamp rslDtUpdate;
    @Nullable @Basic @Column(name = "rsl_fail_reason") private String rslFailReason;
    @Nullable @Basic @Column(name = "rsl_result") private String rslResult;
}
