package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "tb_new_keyword", schema = "sittest", catalog = "")
@Getter
@Setter
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