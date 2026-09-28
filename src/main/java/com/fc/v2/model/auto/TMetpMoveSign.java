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
 * 台站迁移核准画押记录对象 t_metp_move_sign
 * 只增不改：无 update 字段。驳回划去的前一轮签记录原样留存，凭 roundNo 与新签划开。
 *
 * @author fuce
 * @date 2026-09-28
 */
@TableName("t_metp_move_sign")
@ApiModel(value = "TMetpMoveSign", description = "台站迁移核准画押记录(只增不改)")
public class TMetpMoveSign implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 动作：落押 */
    public static final String ACTION_AGREE = "AGREE";
    /** 动作：不予同意 */
    public static final String ACTION_VETO = "VETO";
    /** 动作：驳回(省局退市局) */
    public static final String ACTION_REJECT = "REJECT";

    /** 职权：台站申请 */
    public static final String ROLE_STATION_APPLY = "STATION_APPLY";
    /** 职权：市局核探测环境 */
    public static final String ROLE_CITY_ENV = "CITY_ENV";
    /** 职权：省局盯对比观测满期、历史序列衔接 */
    public static final String ROLE_PROV_COMPARE = "PROV_COMPARE";
    /** 职权：省局盯先建后迁次序 */
    public static final String ROLE_PROV_BUILD = "PROV_BUILD";
    /** 职权：国务院气象主管机构批复 */
    public static final String ROLE_NATION_APPROVE = "NATION_APPROVE";
    /** 职权：省局驳回(省局任一位当值人可驳回) */
    public static final String ROLE_PROV_REJECT = "PROV_REJECT";

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 所属核准单 */
    @TableField("bill_id")
    @ApiModelProperty(value = "所属核准单")
    private Long billId;

    /** 落押层 0台站 1市局 2省局 3国务院气象主管机构 */
    @TableField("node_no")
    @ApiModelProperty(value = "落押层 0台站 1市局 2省局 3国务院气象主管机构")
    private Integer nodeNo;

    /** 职权代号 STATION_APPLY/CITY_ENV/PROV_COMPARE/PROV_BUILD/NATION_APPROVE/PROV_REJECT */
    @TableField("role_key")
    @ApiModelProperty(value = "职权代号")
    private String roleKey;

    /** 画押人登录账号(取自登录身份,不由页面带入) */
    @TableField("signer")
    @ApiModelProperty(value = "画押人登录账号")
    private String signer;

    /** 动作 AGREE落押 VETO不予同意 REJECT驳回 */
    @TableField("action")
    @ApiModelProperty(value = "动作 AGREE/VETO/REJECT")
    private String action;

    /** 押下所附意见 */
    @TableField("opinion")
    @ApiModelProperty(value = "押下所附意见")
    private String opinion;

    /** 画押时所属轮次 */
    @TableField("round_no")
    @ApiModelProperty(value = "画押时所属轮次")
    private Integer roundNo;

    /** 删除标记 0正常 1删除（画押记录只增，不做删除） */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记")
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Integer getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(Integer roundNo) {
        this.roundNo = roundNo;
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
}
