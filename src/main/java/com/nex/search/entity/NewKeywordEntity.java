package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "tb_new_keyword", schema = "sittest", catalog = "")
@Data
public class NewKeywordEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "IDX")
    private Long idx;
    @Basic
    @Column(name = "USER_ID")
    private String userId;
    @Basic
    @Column(name = "KEYWORD")
    private String keyword;
    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;

    @Basic
    @Column(name = "KEYWORD_STUS")
    private String keywordStus;
}