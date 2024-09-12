package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Data
@Table(name = "tb_search_user_file", schema = "sittest", catalog = "")
public class SearchUserFileEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "TSUF_UNO")
    private int tsufUno;
    @Basic
    @Column(name="TSUF_USER_FILE")
    private String tsufUserFile;
    @Basic
    @Column(name = "LAST_DML_DT")
    private Timestamp lastDmlDt;
}