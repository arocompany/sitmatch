package com.nex.user.entity;

import com.nex.search.entity.SearchResultEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "tb_auto_keyword", schema = "sittest", catalog = "")
public class AutoEntity {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "AUTO_UNO")
    private int auto_nuo;

    @Basic
    @Column(name = "AUTO_USER_ID")
    private String auto_user_id;

    @Basic
    @Column(name = "AUTO_KEYWORD")
    private String auto_keyword;

    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fst_dml_dt;

    public int getAuto_nuo() {
        return auto_nuo;
    }

    public void setAuto_nuo(int auto_nuo) {
        this.auto_nuo = auto_nuo;
    }

    public String getAuto_user_id() {
        return auto_user_id;
    }

    public void setAuto_user_id(String auto_user_id) {
        this.auto_user_id = auto_user_id;
    }

    public String getAuto_keyword() {
        return auto_keyword;
    }

    public void setAuto_keyword(String auto_keyword) {
        this.auto_keyword = auto_keyword;
    }

    public Timestamp getFst_dml_dt() {
        return fst_dml_dt;
    }

    public void setFst_dml_dt(Timestamp fst_dml_dt) {
        this.fst_dml_dt = fst_dml_dt;
    }

}
