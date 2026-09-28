package com.fc.v2.model.custom;

/**
 * 台站迁移逐级核准的点印角色。
 * 第三处省局两名点印人各是各的角色，即便同一分钟各自落押也不并成一条。
 *
 * @author fuce
 * @date 2026-09-28
 */
public enum MoveRole {

    /** 第一处 台站：报件并落押 */
    STATION(0, "台站"),

    /** 第二处 市局：核新址是否符合探测环境要求 */
    CITY(1, "市局"),

    /** 第三处 省局甲：盯对比观测做满没有、历史序列能否接上 */
    PROV_COMPARE(2, "省局·对比观测与序列衔接"),

    /** 第三处 省局乙：盯先建后迁次序，新站未起不许拆旧站 */
    PROV_ORDER(2, "省局·先建后迁次序"),

    /** 第四处 国务院气象主管机构：批复 */
    NATIONAL(3, "国务院气象主管机构");

    private final int nodeNo;
    private final String roleName;

    MoveRole(int nodeNo, String roleName) {
        this.nodeNo = nodeNo;
        this.roleName = roleName;
    }

    public int getNodeNo() {
        return nodeNo;
    }

    public String getRoleName() {
        return roleName;
    }

    public static MoveRole of(String roleKey) {
        if (roleKey == null) {
            return null;
        }
        for (MoveRole role : values()) {
            if (role.name().equals(roleKey.trim())) {
                return role;
            }
        }
        return null;
    }
}
