package com.fc.v2.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fc.v2.mapper.auto.TMetpMoveBillMapper;
import com.fc.v2.mapper.auto.TMetpMoveNoticeMapper;
import com.fc.v2.mapper.auto.TMetpMoveSignMapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveAction;
import com.fc.v2.model.custom.MoveEcho;
import com.fc.v2.model.custom.MoveRole;
import com.fc.v2.service.ITMetpMoveBillService;
import com.fc.v2.shiro.util.ShiroUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 台站迁移变更逐级核准单 Service业务层处理（approval-chain 形状）。
 *
 * 规矩：
 * 1. 四层顺序不可颠倒，前一处的人没点齐，件进不去下一层；
 * 2. 第三处省局两人各签各的角色，同一分钟也是两条；同一人替另一角色点印不作数；
 * 3. 省局发回市局，前头（市局及以后）签记录尽数划废，轮次加一，自市局重新攒起，废签不与新签同堂；
 * 4. 任一层落「不予同意」即终局冻死，各栏冻住，要改另起新件；
 * 5. 落押人一律取当前登录人，门脸递来的签人不作数；
 * 6. 屏上所摆与行内所存同锅舀饭：签计数、欠手、台账全部由签记录自己攒，不收手填。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TMetpMoveBillServiceImpl implements ITMetpMoveBillService {

    /** 节点：0台站 1市局 2省局 3国务院气象主管机构 */
    private static final int NODE_STATION = 0;
    private static final int NODE_CITY = 1;
    private static final int NODE_PROV = 2;
    private static final int NODE_NATIONAL = 3;

    /** 同层核准方式 0任一人 1两名点齐 */
    private static final int MODE_OR = 0;
    private static final int MODE_AND = 1;

    /** 报批情形 0在核 1已核讫 2已打回重审 3不予同意(终局) */
    private static final int STATUS_RUNNING = 0;
    private static final int STATUS_PASS = 1;
    private static final int STATUS_SENT_BACK = 2;
    private static final int STATUS_VETO = 3;

    /** 每层点印角色配置（顺序即欠手展示顺序） */
    private static final Map<Integer, List<MoveRole>> NODE_ROLES = new LinkedHashMap<>();
    static {
        NODE_ROLES.put(NODE_STATION, Arrays.asList(MoveRole.STATION));
        NODE_ROLES.put(NODE_CITY, Arrays.asList(MoveRole.CITY));
        NODE_ROLES.put(NODE_PROV, Arrays.asList(MoveRole.PROV_COMPARE, MoveRole.PROV_ORDER));
        NODE_ROLES.put(NODE_NATIONAL, Arrays.asList(MoveRole.NATIONAL));
    }

    private final ObjectMapper snapshotMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Resource
    private TMetpMoveBillMapper metpMoveBillMapper;

    @Resource
    private TMetpMoveSignMapper metpMoveSignMapper;

    @Resource
    private TMetpMoveNoticeMapper metpMoveNoticeMapper;

    /** 当前落押人：只认登录态，门脸无权代传，便于单测替身。 */
    protected String currentSigner() {
        return ShiroUtils.getLoginName();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveEcho createBill(TMetpMoveBill form) {
        if (form == null) {
            return MoveEcho.deny("申报件不存在，报不上去");
        }
        // 第一处门禁：申请四要件（经纬度海拔合为新址坐标一栏）+ 新址编号 + 对比观测起止，缺一不报。
        List<String> missing = new ArrayList<>();
        if (StrUtil.isBlank(form.getSiteNo())) {
            missing.add("台站编号");
        }
        if (StrUtil.isBlank(form.getSiteName())) {
            missing.add("台站名称");
        }
        if (StrUtil.isBlank(form.getReason())) {
            missing.add("申请事由");
        }
        if (StrUtil.isBlank(form.getNewSiteNo())) {
            missing.add("新址编号");
        }
        if (form.getNewLng() == null) {
            missing.add("新址经度");
        }
        if (form.getNewLat() == null) {
            missing.add("新址纬度");
        }
        if (form.getNewAlt() == null) {
            missing.add("新址海拔");
        }
        if (StrUtil.isBlank(form.getSurface())) {
            missing.add("下垫面情形");
        }
        if (StrUtil.isBlank(form.getOldFieldStatus())) {
            missing.add("旧址观测场现状");
        }
        if (form.getCompareStart() == null) {
            missing.add("对比观测起始");
        }
        if (form.getCompareEnd() == null) {
            missing.add("对比观测截止");
        }
        if (!missing.isEmpty()) {
            return MoveEcho.deny("第一处要件不齐，缺：" + String.join("、", missing) + "，报不上去");
        }
        if (form.getCompareEnd().before(form.getCompareStart())) {
            return MoveEcho.deny("对比观测截止早于起始，时序不通，报不上去");
        }

        String signer = currentSigner();
        if (StrUtil.isBlank(signer)) {
            return MoveEcho.deny("取当前落押人失败，件立不起来");
        }

        form.setId(IdWorker.getId());
        form.setBillNo("QP-" + form.getId());
        form.setRoundNo(1);
        form.setNodeNo(NODE_CITY);
        form.setSignMode(MODE_OR);
        form.setNeedCount(1);
        form.setSignCount(0);
        form.setStatus(STATUS_RUNNING);
        form.setDelFlag(0);
        if (form.getNewStationBuilt() == null) {
            form.setNewStationBuilt(0);
        }
        form.setApproveDocNo(null);
        metpMoveBillMapper.insert(form);

        // 台站报件本身即第一处落印，要件齐了才算点齐，件随即进市局页面。
        TMetpMoveSign stationSign = newSign(form, NODE_STATION, MoveRole.STATION,
                signer, MoveAction.AGREE, "台站报件，四要件齐备", snapshot(form));
        metpMoveSignMapper.insert(stationSign);

        return echo(form, "第一处（台站）要件齐备，已报市局核探测环境");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveEcho sign(Long id, MoveRole role, String opinion, String approveDocNo) {
        TMetpMoveBill bill = metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return MoveEcho.deny("核准件不存在");
        }
        if (role == null) {
            return MoveEcho.deny("未说明以哪一处的角色点印");
        }
        MoveEcho gate = checkTouchable(bill, role);
        if (gate != null) {
            return gate;
        }
        String signer = currentSigner();
        if (StrUtil.isBlank(signer)) {
            return MoveEcho.deny("取当前落押人失败，印落不下去");
        }

        int node = role.getNodeNo();
        int round = bill.getRoundNo();
        List<TMetpMoveSign> nodeSigns = validNodeSigns(bill.getId(), round, node);

        // 这一角色已经点过，不许再点。
        for (TMetpMoveSign s : nodeSigns) {
            if (s.getRoleKey().equals(role.name())) {
                return MoveEcho.deny("「" + role.getRoleName() + "」这一印已落，不可重复落押");
            }
        }
        // 省局两名点齐：同一只手不得替另一角色再点，替签不作数。
        if (nodeMode(node) == MODE_AND) {
            for (TMetpMoveSign s : nodeSigns) {
                if (signer.equals(s.getSigner())) {
                    return MoveEcho.deny("同一人已以「" + MoveRole.of(s.getRoleKey()).getRoleName()
                            + "」落过印，替另一角色点印不作数");
                }
            }
        }
        // 省局次序这一印：新站未起不许点（先建后迁，新站未起不许拆旧站）。
        if (role == MoveRole.PROV_ORDER && (bill.getNewStationBuilt() == null || bill.getNewStationBuilt() != 1)) {
            return MoveEcho.deny("新站尚未建起，先建后迁次序这一印不能落（新站未起不许拆旧站）");
        }
        // 第四处批复须带批准文号，通知全靠它带过去。
        if (node == NODE_NATIONAL && StrUtil.isBlank(approveDocNo)) {
            return MoveEcho.deny("第四处批复须落批准文号，无文号件不能核讫");
        }

        String snapshot = snapshot(bill);
        metpMoveSignMapper.insert(newSign(bill, node, role, signer, MoveAction.AGREE, opinion, snapshot));

        nodeSigns = validNodeSigns(bill.getId(), round, node);
        bill.setSignCount(nodeSigns.size());
        if (StrUtil.isNotBlank(approveDocNo) && node == NODE_NATIONAL) {
            bill.setApproveDocNo(approveDocNo.trim());
        }

        if (nodeSigns.size() >= nodeNeed(node)) {
            // 本处点齐，放行下一处；第四处点齐即核讫。
            if (node == NODE_NATIONAL) {
                bill.setStatus(STATUS_PASS);
            } else {
                int next = node + 1;
                bill.setNodeNo(next);
                bill.setSignMode(nodeMode(next));
                bill.setNeedCount(nodeNeed(next));
                bill.setSignCount(0);
                if (bill.getStatus() != null && bill.getStatus() == STATUS_SENT_BACK) {
                    bill.setStatus(STATUS_RUNNING);
                }
            }
        }
        metpMoveBillMapper.updateById(bill);

        return echo(bill, node == NODE_NATIONAL && bill.getStatus() == STATUS_PASS
                ? "四处点齐，核准件已核讫，可一键生成新址启用通知"
                : "「" + role.getRoleName() + "」已落押");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveEcho veto(Long id, MoveRole role, String opinion) {
        TMetpMoveBill bill = metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return MoveEcho.deny("核准件不存在");
        }
        if (role == null) {
            return MoveEcho.deny("未说明以哪一处的角色定否");
        }
        MoveEcho gate = checkTouchable(bill, role);
        if (gate != null) {
            return gate;
        }
        String signer = currentSigner();
        if (StrUtil.isBlank(signer)) {
            return MoveEcho.deny("取当前落押人失败，定否落不下去");
        }

        // 定否即终局：落一条不予同意，件冻死，此后谁也添不进一枚。
        metpMoveSignMapper.insert(newSign(bill, role.getNodeNo(), role, signer,
                MoveAction.VETO, opinion, snapshot(bill)));
        bill.setStatus(STATUS_VETO);
        metpMoveBillMapper.updateById(bill);

        MoveEcho echo = echo(bill, "「" + role.getRoleName() + "」落「不予同意」，此件终局，各栏冻住；要改只许另起新件");
        echo.getPendingRoles().clear();
        return echo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveEcho sendBack(Long id, String opinion) {
        TMetpMoveBill bill = metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return MoveEcho.deny("核准件不存在");
        }
        Integer status = bill.getStatus();
        if (status != null && status == STATUS_VETO) {
            return MoveEcho.deny("件已「不予同意」终局，不能再发回");
        }
        if (status != null && status == STATUS_PASS) {
            return MoveEcho.deny("件已核讫，不能发回");
        }
        if (bill.getNodeNo() == null || bill.getNodeNo() != NODE_PROV) {
            return MoveEcho.deny("件此刻不在省局，发回市局这一手只有省局使得出");
        }
        String signer = currentSigner();
        if (StrUtil.isBlank(signer)) {
            return MoveEcho.deny("取当前落押人失败，驳回落不下去");
        }

        int round = bill.getRoundNo();
        // 前头市局及以后本轮落的签尽数划废（台站报件仍是同一件的底子，留着）；
        // 只划有效标记、记录本身一字不动，废签与新签不同堂并摆。
        List<TMetpMoveSign> roundSigns = listRoundSigns(bill.getId(), round);
        Date now = new Date();
        for (TMetpMoveSign s : roundSigns) {
            if (s.getNodeNo() >= NODE_CITY && s.getValidFlag() != null && s.getValidFlag() == 1) {
                s.setValidFlag(0);
                s.setUpdateTime(now);
                metpMoveSignMapper.updateById(s);
            }
        }
        metpMoveSignMapper.insert(newSign(bill, NODE_PROV, MoveRole.PROV_COMPARE, signer,
                MoveAction.SEND_BACK, StrUtil.isBlank(opinion) ? "对比观测报告毛糙，发回市局" : opinion,
                snapshot(bill)));

        // 自市局那一阶重新攒起：件停在市局页面，轮次加一。
        bill.setRoundNo(round + 1);
        bill.setNodeNo(NODE_CITY);
        bill.setSignMode(MODE_OR);
        bill.setNeedCount(1);
        bill.setSignCount(0);
        bill.setStatus(STATUS_SENT_BACK);
        metpMoveBillMapper.updateById(bill);

        return echo(bill, "省局发回市局：市局及以后签记录尽数划废，自市局重新攒起（第" + bill.getRoundNo() + "轮）");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MoveEcho generateNotice(Long id) {
        TMetpMoveBill bill = metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return MoveEcho.deny("核准件不存在");
        }
        if (bill.getStatus() == null || bill.getStatus() != STATUS_PASS) {
            return MoveEcho.deny("件未核讫，启用通知还生不出来");
        }
        TMetpMoveNotice exist = selectNoticeByBillId(bill.getId());
        if (exist != null) {
            // 一座台站对应一条：重复叩门只把原条端出来，不许另造。
            MoveEcho echo = echo(bill, "启用通知早已生成，仍给这一条（一座台站只一条）");
            echo.getPendingRoles().clear();
            echo.setNotice(exist);
            return echo;
        }
        QueryWrapper<TMetpMoveNotice> siteQuery = new QueryWrapper<>();
        siteQuery.eq("del_flag", 0).eq("site_no", bill.getSiteNo());
        Integer siteCount = metpMoveNoticeMapper.selectCount(siteQuery);
        if (siteCount != null && siteCount > 0) {
            return MoveEcho.deny("该台站已有新址启用通知，一座台站只许一条");
        }

        TMetpMoveNotice notice = new TMetpMoveNotice();
        notice.setId(IdWorker.getId());
        notice.setNoticeNo("QZ-" + notice.getId());
        notice.setBillId(bill.getId());
        notice.setBillNo(bill.getBillNo());
        // 以下各项统统从核准那一份带过去，不经第二个人誊。
        notice.setSiteNo(bill.getSiteNo());
        notice.setSiteName(bill.getSiteName());
        notice.setNewSiteNo(bill.getNewSiteNo());
        notice.setNewLng(bill.getNewLng());
        notice.setNewLat(bill.getNewLat());
        notice.setNewAlt(bill.getNewAlt());
        notice.setCompareStart(bill.getCompareStart());
        notice.setCompareEnd(bill.getCompareEnd());
        notice.setApproveDocNo(bill.getApproveDocNo());
        notice.setDelFlag(0);
        metpMoveNoticeMapper.insert(notice);

        MoveEcho echo = echo(bill, "新址启用通知已由核准件一键带出，编号、经纬度、对比观测起止、批准文号均自核准件携来，无人誊抄");
        echo.getPendingRoles().clear();
        echo.setNotice(notice);
        return echo;
    }

    @Override
    public TMetpMoveBill selectTMetpMoveBillById(Long id) {
        return metpMoveBillMapper.selectById(id);
    }

    @Override
    public MoveEcho detail(Long id) {
        TMetpMoveBill bill = metpMoveBillMapper.selectById(id);
        if (bill == null || (bill.getDelFlag() != null && bill.getDelFlag() == 1)) {
            return null;
        }
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        int status = bill.getStatus() == null ? STATUS_RUNNING : bill.getStatus();
        String message;
        if (status == STATUS_PASS) {
            message = "件已核讫";
        } else if (status == STATUS_VETO) {
            message = "件已「不予同意」终局";
        } else if (status == STATUS_SENT_BACK) {
            message = "省局已发回，自市局重新攒起（第" + bill.getRoundNo() + "轮）";
        } else {
            message = "件此刻停在第" + (node + 1) + "处";
        }
        MoveEcho echo = echo(bill, message);
        echo.setNotice(selectNoticeByBillId(bill.getId()));
        return echo;
    }

    @Override
    public List<TMetpMoveBill> selectList(QueryWrapper<TMetpMoveBill> queryWrapper) {
        return metpMoveBillMapper.selectList(queryWrapper);
    }

    @Override
    public List<TMetpMoveSign> listValidSigns(Long billId, int roundNo) {
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId)
                .eq("round_no", roundNo)
                .eq("valid_flag", 1)
                .eq("del_flag", 0)
                .orderByAsc("node_no").orderByAsc("sign_time").orderByAsc("id");
        return metpMoveSignMapper.selectList(qw);
    }

    @Override
    public List<TMetpMoveSign> traceBack(Long billId) {
        // 逐步倒着核回来：当前有效链上每一处取其最后一轮所落的全部同意印——
        // 第三处省局是两只手，两条都要在，一条不丢；按层 3→0、同层落押时刻倒序。
        // 发回划废的旧签(valid=0)与发回动作本身不进核准链；旧印仍在各轮台账里留着，一字未动。
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId)
                .eq("valid_flag", 1)
                .eq("del_flag", 0)
                .eq("action", MoveAction.AGREE.getCode());
        List<TMetpMoveSign> all = metpMoveSignMapper.selectList(qw);
        Map<Integer, Integer> latestRoundByNode = new LinkedHashMap<>();
        for (TMetpMoveSign s : all) {
            Integer r = latestRoundByNode.get(s.getNodeNo());
            if (r == null || s.getRoundNo() > r) {
                latestRoundByNode.put(s.getNodeNo(), s.getRoundNo());
            }
        }
        List<TMetpMoveSign> chain = new ArrayList<>();
        for (TMetpMoveSign s : all) {
            if (s.getRoundNo() == latestRoundByNode.get(s.getNodeNo())) {
                chain.add(s);
            }
        }
        chain.sort((a, b) -> {
            int byNode = Integer.compare(b.getNodeNo(), a.getNodeNo());
            if (byNode != 0) {
                return byNode;
            }
            int byTime = b.getSignTime().compareTo(a.getSignTime());
            return byTime != 0 ? byTime : Long.compare(b.getId(), a.getId());
        });
        return chain;
    }

    @Override
    public TMetpMoveNotice selectNoticeByBillId(Long billId) {
        QueryWrapper<TMetpMoveNotice> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("del_flag", 0).last("limit 1");
        List<TMetpMoveNotice> list = metpMoveNoticeMapper.selectList(qw);
        return list.isEmpty() ? null : list.get(0);
    }

    // ---------------- 内业 ----------------

    /** 层门禁：终局件、不在此层的角色一律拦在页面外。 */
    private MoveEcho checkTouchable(TMetpMoveBill bill, MoveRole role) {
        Integer status = bill.getStatus();
        if (status != null && status == STATUS_VETO) {
            return MoveEcho.deny("件已「不予同意」终局冻死，两边再不能添进一枚");
        }
        if (status != null && status == STATUS_PASS) {
            return MoveEcho.deny("件已核讫，无路可走，要改只许另起新件");
        }
        int currentNode = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        if (role.getNodeNo() != currentNode) {
            return MoveEcho.deny("件此刻停在第" + (currentNode + 1) + "处，"
                    + "「" + role.getRoleName() + "」这一处的页面进不去（四处顺序不可颠倒）");
        }
        return null;
    }

    private TMetpMoveSign newSign(TMetpMoveBill bill, int node, MoveRole role, String signer,
                                  MoveAction action, String opinion, String snapshot) {
        TMetpMoveSign s = new TMetpMoveSign();
        s.setId(IdWorker.getId());
        s.setBillId(bill.getId());
        s.setRoundNo(bill.getRoundNo());
        s.setNodeNo(node);
        s.setRoleKey(role.name());
        s.setSigner(signer);
        s.setAction(action.getCode());
        s.setOpinion(opinion);
        s.setSnapshot(snapshot);
        s.setValidFlag(1);
        s.setSignTime(new Date());
        s.setDelFlag(0);
        return s;
    }

    private List<TMetpMoveSign> validNodeSigns(Long billId, int round, int node) {
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId)
                .eq("round_no", round)
                .eq("node_no", node)
                .eq("valid_flag", 1)
                .eq("del_flag", 0)
                .eq("action", MoveAction.AGREE.getCode())
                .orderByAsc("sign_time").orderByAsc("id");
        return metpMoveSignMapper.selectList(qw);
    }

    private List<TMetpMoveSign> listRoundSigns(Long billId, int round) {
        QueryWrapper<TMetpMoveSign> qw = new QueryWrapper<>();
        qw.eq("bill_id", billId).eq("round_no", round).eq("del_flag", 0);
        return metpMoveSignMapper.selectList(qw);
    }

    private int nodeMode(int node) {
        return node == NODE_PROV ? MODE_AND : MODE_OR;
    }

    private int nodeNeed(int node) {
        return NODE_ROLES.get(node).size();
    }

    private String snapshot(TMetpMoveBill bill) {
        try {
            return snapshotMapper.writeValueAsString(bill);
        } catch (Exception e) {
            throw new IllegalStateException("落定快照攒不出来：" + e.getMessage(), e);
        }
    }

    /** 回音：单据、尚欠角色、当前轮签记录全由账上现攒，门脸只摆这一锅。 */
    private MoveEcho echo(TMetpMoveBill bill, String message) {
        MoveEcho echo = MoveEcho.ok(message);
        echo.setBill(bill);
        int node = bill.getNodeNo() == null ? NODE_STATION : bill.getNodeNo();
        Integer status = bill.getStatus();
        if (status != null && status == STATUS_PASS) {
            // 核讫：摆当前有效核准链（四处、省局两手），废签不在其列。
            echo.setSigns(traceBack(bill.getId()));
            return echo;
        }
        if (status != null && status == STATUS_VETO) {
            // 否决：摆本轮记录，含那条「不予同意」。
            echo.setSigns(listValidSigns(bill.getId(), bill.getRoundNo()));
            return echo;
        }
        List<TMetpMoveSign> nodeSigns = validNodeSigns(bill.getId(), bill.getRoundNo(), node);
        List<String> done = new ArrayList<>();
        for (TMetpMoveSign s : nodeSigns) {
            done.add(s.getRoleKey());
        }
        for (MoveRole role : NODE_ROLES.get(node)) {
            if (!done.contains(role.name())) {
                echo.getPendingRoles().add(new com.fc.v2.model.custom.MovePendingRole(role));
            }
        }
        echo.setSigns(listValidSigns(bill.getId(), bill.getRoundNo()));
        return echo;
    }
}
