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
 * 台站新址启用通知对象 t_metp_move_notice
 *
 * @author fuce
 * @date 2026-09-28
 */
@TableName("t_metp_move_notice")
@ApiModel(value = "TMetpMoveNotice", description = "台站新址启用通知")
public class TMetpMoveNotice implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 新址启用通知编号 */
    @TableField("notice_no")
    @ApiModelProperty(value = "新址启用通知编号")
    private String noticeNo;

    /** 来源核准单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "来源核准单主键")
    private Long billId;

    /** 来源核准单号 */
    @TableField("bill_no")
    @ApiModelProperty(value = "来源核准单号")
    private String billNo;

    /** 台站编号 */
    @TableField("site_no")
    @ApiModelProperty(value = "台站编号")
    private String siteNo;

    /** 台站名称 */
    @TableField("site_name")
    @ApiModelProperty(value = "台站名称")
    private String siteName;

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

    /** 对比观测起始时刻 */
    @TableField("compare_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "对比观测起始时刻")
    private Date compareStart;

    /** 对比观测截止时刻 */
    @TableField("compare_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "对比观测截止时刻")
    private Date compareEnd;

    /** 批准文号 */
    @TableField("approve_doc_no")
    @ApiModelProperty(value = "批准文号")
    private String approveDocNo;

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

    public String getNoticeNo() {
        return noticeNo;
    }

    public void setNoticeNo(String noticeNo) {
        this.noticeNo = noticeNo;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
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
