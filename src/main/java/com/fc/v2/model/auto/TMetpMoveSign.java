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
import java.util.Date;

/**
 * 台站迁移逐级画押记录对象 t_metp_move_sign
 *
 * @author fuce
 * @date 2026-09-28
 */
@TableName("t_metp_move_sign")
@ApiModel(value = "TMetpMoveSign", description = "台站迁移逐级画押记录")
public class TMetpMoveSign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 核准单主键 */
    @TableField("bill_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "核准单主键")
    private Long billId;

    /** 落押时所在攒签轮次 */
    @TableField("round_no")
    @ApiModelProperty(value = "落押时所在攒签轮次")
    private Integer roundNo;

    /** 所处层 0台站 1市局 2省局 3国务院气象主管机构 */
    @TableField("node_no")
    @ApiModelProperty(value = "所处层 0台站 1市局 2省局 3国务院气象主管机构")
    private Integer nodeNo;

    /** 点印角色 STATION/CITY/PROV_COMPARE/PROV_ORDER/NATIONAL */
    @TableField("role_key")
    @ApiModelProperty(value = "点印角色 STATION/CITY/PROV_COMPARE/PROV_ORDER/NATIONAL")
    private String roleKey;

    /** 落押人(服务层取自当前登录人,门脸不得代传) */
    @TableField("signer")
    @ApiModelProperty(value = "落押人(服务层取自当前登录人,门脸不得代传)")
    private String signer;

    /** 动作 1落押同意 2不予同意 3发回市局 */
    @TableField("action")
    @ApiModelProperty(value = "动作 1落押同意 2不予同意 3发回市局")
    private Integer action;

    /** 签批意见 */
    @TableField("opinion")
    @ApiModelProperty(value = "签批意见")
    private String opinion;

    /** 落定当时单据全样(屏幕所见即行内所存) */
    @TableField("snapshot")
    @ApiModelProperty(value = "落定当时单据全样(屏幕所见即行内所存)")
    private String snapshot;

    /** 本轮是否有效 1有效 0发回后划废 */
    @TableField("valid_flag")
    @ApiModelProperty(value = "本轮是否有效 1有效 0发回后划废")
    private Integer validFlag;

    /** 落押时刻 */
    @TableField("sign_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "落押时刻")
    private Date signTime;

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

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
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

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public Integer getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

    public Date getSignTime() {
        return signTime;
    }

    public void setSignTime(Date signTime) {
        this.signTime = signTime;
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
