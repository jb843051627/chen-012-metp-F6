package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveNotice;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveEcho;
import com.fc.v2.model.custom.MoveRole;

import java.util.List;

/**
 * 台站迁移变更逐级核准单 Service接口（approval-chain 形状）。
 * 门牌号「画押」：落押、定否、驳回三大动作概归本服务执掌，
 * 页面只许叩门问回音，不许立单、改单、预设落定何样。
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITMetpMoveBillService {

    /**
     * 台站报件立单（第一处）。申请事由、新址经纬度与海拔、下垫面情形、
     * 旧址观测场现状、对比观测起止，缺一项报不上去。立单即落定，改只许另起新件。
     */
    MoveEcho createBill(TMetpMoveBill form);

    /** 落押同意。signer 由服务层自取，门脸不得代传；approveDocNo 仅第四处批复时带。 */
    MoveEcho sign(Long id, MoveRole role, String opinion, String approveDocNo);

    /** 定否：当前层落「不予同意」，件即终局冻死，两边再不能添进一枚。 */
    MoveEcho veto(Long id, MoveRole role, String opinion);

    /** 驳回：省局嫌对比观测报告毛糙，发回市局；前头签记录尽数划废，自市局重新攒起。 */
    MoveEcho sendBack(Long id, String opinion);

    /** 一键生成新址启用通知（件须已核讫；一座台站一条，重复叩门只给原条）。 */
    MoveEcho generateNotice(Long id);

    /** 按主键回查单据 */
    TMetpMoveBill selectTMetpMoveBillById(Long id);

    /** 只读回音：件停何处、本层还差谁、本轮有效签记录，全由账上现攒，页面不许亲手报数 */
    MoveEcho detail(Long id);

    /** 台账 */
    List<TMetpMoveBill> selectList(QueryWrapper<TMetpMoveBill> queryWrapper);

    /** 某一轮有效签记录（废签不在其列） */
    List<TMetpMoveSign> listValidSigns(Long billId, int roundNo);

    /**
     * 逐步倒着核回来：当前轮签记录按层 3→0、同层按落押时刻倒序，
     * 每条带着落定当时的单据快照，与办当时屏幕所见分毫不差。
     */
    List<TMetpMoveSign> traceBack(Long billId);

    /** 取本件的启用通知（未生成返 null） */
    TMetpMoveNotice selectNoticeByBillId(Long billId);
}
