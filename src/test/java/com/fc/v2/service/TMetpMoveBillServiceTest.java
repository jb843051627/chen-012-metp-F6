package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TMetpMoveBillMapper;
import com.fc.v2.mapper.auto.TMetpMoveNoticeMapper;
import com.fc.v2.mapper.auto.TMetpMoveSignMapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveActionReceipt;
import com.fc.v2.model.custom.MoveBillSubmitForm;
import com.fc.v2.service.impl.TMetpMoveBillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 「画押」服务层规矩核验。Mapper 以内存账册顶替：
 * 屏上所摆、行内所存同锅舀饭——本处几双手全凭画押记录自己攒出来。
 */
class TMetpMoveBillServiceTest {

    private TMetpMoveBillServiceImpl service;
    private TMetpMoveBillMapper billMapper;
    private TMetpMoveSignMapper signMapper;
    private TMetpMoveNoticeMapper noticeMapper;

    private final Map<Long, TMetpMoveBill> bills = new LinkedHashMap<>();
    private final List<TMetpMoveSign> signs = new ArrayList<>();
    private final Map<Long, TMetpMoveNotice> notices = new LinkedHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong idSeq = new java.util.concurrent.atomic.AtomicLong(1000);

    private static final Date D1 = new Date(1700000000000L);
    private static final Date D2 = new Date(1700000000000L + 60L * 24 * 3600 * 1000);

    @BeforeEach
    void setUp() throws Exception {
        service = new TMetpMoveBillServiceImpl();
        billMapper = mock(TMetpMoveBillMapper.class);
        signMapper = mock(TMetpMoveSignMapper.class);
        noticeMapper = mock(TMetpMoveNoticeMapper.class);
        inject(service, "metpMoveBillMapper", billMapper);
        inject(service, "metpMoveSignMapper", signMapper);
        inject(service, "metpMoveNoticeMapper", noticeMapper);

        lenient().when(billMapper.selectById(any())).thenAnswer(inv -> bills.get(inv.getArgument(0)));
        lenient().when(billMapper.insert(any())).thenAnswer(inv -> {
            TMetpMoveBill b = inv.getArgument(0);
            bills.put(b.getId(), b);
            return 1;
        });
        lenient().when(billMapper.updateById(any())).thenAnswer(inv -> {
            TMetpMoveBill b = inv.getArgument(0);
            bills.put(b.getId(), b);
            return 1;
        });
        lenient().when(billMapper.selectList(any())).thenAnswer(inv -> new ArrayList<>(bills.values()));

        lenient().when(signMapper.insert(any())).thenAnswer(inv -> {
            TMetpMoveSign s = inv.getArgument(0);
            // 模拟 MP 主键回填与插入填充，免得画押记录无号无时
            s.setId(idSeq.incrementAndGet());
            s.setCreateTime(new Date(1700000000000L + signs.size() * 1000L));
            signs.add(s);
            return 1;
        });
        lenient().when(signMapper.selectOne(any())).thenAnswer(inv -> {
            QueryWrapper<?> qw0 = inv.getArgument(0);
            Map<String, Object> cv = columnValues(qw0);
            if (!cv.containsKey("bill_id")) {
                return null;
            }
            long billId = ((Number) cv.get("bill_id")).longValue();
            String fRole = (String) cv.get("role_key");
            String fAction = (String) cv.get("action");
            int fn = ((Number) cv.get("node_no")).intValue();
            int fr = ((Number) cv.get("round_no")).intValue();
            return signs.stream().filter(s -> s.getBillId() == billId && s.getNodeNo() == fn
                    && fRole.equals(s.getRoleKey()) && fAction.equals(s.getAction())
                    && s.getRoundNo() == fr).findFirst().orElse(null);
        });
        lenient().when(signMapper.selectList(any())).thenAnswer(inv -> {
            QueryWrapper<?> qw0 = inv.getArgument(0);
            Map<String, Object> cv = columnValues(qw0);
            if (!cv.containsKey("bill_id")) {
                return new ArrayList<TMetpMoveSign>();
            }
            long billId = ((Number) cv.get("bill_id")).longValue();
            if (qw0.getSqlSegment().toUpperCase().contains("ORDER BY")) {
                // trace：按 SQL 同款次序倒排
                List<TMetpMoveSign> out = new ArrayList<>();
                for (TMetpMoveSign s : signs) {
                    if (s.getBillId() == billId) {
                        out.add(s);
                    }
                }
                out.sort((x, y) -> {
                    int c = Integer.compare(y.getRoundNo(), x.getRoundNo());
                    if (c != 0) {
                        return c;
                    }
                    c = Integer.compare(y.getNodeNo(), x.getNodeNo());
                    if (c != 0) {
                        return c;
                    }
                    long tx = x.getCreateTime() == null ? 0L : x.getCreateTime().getTime();
                    long ty = y.getCreateTime() == null ? 0L : y.getCreateTime().getTime();
                    return Long.compare(ty, tx);
                });
                return out;
            }
            // listAgree：节点、轮次、动作皆按列认
            int node = ((Number) cv.get("node_no")).intValue();
            int round = ((Number) cv.get("round_no")).intValue();
            String action = (String) cv.get("action");
            List<TMetpMoveSign> out = new ArrayList<>();
            for (TMetpMoveSign s : signs) {
                if (s.getBillId() == billId && s.getNodeNo() == node
                        && s.getRoundNo() == round && action.equals(s.getAction())) {
                    out.add(s);
                }
            }
            return out;
        });

        lenient().when(noticeMapper.insert(any())).thenAnswer(inv -> {
            TMetpMoveNotice n = inv.getArgument(0);
            notices.put(n.getId(), n);
            return 1;
        });
        lenient().when(noticeMapper.selectOne(any())).thenAnswer(inv -> {
            Map<String, Object> cv = columnValues(inv.getArgument(0));
            if (!cv.containsKey("bill_id")) {
                return null;
            }
            long billId = ((Number) cv.get("bill_id")).longValue();
            return notices.values().stream().filter(n -> n.getBillId() == billId).findFirst().orElse(null);
        });
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = TMetpMoveBillServiceImpl.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static final java.util.regex.Pattern COL_PARAM =
            java.util.regex.Pattern.compile("([a-z_]+)\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)\\}");

    /** 从 SQL 段解析「列 → 值」，不依赖 MP 参数槽次序（3.4.2 底层是 HashMap）。 */
    private static Map<String, Object> columnValues(QueryWrapper<?> qw) {
        String seg = qw.getSqlSegment();
        Map<String, Object> out = new LinkedHashMap<>();
        java.util.regex.Matcher m = COL_PARAM.matcher(seg);
        while (m.find()) {
            out.put(m.group(1), qw.getParamNameValuePairs().get(m.group(2)));
        }
        return out;
    }

    private MoveBillSubmitForm validForm() {
        MoveBillSubmitForm f = new MoveBillSubmitForm();
        f.setSiteNo("TQ00");
        f.setSiteName("临川国家基本气象站");
        f.setApplyReason("城市扩建探测环境受遮挡，申请迁移");
        f.setNewSiteNo("TQ00N");
        f.setNewLng("116.21");
        f.setNewLat("28.07");
        f.setNewAlt(new BigDecimal("47.30"));
        f.setSurface("均匀草地，四周开阔");
        f.setOldSiteStatus("旧观测场尚在观测，未拆");
        f.setCompareStart(D1);
        f.setCompareEnd(D2);
        return f;
    }

    private Long submitOk() {
        MoveActionReceipt r = service.submit(validForm(), "stationZhang");
        assertTrue(r.isAccepted(), r::getMessage);
        assertEquals(1, r.getNodeNo(), "台站落押后件应停在市局");
        return r.getBillId();
    }

    // ---- 第一处：台站材料缺一项报不上去 ----

    @Test
    void submit_missingAnyMaterial_refused() {
        String[][] blanks = {
                {"applyReason"}, {"newSiteNo"}, {"newLng"}, {"newLat"}, {"surface"}, {"oldSiteStatus"}
        };
        for (String[] field : blanks) {
            signs.clear();
            bills.clear();
            MoveBillSubmitForm f = validForm();
            setBlank(f, field[0]);
            MoveActionReceipt r = service.submit(f, "stationZhang");
            assertFalse(r.isAccepted(), field[0] + " 缺项竟报上去了");
            assertTrue(r.getMessage().contains(field[0]) || r.getMessage().contains("缺"), r.getMessage());
        }
        // 海拔缺失
        MoveBillSubmitForm noAlt = validForm();
        noAlt.setNewAlt(null);
        assertFalse(service.submit(noAlt, "s").isAccepted());
        // 对比观测起止缺失
        MoveBillSubmitForm noStart = validForm();
        noStart.setCompareStart(null);
        assertFalse(service.submit(noStart, "s").isAccepted());
        // 起迄颠倒
        MoveBillSubmitForm badRange = validForm();
        badRange.setCompareStart(D2);
        badRange.setCompareEnd(D1);
        assertFalse(service.submit(badRange, "s").isAccepted());
        // 画押人不明
        assertFalse(service.submit(validForm(), "  ").isAccepted());
        assertEquals(0, bills.size(), "拒收不得立单");
    }

    private void setBlank(MoveBillSubmitForm f, String field) {
        switch (field) {
            case "applyReason": f.setApplyReason(" "); break;
            case "newSiteNo": f.setNewSiteNo(""); break;
            case "newLng": f.setNewLng(" "); break;
            case "newLat": f.setNewLat(""); break;
            case "surface": f.setSurface(" "); break;
            case "oldSiteStatus": f.setOldSiteStatus(""); break;
            default: break;
        }
    }

    @Test
    void submit_ok_stationSignAndEnterCity() {
        Long id = submitOk();
        TMetpMoveBill bill = bills.get(id);
        assertEquals(1, bill.getRoundNo());
        assertEquals(0, bill.getStatus());
        assertEquals(1, bill.getNodeNo());
        assertEquals(1, bill.getNeedCount());
        assertEquals(0, bill.getSignCount(), "市局尚无人签，已签数须为 0");

        TMetpMoveSign s = signs.get(0);
        assertEquals(0, s.getNodeNo());
        assertEquals(TMetpMoveSign.ROLE_STATION_APPLY, s.getRoleKey());
        assertEquals(TMetpMoveSign.ACTION_AGREE, s.getAction());
        assertEquals("stationZhang", s.getSigner(), "画押人须是登录身份");
        assertEquals(1, s.getRoundNo());

        MoveActionReceipt snap = service.snapshot(id);
        assertEquals("市局", snap.getNodeName());
        assertTrue(snap.getPendingRoles().contains(TMetpMoveSign.ROLE_CITY_ENV));
    }

    // ---- 四处顺序不可颠倒，前处没点齐进不去下一处 ----

    @Test
    void wrongNodeRole_cannotEnterPage() {
        Long id = submitOk();
        // 件在市局，省局/国家的印位按不下去
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", null, null).isAccepted());
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_NATION_APPROVE, "nation", null, "GW-1").isAccepted());
        assertEquals(1, bills.get(id).getNodeNo());
        // 画押人不明
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "", null, null).isAccepted());
        // 件不存在
        assertFalse(service.agree(999L, TMetpMoveSign.ROLE_CITY_ENV, "u", null, null).isAccepted());
    }

    @Test
    void citySign_thenEnterProvince() {
        Long id = submitOk();
        MoveActionReceipt r = service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", "探测环境合格", null);
        assertTrue(r.isAccepted(), r::getMessage);
        assertEquals(2, r.getNodeNo());
        assertEquals(2, r.getNeedCount());
        assertEquals(0, r.getSignCount());
        assertTrue(r.getPendingRoles().contains(TMetpMoveSign.ROLE_PROV_COMPARE));
        assertTrue(r.getPendingRoles().contains(TMetpMoveSign.ROLE_PROV_BUILD));
    }

    // ---- 第三处：两人各签各的；替签不作数；同一分钟也不并成一条 ----

    @Test
    void province_needsTwoDifferentHands() {
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);

        // 第一位落押后件仍停省局，差一位
        MoveActionReceipt first = service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", "满期可衔接", null);
        assertTrue(first.isAccepted(), first::getMessage);
        assertEquals(2, first.getNodeNo());
        assertEquals(1, first.getSignCount());
        assertEquals(1, first.getPendingRoles().size());
        assertEquals(TMetpMoveSign.ROLE_PROV_BUILD, first.getPendingRoles().get(0));

        // 同一人替另一职权签：不作数
        MoveActionReceipt forged = service.agree(id, TMetpMoveSign.ROLE_PROV_BUILD, "provA", null, null);
        assertFalse(forged.isAccepted());
        assertTrue(forged.getMessage().contains("替签"));
        assertEquals(1, bills.get(id).getSignCount(), "替签不得计数");

        // 同一职权重押：不作数
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provB", null, null).isAccepted());

        // 第二位（不同的人）落押：凑齐，两条独立记录（哪怕时刻同在一分钟也不并条）
        MoveActionReceipt second = service.agree(id, TMetpMoveSign.ROLE_PROV_BUILD, "provB", "先建后迁无虞", null);
        assertTrue(second.isAccepted(), second::getMessage);
        assertEquals(3, second.getNodeNo(), "省局点齐应进国务院气象主管机构");

        long roundProvRows = signs.stream().filter(s -> s.getBillId() == id
                && s.getNodeNo() == 2 && TMetpMoveSign.ACTION_AGREE.equals(s.getAction())
                && s.getRoundNo() == 1).count();
        assertEquals(2, roundProvRows, "省局须是两条画押记录，不并成一条");
        TMetpMoveSign a = signs.stream().filter(s -> TMetpMoveSign.ROLE_PROV_COMPARE.equals(s.getRoleKey())).findFirst().get();
        TMetpMoveSign b = signs.stream().filter(s -> TMetpMoveSign.ROLE_PROV_BUILD.equals(s.getRoleKey())).findFirst().get();
        assertNotEquals(a.getSigner(), b.getSigner());
        assertNotEquals(a.getId(), b.getId());
    }

    // ---- 第四处：批复须携批准文号 ----

    @Test
    void nation_approveRequiresNo_thenPass() {
        Long id = fullToProvinceDone();
        // 无文号不落成
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_NATION_APPROVE, "nationRen", null, "  ").isAccepted());
        assertEquals(0, bills.get(id).getStatus());

        MoveActionReceipt r = service.agree(id, TMetpMoveSign.ROLE_NATION_APPROVE, "nationRen", "准予", "GWQ〔2026〕7号");
        assertTrue(r.isAccepted(), r::getMessage);
        assertTrue(r.isFinished());
        assertEquals(1, bills.get(id).getStatus());
        assertEquals(3, bills.get(id).getNodeNo());
        assertEquals("GWQ〔2026〕7号", bills.get(id).getApproveNo());
        // 已核讫再押不收
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_NATION_APPROVE, "x", null, "GW-2").isAccepted());
    }

    // ---- 定否：四字一落件即终，两边再添不进一枚 ----

    @Test
    void veto_freezesBill() {
        Long id = submitOk();
        int signsBefore = signs.size();
        MoveActionReceipt v = service.veto(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", "探测环境不符，不予同意");
        assertTrue(v.isAccepted(), v::getMessage);
        assertTrue(v.isFrozen());
        assertEquals(2, bills.get(id).getStatus());

        // 冻住之后：落押、再否、驳回，统统添不进
        assertFalse(service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "x", null, null).isAccepted());
        assertFalse(service.veto(id, TMetpMoveSign.ROLE_CITY_ENV, "x", null).isAccepted());
        assertFalse(service.reject(id, "x", null).isAccepted());
        assertEquals(signsBefore + 1, signs.size(), "冻单后一枚签都不许添");
        assertTrue(signs.stream().anyMatch(s -> s.getBillId() == id
                && TMetpMoveSign.ACTION_VETO.equals(s.getAction())));
    }

    @Test
    void veto_wrongRoleOrStage_refused() {
        Long id = submitOk();
        // 件在市局，省局职权的否落不下来
        assertFalse(service.veto(id, TMetpMoveSign.ROLE_PROV_BUILD, "provA", null).isAccepted());
        assertEquals(0, bills.get(id).getStatus());
    }

    @Test
    void veto_byAlreadySignedRoleAtProvince_alsoCounts() {
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);
        // 对比观测一位先落押，随后改变主意落「不予同意」——本处在职职权，定否作数
        service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", "初拟同意", null);
        MoveActionReceipt v = service.veto(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", "复核后不予同意");
        assertTrue(v.isAccepted(), v::getMessage);
        assertTrue(v.isFrozen());
        assertEquals(2, bills.get(id).getStatus());
        assertEquals(2, bills.get(id).getNodeNo(), "件冻在省局，不后移");
    }

    // ---- 省局驳回：旧签分毫不动，自市局重新攒起，废签新签不同堂 ----

    @Test
    void rejectFromProvince_voidsRound_restartsAtCity() {
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);
        service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", "报告毛糙", null);

        // 非省局处驳不回
        Long id2 = submitOk();
        assertFalse(service.reject(id2, "provA", null).isAccepted(), "市局在办的件省局无从驳回");

        MoveActionReceipt r = service.reject(id, "provA", "对比观测报告毛糙，补齐再来");
        assertTrue(r.isAccepted(), r::getMessage);
        assertEquals(2, r.getRoundNo(), "轮次应加一");
        assertEquals(1, r.getNodeNo(), "件应发回市局");
        assertEquals(0, r.getSignCount(), "废签不计入新轮，新轮本处从 0 攒起");
        assertTrue(r.getPendingRoles().contains(TMetpMoveSign.ROLE_CITY_ENV));

        // 旧签记录原样留痕：第 1 轮台站、市局、省局落押各一枚，另加一枚驳回
        assertEquals(1, countRound(id, 1, 0, TMetpMoveSign.ACTION_AGREE));
        assertEquals(1, countRound(id, 1, 1, TMetpMoveSign.ACTION_AGREE));
        assertEquals(1, countRound(id, 1, 2, TMetpMoveSign.ACTION_AGREE));
        assertEquals(1, signs.stream().filter(s -> s.getBillId() == id
                && TMetpMoveSign.ACTION_REJECT.equals(s.getAction()) && s.getRoundNo() == 1).count());

        // 同一轮不可重复驳回
        assertFalse(service.reject(id, "provA", null).isAccepted(), "件已发回市局，不在省局");

        // 自市局重新攒：市局第二轮落押后进省局，计数只认新轮，旧签不同堂
        MoveActionReceipt city2 = service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityWang", "重核合格", null);
        assertTrue(city2.isAccepted(), city2::getMessage);
        assertEquals(2, city2.getNodeNo());
        assertEquals(1, countRound(id, 2, 1, TMetpMoveSign.ACTION_AGREE));
        // 旧的第一轮市局签仍在，未被删改
        TMetpMoveSign oldCity = signs.stream().filter(s -> s.getBillId() == id && s.getNodeNo() == 1
                && s.getRoundNo() == 1 && TMetpMoveSign.ACTION_AGREE.equals(s.getAction())).findFirst().get();
        assertEquals("cityLi", oldCity.getSigner());
    }

    private long countRound(Long id, int round, int node, String action) {
        return signs.stream().filter(s -> s.getBillId() != null && s.getBillId().longValue() == id.longValue()
                && s.getRoundNo() == round
                && s.getNodeNo() == node && action.equals(s.getAction())).count();
    }

    // ---- 核讫后一键誊带通知，字段同源，一单一通知 ----

    @Test
    void notice_generatedFromBill_copyFieldsAndOneOnly() {
        Long id = fullToPass();

        // 未核讫生不出来（取另一件在办的验）
        Long running = submitOk();
        assertFalse(service.generateNotice(running, "clerk").isAccepted());
        assertEquals(0, notices.size());

        MoveActionReceipt r = service.generateNotice(id, "clerk");
        assertTrue(r.isAccepted(), r::getMessage);
        ArgumentCaptor<TMetpMoveNotice> cap = ArgumentCaptor.forClass(TMetpMoveNotice.class);
        verify(noticeMapper, times(1)).insert(cap.capture());
        TMetpMoveNotice n = cap.getValue();
        TMetpMoveBill b = bills.get(id);
        assertEquals(b.getId(), n.getBillId());
        assertEquals(b.getBillNo(), n.getBillNo());
        assertEquals(b.getSiteNo(), n.getSiteNo());
        assertEquals(b.getNewSiteNo(), n.getNewSiteNo());
        assertEquals(b.getNewLng(), n.getNewLng());
        assertEquals(b.getNewLat(), n.getNewLat());
        assertEquals(0, b.getNewAlt().compareTo(n.getNewAlt()));
        assertEquals(b.getCompareStart(), n.getCompareStart());
        assertEquals(b.getCompareEnd(), n.getCompareEnd());
        assertEquals(b.getApproveNo(), n.getApproveNo());

        // 再来一键：不另生第二条，原条端回，仍是作数的回音
        MoveActionReceipt again = service.generateNotice(id, "clerk");
        assertTrue(again.isAccepted());
        assertNotNull(again.getNotice());
        verify(noticeMapper, times(1)).insert(any());
        assertEquals(1, notices.size());
    }

    // ---- 逐步倒着核回来，次序为轮次/层/时刻倒序，废签在册可辨 ----

    @Test
    void trace_returnsReverseOrderWithVoidedRound() {
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);
        service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", null, null);
        service.reject(id, "provA", "毛糙");
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityWang", null, null);

        List<TMetpMoveSign> trace = service.traceSigns(id);
        assertEquals(5, trace.size(), "台站+市1+省1+驳回+市2 = 5 条（含第1轮废签）");
        for (int i = 1; i < trace.size(); i++) {
            TMetpMoveSign pre = trace.get(i - 1);
            TMetpMoveSign cur = trace.get(i);
            assertTrue(pre.getRoundNo() > cur.getRoundNo()
                            || (pre.getRoundNo() == cur.getRoundNo() && pre.getNodeNo() >= cur.getNodeNo()),
                    "倒序排列有误");
        }
        // 第一轮被划去的签仍在册中可辨
        assertTrue(trace.stream().anyMatch(s -> s.getRoundNo() == 1
                && TMetpMoveSign.ROLE_CITY_ENV.equals(s.getRoleKey()) && "cityLi".equals(s.getSigner())));
        assertTrue(trace.stream().anyMatch(s -> s.getRoundNo() == 2
                && TMetpMoveSign.ROLE_CITY_ENV.equals(s.getRoleKey()) && "cityWang".equals(s.getSigner())));
    }

    @Test
    void traceQuery_isReverseOrderedAtDataLayer() {
        // 屏上所见须是行内所存同锅舀出：倒序由数据层查询承担
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);
        service.traceSigns(id);
        ArgumentCaptor<QueryWrapper<TMetpMoveSign>> cap = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(signMapper, atLeastOnce()).selectList(cap.capture());
        String sql = cap.getValue().getSqlSegment();
        assertTrue(sql.contains("ORDER BY"), sql);
        assertTrue(sql.contains("round_no") && sql.contains("node_no")
                && sql.contains("create_time"), "须按轮次/层/时刻倒核");
    }

    @Test
    void snapshot_unknownBill_refused() {
        assertFalse(service.snapshot(404L).isAccepted());
        assertNull(service.selectTMetpMoveBillById(null));
    }

    // ---- 帮一路走到省局点齐 / 核讫 ----

    private Long fullToProvinceDone() {
        Long id = submitOk();
        service.agree(id, TMetpMoveSign.ROLE_CITY_ENV, "cityLi", null, null);
        service.agree(id, TMetpMoveSign.ROLE_PROV_COMPARE, "provA", null, null);
        service.agree(id, TMetpMoveSign.ROLE_PROV_BUILD, "provB", null, null);
        return id;
    }

    private Long fullToPass() {
        Long id = fullToProvinceDone();
        service.agree(id, TMetpMoveSign.ROLE_NATION_APPROVE, "nationRen", null, "GWQ〔2026〕7号");
        return id;
    }
}
