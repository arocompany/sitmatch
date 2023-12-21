package com.nex.user.entity;

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
@Table(name = "TB_USER", schema = "sittest", catalog = "")
public class UserEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "USER_UNO")
    private Long userUno;
    @Basic
    @Column(name = "USER_ID")
    private String userId;
    @Basic
    @Column(name = "USER_PW")
    private String userPw;
    @Basic
    @Column(name = "PW_MODIFY_DT")
    private Timestamp pwModifyDt;
    @Basic
    @Column(name = "LST_LOGIN_DT")
    private Timestamp lstLoginDt;
    @Basic
    @Column(name = "USER_NM")
    private String userNm;
    @Basic
    @Column(name = "PHONE_NUM_1")
    private String phoneNum1;
    @Basic
    @Column(name = "PHONE_NUM_2")
    private String phoneNum2;
    @Basic
    @Column(name = "PHONE_NUM_3")
    private String phoneNum3;
    @Basic
    @Column(name = "EMAIL_ID")
    private String emailId;
    @Basic
    @Column(name = "EMAIL_DOMAIN")
    private String emailDomain;
    @Basic
    @Column(name = "USE_YN")
    private String useYn;
    @Basic
    @Column(name = "ORG_NM")
    private String orgNm;
    @Basic
    @Column(name = "USER_POSITION")
    private String userPosition;
    @Basic
    @Column(name = "USER_CLF_CD")
    private String userClfCd;
    @Basic
    @Column(name = "BIZ_DETAIL")
    private String bizDetail;
    @Basic
    @Column(name = "FST_DML_DT")
    private Timestamp fstDmlDt;
    @Basic
    @Column(name = "LST_DML_DT")
    private Timestamp lstDmlDt;

    @Basic
    @Column(name = "CRAWLING_LIMIT")
    private String crawling_limit;
    // private Integer crawling_limit;

    @Basic
    @Column(name = "PERCENT_LIMIT")
    private Integer percent_limit;

    @Basic
    @Column(name = "USER_CHK_CNT")
    private Integer userChkCnt;

    public Integer getUserChkCnt() {
        return userChkCnt;
    }

    public void setUserChkCnt(Integer userChkCnt) {
        this.userChkCnt = userChkCnt;
    }


    public String getCrawling_limit() {
        return crawling_limit;
    }

    public void setCrawling_limit(String crawling_limit) {
        this.crawling_limit = crawling_limit;
    }

    public Integer getPercent_limit() {
        return percent_limit;
    }

    public void setPercent_limit(Integer percent_limit) {
        this.percent_limit = percent_limit;
    }

    public Long getUserUno() {
        return userUno;
    }

    public void setUserUno(Long userUno) {
        this.userUno = userUno;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPw() {
        return userPw;
    }

    public void setUserPw(String userPw) {
        this.userPw = userPw;
    }

    public Timestamp getPwModifyDt() {
        return pwModifyDt;
    }

    public void setPwModifyDt(Timestamp pwModifyDt) {
        this.pwModifyDt = pwModifyDt;
    }

    public Timestamp getLstLoginDt() {
        return lstLoginDt;
    }

    public void setLstLoginDt(Timestamp lstLoginDt) {
        this.lstLoginDt = lstLoginDt;
    }

    public String getUserNm() {
        return userNm;
    }

    public void setUserNm(String userNm) {
        this.userNm = userNm;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public String getPhoneNum3() {
        return phoneNum3;
    }

    public void setPhoneNum3(String phoneNum3) {
        this.phoneNum3 = phoneNum3;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public String getOrgNm() {
        return orgNm;
    }

    public void setOrgNm(String orgNm) {
        this.orgNm = orgNm;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public String getUserClfCd() {
        return userClfCd;
    }

    public void setUserClfCd(String userClfCd) {
        this.userClfCd = userClfCd;
    }

    public String getBizDetail() {
        return bizDetail;
    }

    public void setBizDetail(String bizDetail) {
        this.bizDetail = bizDetail;
    }

    public Timestamp getFstDmlDt() {
        return fstDmlDt;
    }

    public void setFstDmlDt(Timestamp fstDmlDt) {
        this.fstDmlDt = fstDmlDt;
    }

    public Timestamp getLstDmlDt() {
        return lstDmlDt;
    }

    public void setLstDmlDt(Timestamp lstDmlDt) {
        this.lstDmlDt = lstDmlDt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return userUno == that.userUno && Objects.equals(userId, that.userId) && Objects.equals(userPw, that.userPw) && Objects.equals(pwModifyDt, that.pwModifyDt) && Objects.equals(lstLoginDt, that.lstLoginDt) && Objects.equals(userNm, that.userNm) && Objects.equals(phoneNum1, that.phoneNum1) && Objects.equals(phoneNum2, that.phoneNum2) && Objects.equals(phoneNum3, that.phoneNum3) && Objects.equals(emailId, that.emailId) && Objects.equals(emailDomain, that.emailDomain) && Objects.equals(useYn, that.useYn) && Objects.equals(orgNm, that.orgNm) && Objects.equals(userPosition, that.userPosition) && Objects.equals(userClfCd, that.userClfCd) && Objects.equals(bizDetail, that.bizDetail) && Objects.equals(fstDmlDt, that.fstDmlDt) && Objects.equals(lstDmlDt, that.lstDmlDt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUno, userId, userPw, pwModifyDt, lstLoginDt, userNm, phoneNum1, phoneNum2, phoneNum3, emailId, emailDomain, useYn, orgNm, userPosition, userClfCd, bizDetail, fstDmlDt, lstDmlDt);
    }
}
