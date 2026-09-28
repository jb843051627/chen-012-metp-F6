package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.custom.MoveActionReceipt;
import com.fc.v2.model.custom.MoveBillSubmitForm;
import com.fc.v2.service.ITMetpMoveBillService;
import com.fc.v2.shiro.util.ShiroUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 台站迁移变更逐级核准单 Controller（approval-chain 形状：流转入口）。
 *
 * 门牌号「画押」。门脸只传事、不传落定：没有立单/改单的裸入口，
 * 落押、定否、驳回、出通知概归服务层执掌；画押人只认登录身份，页面带谁都不算；
 * 灵没灵、件停何处、还差谁签，全在回音里。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Api(value = "台站迁移变更逐级核准单")
@Controller
@RequestMapping("/metpMoveBill")
public class MetpMoveBillController extends BaseController {

    private final String prefix = "admin/metpMoveBill";

    @Autowired
    private ITMetpMoveBillService metpMoveBillService;

    @ApiOperation(value = "逐级核准台账跳转", notes = "逐级核准台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("gen:metpMoveBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "台站迁移变更逐级核准台账", action = "list")
    @ApiOperation(value = "核准台账", notes = "核准台账只读列示")
    @GetMapping("/list")
    @RequiresPermissions("gen:metpMoveBill:list")
    @ResponseBody
    public ResultTable list(TMetpMoveBill record) {
        QueryWrapper<TMetpMoveBill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", 0);
        if (record != null) {
            if (record.getBillNo() != null && !record.getBillNo().trim().isEmpty()) {
                queryWrapper.like("bill_no", record.getBillNo().trim());
            }
            if (record.getStatus() != null) {
                queryWrapper.eq("status", record.getStatus());
            }
        }
        queryWrapper.orderByDesc("create_time");
        startPage();
        com.github.pagehelper.PageInfo<TMetpMoveBill> page =
                new com.github.pagehelper.PageInfo<TMetpMoveBill>(metpMoveBillService.selectMoveBillList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "台站迁移立单申报", action = "submit")
    @ApiOperation(value = "台站立单申报", notes = "材料缺一项报不上去；落押人取登录身份")
    @PostMapping("/submit")
    @RequiresPermissions("gen:metpMoveBill:submit")
    @ResponseBody
    public AjaxResult submit(MoveBillSubmitForm form) {
        return echo(metpMoveBillService.submit(form, ShiroUtils.getLoginName()));
    }

    @Log(title = "台站迁移落押", action = "agree")
    @ApiOperation(value = "落押", notes = "市局/省局两职权/国务院气象主管机构批复；省局替签重签不作数")
    @PostMapping("/agree")
    @RequiresPermissions("gen:metpMoveBill:agree")
    @ResponseBody
    public AjaxResult agree(Long id, String roleKey, String opinion, String approveNo) {
        return echo(metpMoveBillService.agree(id, roleKey, ShiroUtils.getLoginName(), opinion, approveNo));
    }

    @Log(title = "台站迁移定否", action = "veto")
    @ApiOperation(value = "定否", notes = "落不予同意，件即刻终止冻单")
    @PostMapping("/veto")
    @RequiresPermissions("gen:metpMoveBill:veto")
    @ResponseBody
    public AjaxResult veto(Long id, String roleKey, String opinion) {
        return echo(metpMoveBillService.veto(id, roleKey, ShiroUtils.getLoginName(), opinion));
    }

    @Log(title = "台站迁移省局驳回", action = "reject")
    @ApiOperation(value = "省局驳回", notes = "发回市局，前轮签记录划去留痕，自市局重攒")
    @PostMapping("/reject")
    @RequiresPermissions("gen:metpMoveBill:reject")
    @ResponseBody
    public AjaxResult reject(Long id, String opinion) {
        return echo(metpMoveBillService.reject(id, ShiroUtils.getLoginName(), opinion));
    }

    @Log(title = "新址启用通知生成", action = "notice")
    @ApiOperation(value = "一键生成新址启用通知", notes = "核讫后方可生成，字段由服务层自核准件誊带，一单一通知")
    @PostMapping("/notice")
    @RequiresPermissions("gen:metpMoveBill:notice")
    @ResponseBody
    public AjaxResult notice(Long id) {
        return echo(metpMoveBillService.generateNotice(id, ShiroUtils.getLoginName()));
    }

    @ApiOperation(value = "件停何处", notes = "回音：当前层、已签/应签、还差谁签、是否冻单核讫")
    @GetMapping("/snapshot")
    @RequiresPermissions("gen:metpMoveBill:view")
    @ResponseBody
    public AjaxResult snapshot(Long id) {
        return echo(metpMoveBillService.snapshot(id));
    }

    @ApiOperation(value = "画押倒核", notes = "全部签记录按轮次/层/时刻倒序，废签新签分轮可辨")
    @GetMapping("/trace")
    @RequiresPermissions("gen:metpMoveBill:view")
    @ResponseBody
    public AjaxResult trace(Long id) {
        return AjaxResult.success(200, "ok", metpMoveBillService.traceSigns(id));
    }

    /** 回音即权柄：没作数的动作以错误码回，话由服务层定。 */
    private AjaxResult echo(MoveActionReceipt receipt) {
        if (receipt != null && receipt.isAccepted()) {
            return AjaxResult.success(200, receipt.getMessage(), receipt);
        }
        String msg = receipt == null ? "动作未被受理" : receipt.getMessage();
        return AjaxResult.error(500, msg).put("data", receipt);
    }
}
