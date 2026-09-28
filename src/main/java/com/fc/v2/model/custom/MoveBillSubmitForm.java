package com.fc.v2.model.custom;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 台站迁移核准单立单申报（沿用现行迁建申报表，不另做新单）。
 * 第一处台站材料：申请事由、新址经纬度与海拔、下垫面情形、旧址观测场现状，缺一项报不上去；
 * 新址编号、对比观测起止随单呈报，供省局核满期、供核讫后誊入启用通知。
 *
 * @author fuce
 * @date 2026-09-28
 */
public class MoveBillSubmitForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 申请台站编号 */
    private String siteNo;
    /** 申请台站名称 */
    private String siteName;
    /** 申请事由 */
    private String applyReason;
    /** 新址台站编号 */
    private String newSiteNo;
    /** 新址经度 */
    private String newLng;
    /** 新址纬度 */
    private String newLat;
    /** 新址海拔(米) */
    private BigDecimal newAlt;
    /** 下垫面情形 */
    private String surface;
    /** 旧址观测场现状 */
    private String oldSiteStatus;
    /** 对比观测起始时刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date compareStart;
    /** 对比观测截止时刻 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date compareEnd;

    public String getSiteNo() {
        return siteNo;
    }

    public void setSiteNo(String siteNo) {
        this.siteNo = siteNo;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public String getNewSiteNo() {
        return newSiteNo;
    }

    public void setNewSiteNo(String newSiteNo) {
        this.newSiteNo = newSiteNo;
    }

    public String getNewLng() {
        return newLng;
    }

    public void setNewLng(String newLng) {
        this.newLng = newLng;
    }

    public String getNewLat() {
        return newLat;
    }

    public void setNewLat(String newLat) {
        this.newLat = newLat;
    }

    public BigDecimal getNewAlt() {
        return newAlt;
    }

    public void setNewAlt(BigDecimal newAlt) {
        this.newAlt = newAlt;
    }

    public String getSurface() {
        return surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public String getOldSiteStatus() {
        return oldSiteStatus;
    }

    public void setOldSiteStatus(String oldSiteStatus) {
        this.oldSiteStatus = oldSiteStatus;
    }

    public Date getCompareStart() {
        return compareStart;
    }

    public void setCompareStart(Date compareStart) {
        this.compareStart = compareStart;
    }

    public Date getCompareEnd() {
        return compareEnd;
    }

    public void setCompareEnd(Date compareEnd) {
        this.compareEnd = compareEnd;
    }
}
