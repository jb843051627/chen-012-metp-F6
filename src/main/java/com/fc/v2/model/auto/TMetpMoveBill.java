package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 台站迁移变更逐级核准单对象 t_metp_move_bill
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_metp_move_bill")
@ApiModel(value = "TMetpMoveBill", description = "台站迁移变更逐级核准单")
public class TMetpMoveBill implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 台站迁移变更核准单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "台站迁移变更核准单号")
    private String billNo;

    /** 台站编号 */
    @TableField("site_no")
    @ApiModelProperty(value = "台站编号")
    private String siteNo;

    /** 台站名称 */
    @TableField("site_name")
    @ApiModelProperty(value = "台站名称")
    private String siteName;

    /** 申请事由 */
    @TableField("reason")
    @ApiModelProperty(value = "申请事由")
    private String reason;

    /** 新址编号 */
    @TableField("new_site_no")
    @ApiModelProperty(value = "新址编号")
    private String newSiteNo;

    /** 新址经度 */
    @TableField("new_lng")
    @ApiModelProperty(value = "新址经度")
    private BigDecimal newLng;

    /** 新址纬度 */
    @TableField("new_lat")
    @ApiModelProperty(value = "新址纬度")
    private BigDecimal newLat;

    /** 新址海拔(米) */
    @TableField("new_alt")
    @ApiModelProperty(value = "新址海拔(米)")
    private BigDecimal newAlt;

    /** 新址下垫面情形 */
    @TableField("surface")
    @ApiModelProperty(value = "新址下垫面情形")
    private String surface;

    /** 旧址观测场现状 */
    @TableField("old_field_status")
    @ApiModelProperty(value = "旧址观测场现状")
    private String oldFieldStatus;

    /** 新址对比观测起始时刻 */
    @TableField("compare_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "新址对比观测起始时刻")
    private Date compareStart;

    /** 新址对比观测截止时刻 */
    @TableField("compare_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "新址对比观测截止时刻")
    private Date compareEnd;

    /** 国务院气象主管机构批准文号 */
    @TableField("approve_doc_no")
    @ApiModelProperty(value = "国务院气象主管机构批准文号")
    private String approveDocNo;

    /** 新站是否已建起 0未起 1已起(先建后迁把关依据,新站未起不许拆旧站) */
    @TableField("new_station_built")
    @ApiModelProperty(value = "新站是否已建起 0未起 1已起(先建后迁把关依据,新站未起不许拆旧站)")
    private Integer newStationBuilt;

    /** 攒签轮次(发回市局一次加一轮,废签留旧轮) */
    @TableField("round_no")
    @ApiModelProperty(value = "攒签轮次(发回市局一次加一轮,废签留旧轮)")
    private Integer roundNo;

    /** 当前所在层 0台站 1市局 2省局 3国务院气象主管机构 */
    @TableField("node_no")
    @ApiModelProperty(value = "当前所在层 0台站 1市局 2省局 3国务院气象主管机构")
    private Integer nodeNo;

    /** 同层核准方式 0任一人 1两名点齐 */
    @TableField("sign_mode")
    @ApiModelProperty(value = "同层核准方式 0任一人 1两名点齐")
    private Integer signMode;

    /** 本层应签人数 */
    @TableField("need_count")
    @ApiModelProperty(value = "本层应签人数")
    private Integer needCount;

    /** 本层已签人数 */
    @TableField("sign_count")
    @ApiModelProperty(value = "本层已签人数")
    private Integer signCount;

    /** 报批情形 0在核 1已核讫 2已打回重审 3不予同意(终局) */
    @TableField("status")
    @ApiModelProperty(value = "报批情形 0在核 1已核讫 2已打回重审 3不予同意(终局)")
    private Integer status;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNewSiteNo() {
        return newSiteNo;
    }

    public void setNewSiteNo(String newSiteNo) {
        this.newSiteNo = newSiteNo;
    }

    public BigDecimal getNewLng() {
        return newLng;
    }

    public void setNewLng(BigDecimal newLng) {
        this.newLng = newLng;
    }

    public BigDecimal getNewLat() {
        return newLat;
    }

    public void setNewLat(BigDecimal newLat) {
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

    public String getOldFieldStatus() {
        return oldFieldStatus;
    }

    public void setOldFieldStatus(String oldFieldStatus) {
        this.oldFieldStatus = oldFieldStatus;
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

    public String getApproveDocNo() {
        return approveDocNo;
    }

    public void setApproveDocNo(String approveDocNo) {
        this.approveDocNo = approveDocNo;
    }

    public Integer getNewStationBuilt() {
        return newStationBuilt;
    }

    public void setNewStationBuilt(Integer newStationBuilt) {
        this.newStationBuilt = newStationBuilt;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(Integer nodeNo) {
        this.nodeNo = nodeNo;
    }

    public Integer getSignMode() {
        return signMode;
    }

    public void setSignMode(Integer signMode) {
        this.signMode = signMode;
    }

    public Integer getNeedCount() {
        return needCount;
    }

    public void setNeedCount(Integer needCount) {
        this.needCount = needCount;
    }

    public Integer getSignCount() {
        return signCount;
    }

    public void setSignCount(Integer signCount) {
        this.signCount = signCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
