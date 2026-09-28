package com.fc.v2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.mapper.auto.TMetpMoveBillMapper;
import com.fc.v2.mapper.auto.TMetpMoveNoticeMapper;
import com.fc.v2.mapper.auto.TMetpMoveSignMapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveEcho;
import com.fc.v2.model.custom.MoveRole;
import com.fc.v2.service.impl.TMetpMoveBillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 「画押」规矩的单元核验：不碰库，三张 Mapper 全是内存账，
 * 灵没灵只看服务层回音。
 *
 * @author fuce
 * @date 2026-09-28
 */
class TMetpMoveBillServiceTest {

    /** 可换人的服务替身：落押人只从这里出，门脸递什么都不作数。 */
    private static class TestService extends TMetpMoveBillServiceImpl {
        String signer = "station-zhang";
        @Override
        protected String currentSigner() {
            return signer;
        }
    }

    private TestService service;
    private TMetpMoveBillMapper billMapper;
    private TMetpMoveSignMapper signMapper;
    private TMetpMoveNoticeMapper noticeMapper;

    private final Map<Long, TMetpMoveBill> bills = new HashMap<>();
    private final List<TMetpMoveSign> signs = new ArrayList<>();
    private final List<TMetpMoveNotice> notices = new ArrayList<>();

    private static final Pattern EQ = Pattern.compile(
            "(?i)([a-z_]+)\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)}");

    @SuppressWarnings("unchecked")
    private static Map<String, Object> eqPairs(Wrapper<?> w) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (w == null) {
            return out;
        }
        String sql = w.getSqlSegment();
        Map<String, Object> params = (Map<String, Object>) ReflectionTestUtils
                .getField(w, "paramNameValuePairs");
        Matcher m = EQ.matcher(sql == null ? "" : sql);
        while (m.find()) {
            out.put(m.group(1), params.get(m.group(2)));
        }
        return out;
    }

    @BeforeEach
    void setUp() {
        service = new TestService();
        billMapper = mock(TMetpMoveBillMapper.class);
        signMapper = mock(TMetpMoveSignMapper.class);
        noticeMapper = mock(TMetpMoveNoticeMapper.class);
        ReflectionTestUtils.setField(service, "metpMoveBillMapper", billMapper);
        ReflectionTestUtils.setField(service, "metpMoveSignMapper", signMapper);
        ReflectionTestUtils.setField(service, "metpMoveNoticeMapper", noticeMapper);

        when(billMapper.selectById(any())).thenAnswer(inv -> bills.get(inv.getArgument(0)));
        when(billMapper.insert(any())).thenAnswer(inv -> {
            TMetpMoveBill b = inv.getArgument(0);
            bills.put(b.getId(), b);
            return 1;
        });
        when(billMapper.updateById(any())).thenAnswer(inv -> {
            TMetpMoveBill b = inv.getArgument(0);
            bills.put(b.getId(), b);
            return 1;
        });

        when(signMapper.insert(any())).thenAnswer(inv -> {
            signs.add(inv.getArgument(0));
            return 1;
        });
        when(signMapper.updateById(any())).thenAnswer(inv -> {
            TMetpMoveSign s = inv.getArgument(0);
            for (int i = 0; i < signs.size(); i++) {
                if (signs.get(i).getId().equals(s.getId())) {
                    signs.set(i, s);
                }
            }
            return 1;
        });
        when(signMapper.selectList(any())).thenAnswer(inv -> {
            Wrapper<?> w = inv.getArgument(0);
            Map<String, Object> eq = eqPairs(w);
            List<TMetpMoveSign> list = new ArrayList<>();
            for (TMetpMoveSign s : signs) {
                if (match(eq, "bill_id", s.getBillId())
                        && match(eq, "round_no", s.getRoundNo())
                        && match(eq, "node_no", s.getNodeNo())
                        && match(eq, "valid_flag", s.getValidFlag())
                        && match(eq, "del_flag", s.getDelFlag())
                        && match(eq, "action", s.getAction())) {
                    list.add(s);
                }
            }
            String sql = w.getSqlSegment() == null ? "" : w.getSqlSegment();
            if (sql.toUpperCase().contains("NODE_NO DESC")) {
                list.sort(Comparator.comparing((TMetpMoveSign s) -> s.getNodeNo())
                        .thenComparing(TMetpMoveSign::getSignTime)
                        .thenComparing(TMetpMoveSign::getId).reversed());
            } else {
                list.sort(Comparator.comparing((TMetpMoveSign s) -> s.getNodeNo())
                        .thenComparing(TMetpMoveSign::getSignTime)
                        .thenComparing(TMetpMoveSign::getId));
            }
            return list;
        });

        when(noticeMapper.insert(any())).thenAnswer(inv -> {
            notices.add(inv.getArgument(0));
            return 1;
        });
        when(noticeMapper.selectList(any())).thenAnswer(inv -> {
            Wrapper<?> w = inv.getArgument(0);
            Map<String, Object> eq = eqPairs(w);
            List<TMetpMoveNotice> list = new ArrayList<>();
            for (TMetpMoveNotice n : notices) {
                if (match(eq, "bill_id", n.getBillId())
                        && match(eq, "del_flag", n.getDelFlag())
                        && match(eq, "site_no", n.getSiteNo())) {
                    list.add(n);
                }
            }
            return list;
        });
        when(noticeMapper.selectCount(any())).thenAnswer(inv -> {
            Wrapper<?> w = inv.getArgument(0);
            Map<String, Object> eq = eqPairs(w);
            return (int) notices.stream().filter(n -> match(eq, "del_flag", n.getDelFlag())
                    && match(eq, "site_no", n.getSiteNo())).count();
        });
    }

    private static boolean match(Map<String, Object> eq, String col, Object val) {
        if (!eq.containsKey(col)) {
            return true;
        }
        Object want = eq.get(col);
        if (want == null) {
            return val == null;
        }
        return want instanceof Number && val instanceof Number
                ? ((Number) want).longValue() == ((Number) val).longValue()
                : want.equals(val);
    }

    private TMetpMoveBill fullForm() {
        TMetpMoveBill f = new TMetpMoveBill();
        f.setSiteNo("TQ00");
        f.setSiteName("临川国家基本气象站");
        f.setReason("城市扩张，旧址探测环境不再符合要求");
        f.setNewSiteNo("TQ00-N");
        f.setNewLng(new BigDecimal("116.391230"));
        f.setNewLat(new BigDecimal("28.009870"));
        f.setNewAlt(new BigDecimal("45.60"));
        f.setSurface("缓坡草地，四周开阔无高大建筑");
        f.setOldFieldStatus("旧址观测场仍在运行，未拆");
        f.setCompareStart(new Date(1_700_000_000_000L));
        f.setCompareEnd(new Date(1_705_000_000_000L));
        f.setNewStationBuilt(1);
        return f;
    }

    private TMetpMoveBill createFull() {
        MoveEcho echo = service.createBill(fullForm());
        assertTrue(echo.isEffective(), echo.getMessage());
        return echo.getBill();
    }

    @Test
    void 第一处要件缺一便报不上去() {
        TMetpMoveBill f = fullForm();
        f.setSurface(null);
        MoveEcho echo = service.createBill(f);
        assertFalse(echo.isEffective());
        assertTrue(echo.getMessage().contains("下垫面情形"));
        assertTrue(bills.isEmpty());
    }

    @Test
    void 对比观测止早于起也报不上去() {
        TMetpMoveBill f = fullForm();
        f.setCompareEnd(new Date(f.getCompareStart().getTime() - 1000));
        assertFalse(service.createBill(f).isEffective());
    }

    @Test
    void 台站报件即第一处落印件停市局() {
        TMetpMoveBill bill = createFull();
        assertEquals(1, bill.getNodeNo());
        assertEquals(0, bill.getStatus());
        assertEquals(1, bill.getRoundNo());
        assertEquals(1, signs.size());
        assertEquals("STATION", signs.get(0).getRoleKey());
        assertNotNull(signs.get(0).getSnapshot());
        assertTrue(signs.get(0).getSnapshot().contains("TQ00"));
    }

    @Test
    void 顺序不可颠倒省局角色在市局页面点不下去() {
        TMetpMoveBill bill = createFull();
        service.signer = "prov-li";
        MoveEcho echo = service.sign(bill.getId(), MoveRole.PROV_COMPARE, "提前点", null);
        assertFalse(echo.isEffective());
        assertTrue(echo.getMessage().contains("顺序不可颠倒"));
    }

    @Test
    void 市局落押后件进省局且回音报两名欠手() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        MoveEcho echo = service.sign(bill.getId(), MoveRole.CITY, "探测环境合格", null);
        assertTrue(echo.isEffective(), echo.getMessage());
        assertEquals(2, echo.getBill().getNodeNo());
        assertEquals(2, echo.getPendingRoles().size());
        assertEquals("PROV_COMPARE", echo.getPendingRoles().get(0).getRoleKey());
        assertEquals("PROV_ORDER", echo.getPendingRoles().get(1).getRoleKey());
    }

    @Test
    void 省局只签一位件仍停第三处走不到第四处() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        MoveEcho echo = service.sign(bill.getId(), MoveRole.PROV_COMPARE, "对比观测满期可衔接", null);
        assertTrue(echo.isEffective());
        assertEquals(2, echo.getBill().getNodeNo());
        assertEquals(1, echo.getBill().getSignCount());
        assertEquals(1, echo.getPendingRoles().size());
        assertEquals("PROV_ORDER", echo.getPendingRoles().get(0).getRoleKey());
    }

    @Test
    void 同一分钟两人各签各角色也是两条不并条() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        MoveEcho e1 = service.sign(bill.getId(), MoveRole.PROV_COMPARE, "序列可接", null);
        service.signer = "prov-zhao";
        MoveEcho e2 = service.sign(bill.getId(), MoveRole.PROV_ORDER, "先建后迁无误", null);
        assertTrue(e1.isEffective() && e2.isEffective());
        long provCount = signs.stream().filter(s -> s.getNodeNo() == 2 && s.getAction() == 1).count();
        assertEquals(2, provCount);
        assertEquals(3, e2.getBill().getNodeNo(), "两人凑齐才进第四处");
    }

    @Test
    void 同一只手替另一角色点印不作数() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        service.sign(bill.getId(), MoveRole.PROV_COMPARE, null, null);
        MoveEcho echo = service.sign(bill.getId(), MoveRole.PROV_ORDER, null, null);
        assertFalse(echo.isEffective());
        assertTrue(echo.getMessage().contains("替另一角色点印不作数"));
        assertEquals(2, echo.getBill() == null ? 2 : bills.get(bill.getId()).getNodeNo());
    }

    @Test
    void 新站未起先建后迁这一印落不下() {
        TMetpMoveBill f = fullForm();
        f.setNewStationBuilt(0);
        MoveEcho created = service.createBill(f);
        TMetpMoveBill bill = created.getBill();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        service.sign(bill.getId(), MoveRole.PROV_COMPARE, null, null);
        service.signer = "prov-zhao";
        MoveEcho echo = service.sign(bill.getId(), MoveRole.PROV_ORDER, null, null);
        assertFalse(echo.isEffective());
        assertTrue(echo.getMessage().contains("新站尚未建起"));
    }

    @Test
    void 同一角色重复落押被拒() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        // 件到省局后市局再点，层不对
        service.signer = "city-wang2";
        MoveEcho echo = service.sign(bill.getId(), MoveRole.CITY, null, null);
        assertFalse(echo.isEffective());
    }

    @Test
    void 第四处无文号不批有文号才核讫() {
        TMetpMoveBill bill = pushToNational();
        service.signer = "national-chen";
        assertFalse(service.sign(bill.getId(), MoveRole.NATIONAL, null, null).isEffective());
        MoveEcho echo = service.sign(bill.getId(), MoveRole.NATIONAL, "准予迁移", "国气审〔2026〕88号");
        assertTrue(echo.isEffective(), echo.getMessage());
        assertEquals(1, echo.getBill().getStatus());
        assertEquals(3, echo.getBill().getNodeNo());
        assertEquals("国气审〔2026〕88号", echo.getBill().getApproveDocNo());
    }

    @Test
    void 任一落不予同意件即冻死两边再添不进() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        long before = signs.size();
        MoveEcho veto = service.veto(bill.getId(), MoveRole.CITY, "探测环境不符");
        assertTrue(veto.isEffective());
        assertEquals(3, bills.get(bill.getId()).getStatus());

        MoveEcho againSign = service.sign(bill.getId(), MoveRole.CITY, "再试试", null);
        MoveEcho againVeto = service.veto(bill.getId(), MoveRole.CITY, null);
        MoveEcho back = service.sendBack(bill.getId(), null);
        assertFalse(againSign.isEffective());
        assertFalse(againVeto.isEffective());
        assertFalse(back.isEffective());
        assertEquals(before + 1, signs.size(), "终局后一枚签也添不进");
    }

    @Test
    void 省局发回市局前签划废自市局重攒废签不与新签同堂() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, "一轮市局", null);
        service.signer = "prov-li";
        service.sign(bill.getId(), MoveRole.PROV_COMPARE, "报告毛糙先看着", null);

        MoveEcho back = service.sendBack(bill.getId(), "对比观测报告毛糙");
        assertTrue(back.isEffective(), back.getMessage());
        assertEquals(2, back.getBill().getRoundNo());
        assertEquals(1, back.getBill().getNodeNo());
        assertEquals(2, back.getBill().getStatus());

        // 市局及以后的旧签划废；台站报件留着不动。
        TMetpMoveSign oldCity = signs.stream().filter(s -> s.getNodeNo() == 1).findFirst().orElse(null);
        TMetpMoveSign station = signs.stream().filter(s -> s.getNodeNo() == 0).findFirst().orElse(null);
        assertNotNull(oldCity);
        assertEquals(0, oldCity.getValidFlag());
        assertEquals(1, station.getValidFlag());

        // 新一轮台账里只有台站旧印 + 发回动作在第1轮；第2轮尚无新签
        List<TMetpMoveSign> round2 = service.listValidSigns(bill.getId(), 2);
        assertTrue(round2.isEmpty(), "废签不与新签同堂并摆");

        // 自市局重新攒起，走通全程
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, "二轮市局补齐报告", null);
        service.signer = "prov-li";
        service.sign(bill.getId(), MoveRole.PROV_COMPARE, "二轮满期", null);
        service.signer = "prov-zhao";
        service.sign(bill.getId(), MoveRole.PROV_ORDER, "次序无误", null);
        service.signer = "national-chen";
        MoveEcho passed = service.sign(bill.getId(), MoveRole.NATIONAL, null, "国气审〔2026〕89号");
        assertEquals(1, passed.getBill().getStatus());

        // 倒核：当前有效链上四处各取最新一轮的印，第三处两只手都在
        List<TMetpMoveSign> trace = service.traceBack(bill.getId());
        assertEquals(5, trace.size());
        assertEquals(3, trace.get(0).getNodeNo());
        long provHands = trace.stream().filter(s -> s.getNodeNo() == 2).count();
        assertEquals(2, provHands, "省局两只手倒核时一条不丢");
        assertEquals(0, trace.get(4).getNodeNo());
        TMetpMoveSign cityLatest = trace.stream().filter(s -> s.getNodeNo() == 1).findFirst().get();
        assertEquals(2, cityLatest.getRoundNo());
        assertEquals("二轮市局补齐报告", cityLatest.getOpinion());
    }

    @Test
    void 件不在省局发回这手使不出() {
        TMetpMoveBill bill = createFull();
        assertFalse(service.sendBack(bill.getId(), null).isEffective());
    }

    @Test
    void 核讫后一键生通知字段全自核准件带出且一座台站只一条() {
        TMetpMoveBill bill = pushToNational();
        service.signer = "national-chen";
        service.sign(bill.getId(), MoveRole.NATIONAL, null, "国气审〔2026〕88号");

        // 未核讫生不出来的反向情形已在另案，这里直接核讫生通知
        MoveEcho n1 = service.generateNotice(bill.getId());
        assertTrue(n1.isEffective(), n1.getMessage());
        TMetpMoveNotice notice = n1.getNotice();
        assertEquals(bill.getBillNo(), notice.getBillNo());
        assertEquals("TQ00", notice.getSiteNo());
        assertEquals("TQ00-N", notice.getNewSiteNo());
        assertEquals(0, new BigDecimal("116.391230").compareTo(notice.getNewLng()));
        assertEquals(0, new BigDecimal("28.009870").compareTo(notice.getNewLat()));
        assertEquals(bill.getCompareStart(), notice.getCompareStart());
        assertEquals(bill.getCompareEnd(), notice.getCompareEnd());
        assertEquals("国气审〔2026〕88号", notice.getApproveDocNo());

        // 再点一次只给原条，不另造
        MoveEcho n2 = service.generateNotice(bill.getId());
        assertTrue(n2.isEffective());
        assertEquals(notice.getId(), n2.getNotice().getId());
        assertEquals(1, notices.size());

        // 同一台站第二件即便核讫，也不许再有一条
        TMetpMoveBill second = fullForm();
        MoveEcho c2 = service.createBill(second);
        TMetpMoveBill b2 = c2.getBill();
        service.signer = "city-wang";
        service.sign(b2.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        service.sign(b2.getId(), MoveRole.PROV_COMPARE, null, null);
        service.signer = "prov-zhao";
        service.sign(b2.getId(), MoveRole.PROV_ORDER, null, null);
        service.signer = "national-chen";
        service.sign(b2.getId(), MoveRole.NATIONAL, null, "国气审〔2026〕90号");
        MoveEcho dup = service.generateNotice(b2.getId());
        assertFalse(dup.isEffective());
        assertTrue(dup.getMessage().contains("一座台站只许一条"));
        assertEquals(1, notices.size());
    }

    @Test
    void 未核讫生不出通知() {
        TMetpMoveBill bill = createFull();
        assertFalse(service.generateNotice(bill.getId()).isEffective());
    }

    @Test
    void 倒核快照与当时屏幕所见同源() {
        TMetpMoveBill bill = pushToNational();
        service.signer = "national-chen";
        service.sign(bill.getId(), MoveRole.NATIONAL, null, "国气审〔2026〕88号");
        List<TMetpMoveSign> trace = service.traceBack(bill.getId());
        assertEquals(5, trace.size());
        long provHands = trace.stream().filter(s -> s.getNodeNo() == 2).count();
        assertEquals(2, provHands);
        for (TMetpMoveSign s : trace) {
            assertNotNull(s.getSnapshot());
            assertTrue(s.getSnapshot().contains("\"billNo\""));
            // 旧存签记录分毫不动：发回划废只改 valid_flag，本流程没有发回，全部有效
            assertEquals(1, s.getValidFlag());
        }
    }

    @Test
    void 详情回音报件停何处与欠谁() {
        TMetpMoveBill bill = createFull();
        MoveEcho d = service.detail(bill.getId());
        assertTrue(d.isEffective());
        assertEquals(1, d.getBill().getNodeNo());
        assertEquals(1, d.getPendingRoles().size());
        assertEquals("CITY", d.getPendingRoles().get(0).getRoleKey());
        assertNull(service.detail(999999L));
    }

    private TMetpMoveBill pushToNational() {
        TMetpMoveBill bill = createFull();
        service.signer = "city-wang";
        service.sign(bill.getId(), MoveRole.CITY, null, null);
        service.signer = "prov-li";
        service.sign(bill.getId(), MoveRole.PROV_COMPARE, null, null);
        service.signer = "prov-zhao";
        service.sign(bill.getId(), MoveRole.PROV_ORDER, null, null);
        return bill;
    }
}
