package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fc.v2.mapper.auto.TMetpMoveBillMapper;
import com.fc.v2.mapper.auto.TMetpMoveNoticeMapper;
import com.fc.v2.mapper.auto.TMetpMoveSignMapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveActionReceipt;
import com.fc.v2.model.custom.MoveBillSubmitForm;
import com.fc.v2.service.ITMetpMoveBillService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 台站迁移变更逐级核准单 Service业务层处理（approval-chain 形状：多阶段签批）。
 *
 * 门牌号「画押」。四处顺序不可颠倒，前一处未点齐，件进不去下一处：
 * 0 台站（立单六料齐 → 台站落押）→ 1 市局（探测环境，一人）
 * → 2 省局（对比观测满期/历史序列、先建后迁次序，两名不同的人各签各的）
 * → 3 国务院气象主管机构（批复携文号）→ 已核讫。
 *
 * 落押、定否、驳回三大动作只此一门；本处几双手指凭画押记录自己攒出来；
 * 省局驳回到市局，轮次加一，旧签分毫不动、与新签不同堂；定否即冻单。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TMetpMoveBillServiceImpl implements ITMetpMoveBillService {

    /** 层：台站 */
    private static final int NODE_STATION = 0;
    /** 层：市局 */
    private static final int NODE_CITY = 1;
    /** 层：省局 */
    private static final int NODE_PROV = 2;
    /** 层：国务院气象主管机构 */
    private static final int NODE_NATION = 3;

    /** 同层任一人 */
    private static final int MODE_OR = 0;
    /** 同层两名点齐 */
    private static final int MODE_AND = 1;

    /** 在核 */
    private static final int STATUS_RUNNING = 0;
    /** 已核讫 */
    private static final int STATUS_PASS = 1;
    /** 不予同意（终态冻单） */
    private static final int STATUS_VETO = 2;

    private static final int NORMAL = 0;

    /** 每层的固定编排：职权次序、核准方式、名目。顺序即四处先后，不可颠倒。 */
    private static final Map<Integer, NodeDef> NODES = new LinkedHashMap<>();
    static {
        NODES.put(NODE_STATION, new NodeDef("台站", MODE_OR,
                new String[]{TMetpMoveSign.ROLE_STATION_APPLY},
                new String[]{"台站申报"}));
        NODES.put(NODE_CITY, new NodeDef("市局", MODE_OR,
                new String[]{TMetpMoveSign.ROLE_CITY_ENV},
                new String[]{"市局核探测环境"}));
        NODES.put(NODE_PROV, new NodeDef("省局", MODE_AND,
                new String[]{TMetpMoveSign.ROLE_PROV_COMPARE, TMetpMoveSign.ROLE_PROV_BUILD},
                new String[]{"省局核对比观测满期与历史序列衔接", "省局核先建后迁次序"}));
        NODES.put(NODE_NATION, new NodeDef("国务院气象主管机构", MODE_OR,
                new String[]{TMetpMoveSign.ROLE_NATION_APPROVE},
                new String[]{"国务院气象主管机构批复"}));
    }

    @Resource
    private TMetpMoveBillMapper metpMoveBillMapper;
    @Resource
    private TMetpMoveSignMapper metpMoveSignMapper;
    @Resource
    private TMetpMoveNoticeMapper metpMoveNoticeMapper;

    @Override
    public TMetpMoveBill selectTMetpMoveBillById(Long id) {
        return id == null ? null : this.metpMoveBillMapper.selectById(id);
    }

    @Override
    public List<TMetpMoveBill> selectMoveBillList(QueryWrapper<TMetpMoveBill> queryWrapper) {
        return this.metpMoveBillMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveActionReceipt submit(MoveBillSubmitForm form, String operator) {
        if (form == null) {
            return MoveActionReceipt.refused("SUBMIT", "申报材料缺项，报不上去");
        }
        if (StringUtils.isBlank(operator)) {
            return MoveActionReceipt.refused("SUBMIT", "画押人不明，报不上去");
        }
        // 第一处台站材料：申请事由、新址经纬度与海拔、下垫面、旧址现状，缺一项报不上去；
        // 新址编号与对比观测起止亦随单呈报——省局要据此核满期，通知要据此誊带。
        String missing = firstBlank(
                entry("申请事由", form.getApplyReason()),
                entry("新址编号", form.getNewSiteNo()),
                entry("新址经度", form.getNewLng()),
                entry("新址纬度", form.getNewLat()),
                entry("新址海拔", form.getNewAlt() == null ? null : form.getNewAlt().toPlainString()),
                entry("下垫面情形", form.getSurface()),
                entry("旧址观测场现状", form.getOldSiteStatus()),
                entry("对比观测起始", form.getCompareStart() == null ? null : "x"),
                entry("对比观测截止", form.getCompareEnd() == null ? null : "x"));
        if (missing != null) {
            return MoveActionReceipt.refused("SUBMIT", "台站材料缺项【" + missing + "】，报不上去");
        }
        if (!form.getCompareEnd().after(form.getCompareStart())) {
            return MoveActionReceipt.refused("SUBMIT", "对比观测截止不得早于起始");
        }

        long id = IdWorker.getId();
        TMetpMoveBill bill = new TMetpMoveBill();
        bill.setId(id);
        bill.setBillNo("QY" + id);
        bill.setSiteNo(trim(form.getSiteNo()));
        bill.setSiteName(trim(form.getSiteName()));
        bill.setApplyReason(trim(form.getApplyReason()));
        bill.setNewSiteNo(trim(form.getNewSiteNo()));
        bill.setNewLng(trim(form.getNewLng()));
        bill.setNewLat(trim(form.getNewLat()));
        bill.setNewAlt(form.getNewAlt());
        bill.setSurface(trim(form.getSurface()));
        bill.setOldSiteStatus(trim(form.getOldSiteStatus()));
        bill.setCompareStart(form.getCompareStart());
        bill.setCompareEnd(form.getCompareEnd());
        bill.setRoundNo(1);
        bill.setStatus(STATUS_RUNNING);
        bill.setDelFlag(NORMAL);
        // 台站落押本处即点齐，件离台站、停在市局门外。
        bill.setNodeNo(NODE_CITY);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        this.metpMoveBillMapper.insert(bill);

        insertSign(id, NODE_STATION, TMetpMoveSign.ROLE_STATION_APPLY, operator,
                TMetpMoveSign.ACTION_AGREE, "台站立单申报", 1);
        return snapshot(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveActionReceipt agree(Long id, String roleKey, String operator, String opinion, String approveNo) {
        TMetpMoveBill bill = load(id);
        if (bill == null) {
            return MoveActionReceipt.refused("AGREE", "核准单不存在");
        }
        if (StringUtils.isBlank(operator)) {
            return MoveActionReceipt.refused("AGREE", "画押人不明，此押不作数");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_VETO) {
            return MoveActionReceipt.refused("AGREE", "此件已落「不予同意」，各栏冻住，要改只许另起新件");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_PASS) {
            return MoveActionReceipt.refused("AGREE", "此件已核讫，无须再押");
        }
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        if (node == NODE_STATION) {
            return MoveActionReceipt.refused("AGREE", "台站尚未申报，件不在此处");
        }
        NodeDef def = NODES.get(node);
        int roleIdx = Arrays.asList(def.roles).indexOf(roleKey);
        if (roleIdx < 0) {
            return MoveActionReceipt.refused("AGREE", "件此刻停在「" + def.name + "」，" + roleKey + " 进不了这一处的页面");
        }
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();

        // 本职权本轮已有人落过押——本人重押、别人替签，皆不作数。
        if (findAgree(id, node, roleKey, round) != null) {
            return MoveActionReceipt.refused("AGREE", def.roleNames[roleIdx] + "一处本轮已点过，不得重押替签");
        }
        // 省局两名须是两双手：同一人替另一职权签，不作数。
        if (node == NODE_PROV) {
            for (String other : def.roles) {
                if (other.equals(roleKey)) {
                    continue;
                }
                TMetpMoveSign prior = findAgree(id, node, other, round);
                if (prior != null && operator.equals(prior.getSigner())) {
                    return MoveActionReceipt.refused("AGREE", "省局两项条件须两名不同的人各签各的，替签不作数");
                }
            }
        }
        // 第四处批复须携批准文号，无文号不落成。
        if (node == NODE_NATION && StringUtils.isBlank(approveNo)) {
            return MoveActionReceipt.refused("AGREE", "国务院气象主管机构批复须带批准文号");
        }

        insertSign(id, node, roleKey, operator, TMetpMoveSign.ACTION_AGREE, opinion, round);
        if (node == NODE_NATION) {
            bill.setApproveNo(approveNo.trim());
        }

        List<TMetpMoveSign> here = listAgree(id, node, round);
        if (here.size() < def.roles.length) {
            // 本处尚未点齐：件原地不动，已签数由记录回写。
            bill.setSignMode(def.mode);
            bill.setNeedCount(def.roles.length);
            bill.setSignCount(here.size());
            this.metpMoveBillMapper.updateById(bill);
            return snapshot(id);
        }

        // 本处点齐：顺序开门。四处走完则核讫；否则件进下一处。
        if (node == NODE_NATION) {
            bill.setNodeNo(NODE_NATION);
            bill.setSignMode(def.mode);
            bill.setNeedCount(def.roles.length);
            bill.setSignCount(here.size());
            bill.setStatus(STATUS_PASS);
        } else {
            NodeDef next = NODES.get(node + 1);
            bill.setNodeNo(node + 1);
            bill.setSignMode(next.mode);
            bill.setNeedCount(next.roles.length);
            bill.setSignCount(0);
            bill.setStatus(STATUS_RUNNING);
        }
        this.metpMoveBillMapper.updateById(bill);
        return snapshot(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveActionReceipt veto(Long id, String roleKey, String operator, String opinion) {
        TMetpMoveBill bill = load(id);
        if (bill == null) {
            return MoveActionReceipt.refused("VETO", "核准单不存在");
        }
        if (StringUtils.isBlank(operator)) {
            return MoveActionReceipt.refused("VETO", "画押人不明，此押不作数");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_VETO) {
            return MoveActionReceipt.refused("VETO", "此件性命已交代，各栏早冻住了");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_PASS) {
            return MoveActionReceipt.refused("VETO", "此件已核讫，落不下否");
        }
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        if (node == NODE_STATION) {
            return MoveActionReceipt.refused("VETO", "台站尚未申报，件不在此处");
        }
        NodeDef def = NODES.get(node);
        int roleIdx = Arrays.asList(def.roles).indexOf(roleKey);
        if (roleIdx < 0) {
            return MoveActionReceipt.refused("VETO", "件此刻停在「" + def.name + "」，这一处没有 " + roleKey + " 的印位");
        }
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        insertSign(id, node, roleKey, operator, TMetpMoveSign.ACTION_VETO, opinion, round);
        // 「不予同意」四字一落，件即刻终止冻住；已签数照旧回写，此后两边添不进一枚。
        bill.setStatus(STATUS_VETO);
        bill.setSignCount(listAgree(id, node, round).size());
        this.metpMoveBillMapper.updateById(bill);
        return snapshot(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveActionReceipt reject(Long id, String operator, String opinion) {
        TMetpMoveBill bill = load(id);
        if (bill == null) {
            return MoveActionReceipt.refused("REJECT", "核准单不存在");
        }
        if (StringUtils.isBlank(operator)) {
            return MoveActionReceipt.refused("REJECT", "画押人不明，驳回不作数");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_VETO) {
            return MoveActionReceipt.refused("REJECT", "此件已落「不予同意」，冻住了");
        }
        if (bill.getStatus() != null && bill.getStatus() == STATUS_PASS) {
            return MoveActionReceipt.refused("REJECT", "此件已核讫，无从驳回");
        }
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        if (node != NODE_PROV) {
            return MoveActionReceipt.refused("REJECT", "驳回只在省局一处；件此刻停在「" + NODES.get(node).name + "」");
        }
        int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
        if (findSign(id, node, TMetpMoveSign.ROLE_PROV_REJECT, TMetpMoveSign.ACTION_REJECT, round) != null) {
            return MoveActionReceipt.refused("REJECT", "本轮已经驳回过一次，件在发回市局的路上");
        }

        // 前头各轮签记录原样留痕、分毫不动，只凭轮次划开；轮次加一，自市局重新攒起。
        insertSign(id, NODE_PROV, TMetpMoveSign.ROLE_PROV_REJECT, operator,
                TMetpMoveSign.ACTION_REJECT, opinion, round);
        int newRound = round + 1;
        bill.setRoundNo(newRound);
        bill.setNodeNo(NODE_CITY);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        bill.setStatus(STATUS_RUNNING);
        this.metpMoveBillMapper.updateById(bill);
        return snapshot(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveActionReceipt generateNotice(Long id, String operator) {
        TMetpMoveBill bill = load(id);
        if (bill == null) {
            return MoveActionReceipt.refused("NOTICE", "核准单不存在");
        }
        if (bill.getStatus() == null || bill.getStatus() != STATUS_PASS) {
            return MoveActionReceipt.refused("NOTICE", "四处未走完、件未核讫，通知生不出来");
        }
        TMetpMoveNotice existing = findNoticeByBill(id);
        if (existing != null) {
            // 一座台站一份核准单对应一条通知：再点不另生第二条，把原条端回，仍是作数的回音。
            MoveActionReceipt r = snapshot(id);
            r.setAccepted(true);
            r.setAction("NOTICE");
            r.setMessage("此件的新址启用通知早已生成，一座台站只此一条，原条端回");
            r.setSigner(operator);
            r.setNotice(existing);
            return r;
        }

        TMetpMoveNotice notice = new TMetpMoveNotice();
        long noticeId = IdWorker.getId();
        notice.setId(noticeId);
        notice.setNoticeNo("TZ" + noticeId);
        notice.setBillId(bill.getId());
        notice.setBillNo(bill.getBillNo());
        notice.setSiteNo(bill.getSiteNo());
        // 新址编号、经纬度、对比观测起止、批准文号，统统从核准那一份带过去，不经第二个人誊。
        notice.setNewSiteNo(bill.getNewSiteNo());
        notice.setNewLng(bill.getNewLng());
        notice.setNewLat(bill.getNewLat());
        notice.setNewAlt(bill.getNewAlt());
        notice.setCompareStart(bill.getCompareStart());
        notice.setCompareEnd(bill.getCompareEnd());
        notice.setApproveNo(bill.getApproveNo());
        notice.setDelFlag(NORMAL);
        this.metpMoveNoticeMapper.insert(notice);

        MoveActionReceipt r = snapshot(id);
        r.setAccepted(true);
        r.setAction("NOTICE");
        r.setMessage("新址启用通知已据核准件生成");
        r.setSigner(operator);
        r.setNotice(notice);
        return r;
    }

    @Override
    public MoveActionReceipt snapshot(Long id) {
        TMetpMoveBill bill = load(id);
        if (bill == null) {
            return MoveActionReceipt.refused("SNAPSHOT", "核准单不存在");
        }
        MoveActionReceipt r = new MoveActionReceipt();
        r.setAccepted(true);
        r.setBillId(bill.getId());
        r.setBillNo(bill.getBillNo());
        r.setRoundNo(bill.getRoundNo());
        r.setBillStatus(bill.getStatus());
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        r.setNodeNo(node);
        boolean veto = bill.getStatus() != null && bill.getStatus() == STATUS_VETO;
        boolean pass = bill.getStatus() != null && bill.getStatus() == STATUS_PASS;
        r.setFrozen(veto);
        r.setFinished(pass);

        if (veto) {
            r.setNodeName(NODES.get(node).name + "（已落不予同意，件冻于此）");
            r.setMessage("件停在「" + NODES.get(node).name + "」，已冻单");
        } else if (pass) {
            r.setNodeName(NODES.get(node).name + "（已核讫）");
            r.setMessage("四处走完，件已核讫");
        } else {
            NodeDef def = NODES.get(node);
            r.setNodeName(def.name);
            int round = bill.getRoundNo() == null ? 1 : bill.getRoundNo();
            r.setSignMode(def.mode);
            r.setNeedCount(def.roles.length);
            List<TMetpMoveSign> here = listAgree(id, node, round);
            r.setSignCount(here.size());
            List<String> pending = new ArrayList<>();
            List<String> pendingNames = new ArrayList<>();
            for (int i = 0; i < def.roles.length; i++) {
                if (findAgree(id, node, def.roles[i], round) == null) {
                    pending.add(def.roles[i]);
                    pendingNames.add(def.roleNames[i]);
                }
            }
            r.setPendingRoles(pending);
            r.setPendingRoleNames(pendingNames);
            r.setAllRoles(new ArrayList<>(Arrays.asList(def.roles)));
            r.setAllRoleNames(new ArrayList<>(Arrays.asList(def.roleNames)));
            String tail;
            if (node == NODE_STATION) {
                tail = "台站材料齐了方可报上去";
            } else if (pending.isEmpty()) {
                tail = "本处手印已点齐，正开门放行";
            } else {
                tail = "本处还差：" + String.join("、", pendingNames);
            }
            r.setMessage("件此刻停在「" + def.name + "」（第" + round + "轮），" + tail);
        }
        return r;
    }

    @Override
    public List<TMetpMoveSign> traceSigns(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", id).eq("del_flag", NORMAL)
                // 逐步倒着核回来：轮次、层、时刻倒序；废签新签同册可辨，不并摆。
                .orderByDesc("round_no").orderByDesc("node_no")
                .orderByDesc("create_time").orderByDesc("id");
        return this.metpMoveSignMapper.selectList(qw);
    }

    // ------------------------------------------------------------------

    private TMetpMoveBill load(Long id) {
        if (id == null) {
            return null;
        }
        TMetpMoveBill bill = this.metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() != NORMAL)) {
            return null;
        }
        return bill;
    }

    private void insertSign(Long billId, int node, String roleKey, String operator,
                            String action, String opinion, int round) {
        TMetpMoveSign sign = new TMetpMoveSign();
        sign.setBillId(billId);
        sign.setNodeNo(node);
        sign.setRoleKey(roleKey);
        sign.setSigner(operator);
        sign.setAction(action);
        sign.setOpinion(StringUtils.isBlank(opinion) ? null : opinion.trim());
        sign.setRoundNo(round);
        sign.setDelFlag(NORMAL);
        // 同层同一职权一轮只许一枚有效押：库里唯一索引兜底，并发重押在此炸回滚。
        this.metpMoveSignMapper.insert(sign);
    }

    private TMetpMoveSign findAgree(Long billId, int node, String roleKey, int round) {
        return findSign(billId, node, roleKey, TMetpMoveSign.ACTION_AGREE, round);
    }

    private TMetpMoveSign findSign(Long billId, int node, String roleKey, String action, int round) {
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("node_no", node).eq("role_key", roleKey)
                .eq("action", action).eq("round_no", round).eq("del_flag", NORMAL);
        return this.metpMoveSignMapper.selectOne(qw);
    }

    private List<TMetpMoveSign> listAgree(Long billId, int node, int round) {
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("node_no", node).eq("round_no", round)
                .eq("action", TMetpMoveSign.ACTION_AGREE).eq("del_flag", NORMAL);
        return this.metpMoveSignMapper.selectList(qw);
    }

    private TMetpMoveNotice findNoticeByBill(Long billId) {
        QueryWrapper<TMetpMoveNotice> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("del_flag", NORMAL);
        return this.metpMoveNoticeMapper.selectOne(qw);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String[] entry(String label, String value) {
        return new String[]{label, value};
    }

    private static String firstBlank(String[]... entries) {
        for (String[] e : entries) {
            if (StringUtils.isBlank(e[1])) {
                return e[0];
            }
        }
        return null;
    }

    /** 一层的固定编排 */
    private static class NodeDef {
        private final String name;
        private final int mode;
        private final String[] roles;
        private final String[] roleNames;

        private NodeDef(String name, int mode, String[] roles, String[] roleNames) {
            this.name = name;
            this.mode = mode;
            this.roles = roles;
            this.roleNames = roleNames;
        }
    }
}
