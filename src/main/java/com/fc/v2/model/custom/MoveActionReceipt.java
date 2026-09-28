package com.fc.v2.model.custom;

import com.fc.v2.model.auto.TMetpMoveNotice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 画押回音：动作灵没灵、落押人是谁、件此刻停在何处、本处还差谁签，全由服务层据实填给，
 * 页面只许照此摆陈，不许预设落定结果。
 *
 * @author fuce
 * @date 2026-09-28
 */
public class MoveActionReceipt implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 动作是否作数（被门控挡回、重签替签、冻单后再押，皆为 false） */
    private boolean accepted;
    /** 事由交代 */
    private String message;
    /** 本次动作 AGREE/VETO/REJECT/SUBMIT */
    private String action;
    /** 实际画押人（取自登录身份，页面带不算） */
    private String signer;
    /** 画押时所用职权 */
    private String roleKey;
    /** 职权名目 */
    private String roleName;

    private Long billId;
    private String billNo;
    private Integer roundNo;
    /** 动作后件停在哪一层 */
    private Integer nodeNo;
    private String nodeName;
    /** 件的落定 0在核 1已核讫 2不予同意 */
    private Integer billStatus;
    /** 是否已冻（定否终态） */
    private boolean frozen;
    /** 是否已核讫 */
    private boolean finished;

    private Integer signMode;
    private Integer needCount;
    private Integer signCount;
    /** 本处尚欠的职权（代号） */
    private List<String> pendingRoles = new ArrayList<>();
    /** 本处尚欠的职权（名目） */
    private List<String> pendingRoleNames = new ArrayList<>();
    /** 本处在册的全部职权（代号，供定否择一） */
    private List<String> allRoles = new ArrayList<>();
    /** 本处在册的全部职权（名目） */
    private List<String> allRoleNames = new ArrayList<>();

    /** 核讫一键生成的启用通知 */
    private TMetpMoveNotice notice;

    public static MoveActionReceipt refused(String action, String message) {
        MoveActionReceipt r = new MoveActionReceipt();
        r.accepted = false;
        r.action = action;
        r.message = message;
        return r;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(Integer billStatus) {
        this.billStatus = billStatus;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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

    public List<String> getPendingRoles() {
        return pendingRoles;
    }

    public void setPendingRoles(List<String> pendingRoles) {
        this.pendingRoles = pendingRoles;
    }

    public List<String> getPendingRoleNames() {
        return pendingRoleNames;
    }

    public void setPendingRoleNames(List<String> pendingRoleNames) {
        this.pendingRoleNames = pendingRoleNames;
    }

    public List<String> getAllRoles() {
        return allRoles;
    }

    public void setAllRoles(List<String> allRoles) {
        this.allRoles = allRoles;
    }

    public List<String> getAllRoleNames() {
        return allRoleNames;
    }

    public void setAllRoleNames(List<String> allRoleNames) {
        this.allRoleNames = allRoleNames;
    }

    public TMetpMoveNotice getNotice() {
        return notice;
    }

    public void setNotice(TMetpMoveNotice notice) {
        this.notice = notice;
    }
}
