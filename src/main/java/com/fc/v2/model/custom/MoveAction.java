package com.fc.v2.model.custom;

/**
 * 画押三大动作：落押、定否、驳回，概归服务层执掌，页面只许叩门、不许预设结果。
 *
 * @author fuce
 * @date 2026-09-28
 */
public enum MoveAction {

    /** 落押同意 */
    AGREE(1, "落押同意"),

    /** 不予同意（终局，件即冻死） */
    VETO(2, "不予同意"),

    /** 省局发回市局（前头签记录尽数划废，自市局重新攒起） */
    SEND_BACK(3, "发回市局");

    private final int code;
    private final String name;

    MoveAction(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static MoveAction of(Integer code) {
        if (code == null) {
            return null;
        }
        for (MoveAction action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        return null;
    }
}
