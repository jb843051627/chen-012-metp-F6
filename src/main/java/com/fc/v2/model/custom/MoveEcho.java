package com.fc.v2.model.custom;

import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;

import java.util.ArrayList;
import java.util.List;

/**
 * 画押回音：动作灵没灵，全凭回音里的权柄说话。
 * 页面不得自行假设落定何样，一律以本回音为准——停在哪一层、本层还欠哪双手、
 * 已落了哪些印，全部由服务层从单据账上攒出来。
 *
 * @author fuce
 * @date 2026-09-28
 */
public class MoveEcho {

    /** 本动作是否作数（被门禁/替签/终局拦下时为 false） */
    private boolean effective;

    /** 权柄交代 */
    private String message;

    /** 动作后单据现状（行内所存） */
    private TMetpMoveBill bill;

    /** 当前层本轮尚欠的点印角色 */
    private List<MovePendingRole> pendingRoles = new ArrayList<>();

    /** 当前轮各层有效签记录（废签不在其列） */
    private List<TMetpMoveSign> signs = new ArrayList<>();

    /** 已生成时随回音端出的新址启用通知（未生成则空） */
    private TMetpMoveNotice notice;

    public static MoveEcho deny(String message) {
        MoveEcho echo = new MoveEcho();
        echo.effective = false;
        echo.message = message;
        return echo;
    }

    public static MoveEcho ok(String message) {
        MoveEcho echo = new MoveEcho();
        echo.effective = true;
        echo.message = message;
        return echo;
    }

    public boolean isEffective() {
        return effective;
    }

    public void setEffective(boolean effective) {
        this.effective = effective;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TMetpMoveBill getBill() {
        return bill;
    }

    public void setBill(TMetpMoveBill bill) {
        this.bill = bill;
    }

    public List<MovePendingRole> getPendingRoles() {
        return pendingRoles;
    }

    public void setPendingRoles(List<MovePendingRole> pendingRoles) {
        this.pendingRoles = pendingRoles;
    }

    public List<TMetpMoveSign> getSigns() {
        return signs;
    }

    public void setSigns(List<TMetpMoveSign> signs) {
        this.signs = signs;
    }

    public TMetpMoveNotice getNotice() {
        return notice;
    }

    public void setNotice(TMetpMoveNotice notice) {
        this.notice = notice;
    }
}
