package com.fc.v2.model.custom;

import java.io.Serializable;

/**
 * 回音里摆的一只"欠手"：哪一处、什么角色还没点。
 * 只由服务层从签记录账上现攒，页面照单摆叩门处，不许自行预设。
 *
 * @author fuce
 * @date 2026-09-28
 */
public class MovePendingRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private String roleKey;
    private String roleName;
    private int nodeNo;

    public MovePendingRole() {
    }

    public MovePendingRole(MoveRole role) {
        this.roleKey = role.name();
        this.roleName = role.getRoleName();
        this.nodeNo = role.getNodeNo();
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

    public int getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(int nodeNo) {
        this.nodeNo = nodeNo;
    }
}
