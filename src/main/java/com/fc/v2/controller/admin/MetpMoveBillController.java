package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TMetpMoveBill;
import com.fc.v2.model.custom.MoveEcho;
import com.fc.v2.model.custom.MoveRole;
import com.fc.v2.service.ITMetpMoveBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 台站迁移变更逐级核准 Controller（门牌号「画押」）。
 * 门脸只许叩门：立单、落押、定否、驳回、生通知一律薄转发服务层，
 * 本处不立单、不改单、不收手填签人；落定何样全看回音。
 *
 * @author fuce
 * @date 2026-09-28
 */
@Api(value = "台站迁移变更逐级核准")
@Controller
@RequestMapping("/metpMoveBill")
public class MetpMoveBillController extends BaseController {

    private final String prefix = "admin/metpMoveBill";

    @Autowired
    private ITMetpMoveBillService metpMoveBillService;

    @ApiOperation(value = "流转台账跳转", notes = "流转台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("metpMoveBill:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "台站迁移核准台账", action = "list")
    @ApiOperation(value = "台账", notes = "台账")
    @GetMapping("/list")
    @RequiresPermissions("metpMoveBill:list")
    @ResponseBody
    public ResultTable list(TMetpMoveBill query) {
        QueryWrapper<TMetpMoveBill> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (query.getBillNo() != null && !query.getBillNo().trim().isEmpty()) {
            qw.like("bill_no", query.getBillNo().trim());
        }
        if (query.getSiteNo() != null && !query.getSiteNo().trim().isEmpty()) {
            qw.like("site_no", query.getSiteNo().trim());
        }
        if (query.getStatus() != null) {
            qw.eq("status", query.getStatus());
        }
        qw.orderByDesc("create_time");
        startPage();
        com.github.pagehelper.PageInfo<TMetpMoveBill> page =
                new com.github.pagehelper.PageInfo<TMetpMoveBill>(metpMoveBillService.selectList(qw));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "迁建申报表（报件页）", notes = "单式沿用现行迁建申报表")
    @GetMapping("/add")
    @RequiresPermissions("metpMoveBill:create")
    public String add() {
        return prefix + "/add";
    }

    @Log(title = "台站报件立单", action = "create")
    @ApiOperation(value = "台站报件（第一处）", notes = "申请四要件缺一不报")
    @PostMapping("/create")
    @RequiresPermissions("metpMoveBill:create")
    @ResponseBody
    public AjaxResult create(TMetpMoveBill form) {
        return echo(metpMoveBillService.createBill(form));
    }

    @ApiOperation(value = "核准件详情页", notes = "件停何处、还差谁签，全由服务层账上摆出")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("metpMoveBill:view")
    public String detail(@PathVariable("id") Long id, ModelMap model) {
        model.put("id", id);
        return prefix + "/detail";
    }

    @ApiOperation(value = "详情回音", notes = "单据现状、尚欠角色、本轮签记录、启用通知")
    @GetMapping("/detailData/{id}")
    @RequiresPermissions("metpMoveBill:view")
    @ResponseBody
    public AjaxResult detailData(@PathVariable("id") Long id) {
        MoveEcho data = metpMoveBillService.detail(id);
        if (data == null) {
            return AjaxResult.error("核准件不存在");
        }
        return AjaxResult.successData(200, data).put("msg", data.getMessage());
    }

    @Log(title = "画押·落押同意", action = "sign")
    @ApiOperation(value = "落押", notes = "签人取自登录态，门脸不得代传")
    @PostMapping("/sign")
    @RequiresPermissions("metpMoveBill:sign")
    @ResponseBody
    public AjaxResult sign(Long id, String roleKey, String opinion, String approveDocNo) {
        MoveRole role = MoveRole.of(roleKey);
        if (role == null) {
            return AjaxResult.error("未说明以哪一处的角色点印");
        }
        return echo(metpMoveBillService.sign(id, role, opinion, approveDocNo));
    }

    @Log(title = "画押·不予同意", action = "veto")
    @ApiOperation(value = "定否", notes = "件即终局冻死，要改另起新件")
    @PostMapping("/veto")
    @RequiresPermissions("metpMoveBill:sign")
    @ResponseBody
    public AjaxResult veto(Long id, String roleKey, String opinion) {
        MoveRole role = MoveRole.of(roleKey);
        if (role == null) {
            return AjaxResult.error("未说明以哪一处的角色定否");
        }
        return echo(metpMoveBillService.veto(id, role, opinion));
    }

    @Log(title = "画押·发回市局", action = "sendBack")
    @ApiOperation(value = "省局发回市局", notes = "前头签记录尽数划废，自市局重新攒起")
    @PostMapping("/sendBack")
    @RequiresPermissions("metpMoveBill:sign")
    @ResponseBody
    public AjaxResult sendBack(Long id, String opinion) {
        return echo(metpMoveBillService.sendBack(id, opinion));
    }

    @Log(title = "一键生成新址启用通知", action = "notice")
    @ApiOperation(value = "生成启用通知", notes = "各项自核准件带出，无人誊抄；一座台站一条")
    @PostMapping("/notice")
    @RequiresPermissions("metpMoveBill:notice")
    @ResponseBody
    public AjaxResult notice(Long id) {
        return echo(metpMoveBillService.generateNotice(id));
    }

    @ApiOperation(value = "逐层倒核", notes = "四处签记录逐步倒着核回来，带落定当时快照")
    @GetMapping("/trace/{id}")
    @RequiresPermissions("metpMoveBill:view")
    @ResponseBody
    public AjaxResult trace(@PathVariable("id") Long id) {
        return AjaxResult.successData(200, metpMoveBillService.traceBack(id));
    }

    /** 回音里有权柄：灵没灵、停在何处、还差谁，页面只照此摆，不自行预设。 */
    private AjaxResult echo(MoveEcho moveEcho) {
        if (moveEcho == null) {
            return AjaxResult.error("动作被拦下，未作数");
        }
        if (moveEcho.isEffective()) {
            return AjaxResult.successData(200, moveEcho).put("msg", moveEcho.getMessage());
        }
        return AjaxResult.error(moveEcho.getMessage());
    }
}
