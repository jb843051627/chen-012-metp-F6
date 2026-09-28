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
  site_no varchar(64) DEFAULT NULL COMMENT '台站编号',
  site_name varchar(128) DEFAULT NULL COMMENT '台站名称',
  reason varchar(500) DEFAULT NULL COMMENT '申请事由',
  new_site_no varchar(64) DEFAULT NULL COMMENT '新址编号',
  new_lng decimal(12,6) DEFAULT NULL COMMENT '新址经度',
  new_lat decimal(12,6) DEFAULT NULL COMMENT '新址纬度',
  new_alt decimal(12,2) DEFAULT NULL COMMENT '新址海拔(米)',
  surface varchar(500) DEFAULT NULL COMMENT '新址下垫面情形',
  old_field_status varchar(500) DEFAULT NULL COMMENT '旧址观测场现状',
  compare_start datetime DEFAULT NULL COMMENT '新址对比观测起始时刻',
  compare_end datetime DEFAULT NULL COMMENT '新址对比观测截止时刻',
  new_station_built int DEFAULT '0' COMMENT '新站是否已建起 0未起 1已起(先建后迁把关依据,新站未起不许拆旧站)',
  approve_doc_no varchar(64) DEFAULT NULL COMMENT '国务院气象主管机构批准文号',
  round_no int DEFAULT '1' COMMENT '攒签轮次(发回市局一次加一轮,废签留旧轮)',
  node_no int DEFAULT NULL COMMENT '当前所在层 0台站 1市局 2省局 3国务院气象主管机构',
  sign_mode int DEFAULT NULL COMMENT '同层核准方式 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本层应签人数',
  sign_count int DEFAULT NULL COMMENT '本层已签人数',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回重审 3不予同意(终局)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_bill_no (bill_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='台站迁移变更逐级核准单';

-- 画押记录：每一次落押/定否/驳回各存一条；同一分钟两名省局会签也是两条，绝不并条。
-- 发回市局不删旧签，旧轮记录 valid=0 封存，台账只摆当前轮，废签与新签不同堂并摆。
CREATE TABLE IF NOT EXISTS t_metp_move_sign (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint NOT NULL COMMENT '核准单主键',
  round_no int NOT NULL COMMENT '落押时所在攒签轮次',
  node_no int NOT NULL COMMENT '所处层 0台站 1市局 2省局 3国务院气象主管机构',
  role_key varchar(32) NOT NULL COMMENT '点印角色 STATION/CITY/PROV_COMPARE/PROV_ORDER/NATIONAL',
  signer varchar(64) NOT NULL COMMENT '落押人(服务层取自当前登录人,门脸不得代传)',
  action int NOT NULL COMMENT '动作 1落押同意 2不予同意 3发回市局',
  opinion varchar(500) DEFAULT NULL COMMENT '签批意见',
  snapshot text DEFAULT NULL COMMENT '落定当时单据全样(屏幕所见即行内所存)',
  valid_flag int DEFAULT '1' COMMENT '本轮是否有效 1有效 0发回后划废',
  sign_time datetime DEFAULT NULL COMMENT '落押时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_move_sign_bill (bill_id, round_no, valid_flag, node_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='台站迁移逐级画押记录';

-- 新址启用通知：一律由已核讫的核准件一键带出，无人二次誊抄；一座台站只许一条。
CREATE TABLE IF NOT EXISTS t_metp_move_notice (
  id bigint NOT NULL COMMENT '主键',
  notice_no varchar(64) DEFAULT NULL COMMENT '新址启用通知编号',
  bill_id bigint NOT NULL COMMENT '来源核准单主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '来源核准单号',
  site_no varchar(64) DEFAULT NULL COMMENT '台站编号',
  site_name varchar(128) DEFAULT NULL COMMENT '台站名称',
  new_site_no varchar(64) DEFAULT NULL COMMENT '新址编号',
  new_lng decimal(12,6) DEFAULT NULL COMMENT '新址经度',
  new_lat decimal(12,6) DEFAULT NULL COMMENT '新址纬度',
  new_alt decimal(12,2) DEFAULT NULL COMMENT '新址海拔(米)',
  compare_start datetime DEFAULT NULL COMMENT '对比观测起始时刻',
  compare_end datetime DEFAULT NULL COMMENT '对比观测截止时刻',
  approve_doc_no varchar(64) DEFAULT NULL COMMENT '批准文号',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_move_notice_bill (bill_id),
  UNIQUE KEY uk_move_notice_no (notice_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='台站新址启用通知';

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

