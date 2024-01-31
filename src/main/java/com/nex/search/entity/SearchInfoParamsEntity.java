package com.nex.search.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tb_search_info_params", schema = "sittest", catalog = "")
public class SearchInfoParamsEntity {

    @Id @Column(name = "TSI_UNO") private int tsiUno;

    @Basic @Column(name="TSI_IS_NATION_US") private int tsiIsNationUs;
    @Basic @Column(name="TSI_IS_NATION_KR") private int tsiIsNationKr;
    @Basic @Column(name="TSI_IS_NATION_CN") private int tsiIsNationCn;
    @Basic @Column(name="TSI_IS_NATION_NL") private int tsiIsNationNl;
    @Basic @Column(name="TSI_IS_NATION_TH") private int tsiIsNationTh;
    @Basic @Column(name="TSI_IS_NATION_RU") private int tsiIsNationRu;
    @Basic @Column(name="TSI_IS_NATION_VN") private int tsiIsNationVn;

    @Basic @Column(name="TSI_IS_ENGINE_GOOGLE") private int tsiIsEngineGoogle;
    @Basic @Column(name="TSI_IS_ENGINE_YOUTUBE") private int tsiIsEngineYoutube;
    @Basic @Column(name="TSI_IS_ENGINE_GOOGLE_LENS") private int tsiIsEngineGoogleLens;
    @Basic @Column(name="TSI_IS_ENGINE_BAIDU") private int tsiIsEngineBaidu;
    @Basic @Column(name="TSI_IS_ENGINE_BING") private int tsiIsEngineBing;
    @Basic @Column(name="TSI_IS_ENGINE_DUCKDUCKGO") private int tsiIsEngineDuckduckgo;
    @Basic @Column(name="TSI_IS_ENGINE_YAHOO") private int tsiIsEngineYahoo;
    @Basic @Column(name="TSI_IS_ENGINE_YANDEX") private int tsiIsEngineYandex;
    @Basic @Column(name="TSI_IS_ENGINE_NAVER") private int tsiIsEngineNaver;
}