package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.auto.TMetpMoveSign;
import com.fc.v2.model.custom.MoveActionReceipt;
import com.fc.v2.model.custom.MoveBillSubmitForm;

import java.util.List;

/**
 * 台站迁移变更逐级核准单 Service接口（approval-chain 形状）。
 * 门牌号「画押」：落押、定否、驳回三大动作概归服务层执掌；
 * 无立单改单的裸 CRUD 入口，页面不许预设画押落定何样。
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITMetpMoveBillService {

    /** 按主键回查单据 */
    TMetpMoveBill selectTMetpMoveBillById(Long id);

    /** 台账只读列示 */
    List<TMetpMoveBill> selectMoveBillList(QueryWrapper<TMetpMoveBill> queryWrapper);

    /**
     * 第一处·台站立单申报：六类材料缺一项报不上去；立单即台站落押，件进市局。
     *
     * @param form     现行迁建申报表单式
     * @param operator 画押人（取自登录身份，页面带不算）
     */
    MoveActionReceipt submit(MoveBillSubmitForm form, String operator);

    /**
     * 落押。每一道门各有固定职权：
     * 市局 CITY_ENV；省局 PROV_COMPARE（对比观测满期/历史序列）、PROV_BUILD（先建后迁次序）；
     * 国务院气象主管机构 NATION_APPROVE（须携批准文号）。
     * 省局两位各签各的，同人替签不作数。
     */
    MoveActionReceipt agree(Long id, String roleKey, String operator, String opinion, String approveNo);

    /**
     * 定否：落「不予同意」，件即刻终止冻住，此后两边再添不进一枚。
     */
    MoveActionReceipt veto(Long id, String roleKey, String operator, String opinion);

    /**
     * 省局驳回：只在省局一处可用，发回市局。前头各签凭轮次划去、分毫不动，
     * 件自市局一阶重新攒起，废签与新签不同堂并摆。
     */
    MoveActionReceipt reject(Long id, String operator, String opinion);

    /**
     * 核讫后一键生成新址启用通知；新址编号、经纬度、对比观测起止、批准文号
     * 统统由服务层从核准件誊带，不许第二个人誊；一单一通知。
     */
    MoveActionReceipt generateNotice(Long id, String operator);

    /** 件此刻停在何处、本处还差谁签（只读回音） */
    MoveActionReceipt snapshot(Long id);

    /**
     * 逐步倒着核回来：全部画押记录（含被划去的前轮废签），
     * 按轮次、层、时刻倒序；记录自落押起未改分毫。
     */
    List<TMetpMoveSign> traceSigns(Long id);
}
