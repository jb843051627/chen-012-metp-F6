-- metp 气象探测环境保护与气象观测数据质量控制管理 -- schema (chen-012)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_012

CREATE TABLE IF NOT EXISTS t_metp_duty_task (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '到期派工单号',
  due_at datetime DEFAULT NULL COMMENT '本项标定应当到期的日终截止时刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '单条提前派发的自然日数',
  status int DEFAULT NULL COMMENT '条目情形 0待派 1已派 2派不出去',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标定巡检到期派工条目';

CREATE TABLE IF NOT EXISTS t_metp_elem_card (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '观测要素挂用卡号',
  site_id int DEFAULT NULL COMMENT '所属气象台站基础档案',
  site_no varchar(64) DEFAULT NULL COMMENT '气象台站编号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本卡三源合并后在册要素项数(项)',
  fine_amt decimal(12,2) DEFAULT NULL COMMENT '本年度新增要素项数(项)',
  grade_level int DEFAULT NULL COMMENT '资料保障等级档',
  status int DEFAULT NULL COMMENT '进展 0待核对 1已核对 2已定档',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='观测要素挂用建档卡';

CREATE TABLE IF NOT EXISTS t_metp_fix_card (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '探测环境违规整改卡号',
  stage int DEFAULT NULL COMMENT '当前关口 0..5',
  status int DEFAULT NULL COMMENT '卡的落定 0在办 1已解除 2已归档',
  content varchar(255) DEFAULT NULL COMMENT '查处记事',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次过口动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='探测环境违规整改事务卡';

CREATE TABLE IF NOT EXISTS t_metp_move_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '台站迁移变更核准单号',
  site_no varchar(64) DEFAULT NULL COMMENT '申请台站编号',
  site_name varchar(128) DEFAULT NULL COMMENT '申请台站名称',
  apply_reason varchar(500) DEFAULT NULL COMMENT '申请事由(台站一阶材料)',
  new_site_no varchar(64) DEFAULT NULL COMMENT '新址台站编号(启用通知誊带)',
  new_lng varchar(32) DEFAULT NULL COMMENT '新址经度(台站一阶材料)',
  new_lat varchar(32) DEFAULT NULL COMMENT '新址纬度(台站一阶材料)',
  new_alt decimal(12,2) DEFAULT NULL COMMENT '新址海拔(米,台站一阶材料)',
  surface varchar(255) DEFAULT NULL COMMENT '下垫面情形(台站一阶材料)',
  old_site_status varchar(255) DEFAULT NULL COMMENT '旧址观测场现状(台站一阶材料)',
  compare_start datetime DEFAULT NULL COMMENT '新址对比观测起始时刻',
  compare_end datetime DEFAULT NULL COMMENT '新址对比观测截止时刻',
  approve_no varchar(64) DEFAULT NULL COMMENT '国务院气象主管机构批准文号(第四处落押携入)',
  round_no int DEFAULT '1' COMMENT '核准轮次 省局每驳回一次加一 废签按轮次划开',
  node_no int DEFAULT NULL COMMENT '当前所在层 0台站 1市局 2省局 3国务院气象主管机构',
  sign_mode int DEFAULT NULL COMMENT '同层核准方式 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本层应签人数(服务层按层配置写入)',
  sign_count int DEFAULT NULL COMMENT '本层已签人数(只由画押记录派生回写)',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2不予同意(终态冻单)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='台站迁移变更逐级核准单';

-- 画押记录：只增不改。驳回时前轮记录原样留痕，凭 round_no 与新签划开；同层同一职权一轮只许一枚有效押。
CREATE TABLE IF NOT EXISTS t_metp_move_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '所属核准单',
  node_no int NOT NULL COMMENT '落押层 0台站 1市局 2省局 3国务院气象主管机构',
  role_key varchar(32) NOT NULL COMMENT '职权代号 STATION_APPLY/CITY_ENV/PROV_COMPARE/PROV_BUILD/NATION_APPROVE/PROV_REJECT',
  signer varchar(64) NOT NULL COMMENT '画押人登录账号(取自登录身份,不由页面带入)',
  action varchar(16) NOT NULL COMMENT '动作 AGREE落押 VETO不予同意 REJECT驳回',
  opinion varchar(500) DEFAULT NULL COMMENT '押下所附意见',
  round_no int NOT NULL COMMENT '画押时所属轮次',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_sign_one_role (bill_id, node_no, role_key, action, round_no),
  KEY idx_move_sign_bill_round (bill_id, round_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='台站迁移核准画押记录(只增不改)';

-- 新址启用通知：核讫后由服务层一键誊带生成，核准单与通知一对一。
CREATE TABLE IF NOT EXISTS t_metp_move_notice (
  id bigint NOT NULL COMMENT '主键',
  notice_no varchar(64) DEFAULT NULL COMMENT '新址启用通知编号',
  bill_id bigint NOT NULL COMMENT '来源核准单',
  bill_no varchar(64) DEFAULT NULL COMMENT '来源核准单号',
  site_no varchar(64) DEFAULT NULL COMMENT '原台站编号',
  new_site_no varchar(64) DEFAULT NULL COMMENT '新址编号(誊自核准单)',
  new_lng varchar(32) DEFAULT NULL COMMENT '新址经度(誊自核准单)',
  new_lat varchar(32) DEFAULT NULL COMMENT '新址纬度(誊自核准单)',
  new_alt decimal(12,2) DEFAULT NULL COMMENT '新址海拔(米,誊自核准单)',
  compare_start datetime DEFAULT NULL COMMENT '对比观测起始(誊自核准单)',
  compare_end datetime DEFAULT NULL COMMENT '对比观测截止(誊自核准单)',
  approve_no varchar(64) DEFAULT NULL COMMENT '批准文号(誊自核准单)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_notice_bill (bill_id),
  UNIQUE KEY uk_move_notice_no (notice_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='新址启用通知(核准后一键誊带)';

CREATE TABLE IF NOT EXISTS t_metp_qc_line (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '观测质控界限代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '质控界限名称',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '一档要素偏差封顶值',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '二档要素偏差封顶值',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '三档要素偏差封顶值',
  eff_start datetime DEFAULT NULL COMMENT '启用之日',
  eff_end datetime DEFAULT NULL COMMENT '交棒之日(不含)',
  priority int DEFAULT NULL COMMENT '并档顺位(数值越大越优先)',
  status int DEFAULT NULL COMMENT '界限的情形 0现行 1已停用',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='观测数据质量控制界限';

CREATE TABLE IF NOT EXISTS t_metp_stn_doc (
  id bigint NOT NULL COMMENT '主键',
  site_no varchar(64) DEFAULT NULL COMMENT '气象台站编号',
  site_name varchar(128) DEFAULT NULL COMMENT '台站基础档案名称',
  site_type varchar(32) DEFAULT NULL COMMENT '台站类别类',
  road_name varchar(128) DEFAULT NULL COMMENT '所在地(省—市—区县)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '在册观测要素数一档上限(项)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '二档上限(项)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '三档上限(项)',
  status int DEFAULT NULL COMMENT '档案情形 0在册 1已停测',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='气象台站基础档案';

CREATE TABLE IF NOT EXISTS t_metp_submit_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '观测数据报送册号',
  row_no int DEFAULT NULL COMMENT '原册内行次',
  item_code varchar(64) DEFAULT NULL COMMENT '时次要素代号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行填报记录条数(条)',
  status int DEFAULT NULL COMMENT '行进展 0待销 1已转存 2挂驳回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='正点观测数据核收转存行';

-- 初始档案数据（id=1 启用 / id=2 停用）
-- 两条初始档案：id=0 在册、id=1 已停测。
-- 在册观测要素项数上限取 12/40/120 项（等于上限算高一档）。
INSERT IGNORE INTO t_metp_stn_doc (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'TQ00', '江南省临川国家基本气象站基础档案', '国家基本气象站', '江南省—临川市—城东区', 12.00, 40.00, 120.00, 0, 0, 'seed', NOW()),
       (1, 'TQ01', '旧临川市西郊气象观测站基础档案（已停测）', '一般气象站', '江南省—临川市—城西区', 12.00, 40.00, 120.00, 1, 0, 'seed', NOW());


-- 「画押」门牌号菜单与按钮权限（挂在系统管理根目录 411522822607867904 之下；可按需改挂）
INSERT IGNORE INTO t_sys_permission
(id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES
(910100000000000001, '台站迁移逐级核准', '画押：落押、定否、驳回', '/metpMoveBill/view', 0, 411522822607867904, 'gen:metpMoveBill:view', 1, 'fa fa-gavel', 20, 0, 'seed', NOW()),
(910100000000000002, '核准台账', '核准单只读列示', '/metpMoveBill/list', 0, 910100000000000001, 'gen:metpMoveBill:list', 2, NULL, 1, 0, 'seed', NOW()),
(910100000000000003, '台站立单申报', '材料缺一项报不上去', '/metpMoveBill/submit', 0, 910100000000000001, 'gen:metpMoveBill:submit', 2, NULL, 2, 0, 'seed', NOW()),
(910100000000000004, '落押', '市局/省局两职权/国家批复', '/metpMoveBill/agree', 0, 910100000000000001, 'gen:metpMoveBill:agree', 2, NULL, 3, 0, 'seed', NOW()),
(910100000000000005, '定否', '不予同意即刻冻单', '/metpMoveBill/veto', 0, 910100000000000001, 'gen:metpMoveBill:veto', 2, NULL, 4, 0, 'seed', NOW()),
(910100000000000006, '省局驳回', '发回市局，前轮签划去留痕', '/metpMoveBill/reject', 0, 910100000000000001, 'gen:metpMoveBill:reject', 2, NULL, 5, 0, 'seed', NOW()),
(910100000000000007, '新址启用通知', '核讫一键誊带生成', '/metpMoveBill/notice', 0, 910100000000000001, 'gen:metpMoveBill:notice', 2, NULL, 6, 0, 'seed', NOW());
