-- Enterprise finance system schema for MySQL 8.0
-- Registration bootstrap data is included in this script

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `expense_claim_detail`;
DROP TABLE IF EXISTS `expense_claim_attachment`;
DROP TABLE IF EXISTS `expense_claim`;
DROP TABLE IF EXISTS `accounting_period`;
DROP TABLE IF EXISTS `settlement_method`;
DROP TABLE IF EXISTS `trade_partner`;
DROP TABLE IF EXISTS `voucher_entry`;
DROP TABLE IF EXISTS `subject_balance`;
DROP TABLE IF EXISTS `voucher`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `role_menu`;
DROP TABLE IF EXISTS `account_subject`;
DROP TABLE IF EXISTS `menu`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `dept`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `dept` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'parent dept id, root is 0',
  `dept_name` VARCHAR(100) NOT NULL COMMENT 'dept name',
  `sort` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `idx_dept_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='department';

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL COMMENT 'username',
  `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt password',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT 'nickname',
  `email` VARCHAR(100) DEFAULT NULL COMMENT 'email',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT 'mobile',
  `dept_id` BIGINT DEFAULT NULL COMMENT 'dept id',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `last_login_time` DATETIME DEFAULT NULL COMMENT 'last login time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_dept_id` (`dept_id`),
  CONSTRAINT `fk_user_dept` FOREIGN KEY (`dept_id`) REFERENCES `dept` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user';

CREATE TABLE `role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_name` VARCHAR(50) NOT NULL COMMENT 'role name',
  `role_code` VARCHAR(50) NOT NULL COMMENT 'role code',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role';

CREATE TABLE `user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT 'user id',
  `role_id` BIGINT NOT NULL COMMENT 'role id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role_user_role` (`user_id`, `role_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='user role relation';

CREATE TABLE `menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'parent menu id, root is 0',
  `menu_name` VARCHAR(50) NOT NULL COMMENT 'menu name',
  `path` VARCHAR(200) DEFAULT NULL COMMENT 'route path',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT 'permission code',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT 'icon',
  `sort` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
  `type` TINYINT(1) NOT NULL COMMENT '0 directory 1 menu 2 button',
  PRIMARY KEY (`id`),
  KEY `idx_menu_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='menu';

CREATE TABLE `role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL COMMENT 'role id',
  `menu_id` BIGINT NOT NULL COMMENT 'menu id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_menu_menu_id` (`menu_id`),
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='role menu relation';

CREATE TABLE `account_subject` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `subject_code` VARCHAR(50) NOT NULL COMMENT 'subject code',
  `subject_name` VARCHAR(100) NOT NULL COMMENT 'subject name',
  `subject_type` TINYINT(2) NOT NULL COMMENT '1 asset 2 liability 3 equity 4 cost 5 profit_loss',
  `parent_id` BIGINT DEFAULT NULL COMMENT 'parent subject id',
  `level` TINYINT(2) NOT NULL COMMENT 'subject level',
  `balance_direction` TINYINT(1) NOT NULL COMMENT '1 debit 2 credit',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  `has_auxiliary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'has auxiliary accounting',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`),
  KEY `idx_subject_parent_id` (`parent_id`),
  CONSTRAINT `fk_subject_parent` FOREIGN KEY (`parent_id`) REFERENCES `account_subject` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='account subject';

CREATE TABLE `trade_partner` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `partner_code` VARCHAR(50) NOT NULL COMMENT 'partner code',
  `partner_name` VARCHAR(100) NOT NULL COMMENT 'partner name',
  `partner_type` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1 customer 2 supplier 3 both',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT 'contact person',
  `phone` VARCHAR(30) DEFAULT NULL COMMENT 'contact phone',
  `address` VARCHAR(255) DEFAULT NULL COMMENT 'address',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_partner_code` (`partner_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='trade partner';

CREATE TABLE `settlement_method` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `method_code` VARCHAR(50) NOT NULL COMMENT 'method code',
  `method_name` VARCHAR(100) NOT NULL COMMENT 'method name',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0 disabled 1 enabled',
  `sort` INT NOT NULL DEFAULT 0 COMMENT 'sort order',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_settlement_method_code` (`method_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='settlement method';

CREATE TABLE `accounting_period` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `period` VARCHAR(6) NOT NULL COMMENT 'accounting period YYYYMM',
  `start_date` DATE NOT NULL COMMENT 'period start date',
  `end_date` DATE NOT NULL COMMENT 'period end date',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0 draft 1 opened 2 closed',
  `close_time` DATETIME DEFAULT NULL COMMENT 'close time',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_accounting_period_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='accounting period';

CREATE TABLE `voucher` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voucher_no` VARCHAR(50) NOT NULL COMMENT 'voucher number',
  `period` VARCHAR(6) NOT NULL COMMENT 'accounting period YYYYMM',
  `voucher_date` DATE DEFAULT NULL COMMENT 'voucher date',
  `summary` VARCHAR(255) DEFAULT NULL COMMENT 'summary',
  `maker_id` BIGINT DEFAULT NULL COMMENT 'maker id',
  `auditor_id` BIGINT DEFAULT NULL COMMENT 'auditor id',
  `bookkeeper_id` BIGINT DEFAULT NULL COMMENT 'bookkeeper id',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0 unaudited 1 audited 2 booked 3 cancelled',
  `attachment_count` INT NOT NULL DEFAULT 0 COMMENT 'attachment count',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_voucher_period_no` (`period`, `voucher_no`),
  KEY `idx_voucher_status` (`status`),
  KEY `idx_voucher_period` (`period`),
  CONSTRAINT `fk_voucher_maker` FOREIGN KEY (`maker_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_voucher_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_voucher_bookkeeper` FOREIGN KEY (`bookkeeper_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='voucher';

CREATE TABLE `voucher_entry` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `voucher_id` BIGINT NOT NULL COMMENT 'voucher id',
  `subject_id` BIGINT NOT NULL COMMENT 'subject id',
  `summary` VARCHAR(255) DEFAULT NULL COMMENT 'summary',
  `debit_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'debit amount',
  `credit_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'credit amount',
  `auxiliary_type` TINYINT(2) DEFAULT NULL COMMENT 'auxiliary type',
  `auxiliary_id` BIGINT DEFAULT NULL COMMENT 'auxiliary id',
  PRIMARY KEY (`id`),
  KEY `idx_voucher_entry_voucher_id` (`voucher_id`),
  KEY `idx_voucher_entry_subject_id` (`subject_id`),
  CONSTRAINT `fk_voucher_entry_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_voucher_entry_subject` FOREIGN KEY (`subject_id`) REFERENCES `account_subject` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='voucher entry';

CREATE TABLE `subject_balance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `subject_id` BIGINT NOT NULL COMMENT 'subject id',
  `period` VARCHAR(6) NOT NULL COMMENT 'accounting period',
  `opening_debit` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'opening debit',
  `opening_credit` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'opening credit',
  `debit_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'current debit amount',
  `credit_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'current credit amount',
  `ending_debit` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'ending debit',
  `ending_credit` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'ending credit',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_balance_subject_period` (`subject_id`, `period`),
  KEY `idx_subject_balance_period` (`period`),
  CONSTRAINT `fk_subject_balance_subject` FOREIGN KEY (`subject_id`) REFERENCES `account_subject` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='subject balance';

CREATE TABLE `expense_claim` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `claim_no` VARCHAR(50) NOT NULL COMMENT 'claim number',
  `period` VARCHAR(6) NOT NULL COMMENT 'accounting period YYYYMM',
  `reason` VARCHAR(255) NOT NULL COMMENT 'claim reason',
  `total_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'total amount',
  `expense_type` TINYINT(2) NOT NULL COMMENT 'expense type',
  `trade_partner_id` BIGINT DEFAULT NULL COMMENT 'trade partner id',
  `settlement_method_id` BIGINT DEFAULT NULL COMMENT 'settlement method id',
  `applicant_id` BIGINT DEFAULT NULL COMMENT 'applicant id',
  `dept_id` BIGINT DEFAULT NULL COMMENT 'dept id',
  `status` TINYINT(2) NOT NULL COMMENT '0 draft 1 pending 2 approved 3 paid 4 rejected',
  `approver_id` BIGINT DEFAULT NULL COMMENT 'approver id',
  `approve_time` DATETIME DEFAULT NULL COMMENT 'approve time',
  `payer_id` BIGINT DEFAULT NULL COMMENT 'payer id',
  `payment_time` DATETIME DEFAULT NULL COMMENT 'payment time',
  `voucher_id` BIGINT DEFAULT NULL COMMENT 'related voucher id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_expense_claim_no` (`claim_no`),
  KEY `idx_expense_claim_status` (`status`),
  KEY `idx_expense_claim_dept_id` (`dept_id`),
  CONSTRAINT `fk_expense_claim_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_dept` FOREIGN KEY (`dept_id`) REFERENCES `dept` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_partner` FOREIGN KEY (`trade_partner_id`) REFERENCES `trade_partner` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_settlement` FOREIGN KEY (`settlement_method_id`) REFERENCES `settlement_method` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_payer` FOREIGN KEY (`payer_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='expense claim';

CREATE TABLE `expense_claim_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `expense_claim_id` BIGINT NOT NULL COMMENT 'expense claim id',
  `line_no` INT NOT NULL DEFAULT 1 COMMENT 'line number',
  `item_name` VARCHAR(100) NOT NULL COMMENT 'item name',
  `subject_id` BIGINT DEFAULT NULL COMMENT 'account subject id',
  `expense_type` TINYINT(2) NOT NULL COMMENT 'expense type',
  `amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'amount',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'remark',
  PRIMARY KEY (`id`),
  KEY `idx_expense_claim_detail_claim_id` (`expense_claim_id`),
  KEY `idx_expense_claim_detail_subject_id` (`subject_id`),
  CONSTRAINT `fk_expense_claim_detail_claim` FOREIGN KEY (`expense_claim_id`) REFERENCES `expense_claim` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_expense_claim_detail_subject` FOREIGN KEY (`subject_id`) REFERENCES `account_subject` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='expense claim detail';

CREATE TABLE `expense_claim_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `expense_claim_id` BIGINT NOT NULL COMMENT 'expense claim id',
  `file_name` VARCHAR(255) NOT NULL COMMENT 'file name',
  `file_path` VARCHAR(500) NOT NULL COMMENT 'stored file path',
  `file_size` BIGINT NOT NULL DEFAULT 0 COMMENT 'file size bytes',
  `content_type` VARCHAR(120) DEFAULT NULL COMMENT 'content type',
  `uploader_id` BIGINT DEFAULT NULL COMMENT 'uploader user id',
  `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'upload time',
  PRIMARY KEY (`id`),
  KEY `idx_expense_claim_attachment_claim_id` (`expense_claim_id`),
  CONSTRAINT `fk_expense_claim_attachment_claim` FOREIGN KEY (`expense_claim_id`) REFERENCES `expense_claim` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='expense claim attachment';

CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT 'user id',
  `username` VARCHAR(50) DEFAULT NULL COMMENT 'username',
  `operation` VARCHAR(100) DEFAULT NULL COMMENT 'operation',
  `method` VARCHAR(255) DEFAULT NULL COMMENT 'request method',
  `params` TEXT DEFAULT NULL COMMENT 'request params',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'ip address',
  `duration` INT NOT NULL DEFAULT 0 COMMENT 'duration ms',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_user_id` (`user_id`),
  KEY `idx_operation_log_create_time` (`create_time`),
  CONSTRAINT `fk_operation_log_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='operation log';

-- Registration bootstrap data
INSERT INTO `dept` (`id`, `parent_id`, `dept_name`, `sort`, `status`)
SELECT 1, 0, 'DEFAULT_DEPT', 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `dept` WHERE `id` = 1
);

INSERT INTO `role` (`id`, `role_name`, `role_code`, `status`)
SELECT 1, 'System Administrator', 'ADMIN', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `role` WHERE `role_code` = 'ADMIN'
);

INSERT INTO `role` (`id`, `role_name`, `role_code`, `status`)
SELECT 2, 'Accountant', 'ACCOUNTANT', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `role` WHERE `role_code` = 'ACCOUNTANT'
);

INSERT INTO `role` (`id`, `role_name`, `role_code`, `status`)
SELECT 3, 'Employee', 'EMPLOYEE', 1
WHERE NOT EXISTS (
  SELECT 1 FROM `role` WHERE `role_code` = 'EMPLOYEE'
);

-- Minimal account subjects for voucher creation
INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 1, '1001', 'Cash', 1, NULL, 1, 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '1001'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 2, '1002', 'Bank Deposit', 1, NULL, 1, 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '1002'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 3, '1122', 'Accounts Receivable', 1, NULL, 1, 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '1122'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 4, '2202', 'Accounts Payable', 2, NULL, 1, 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '2202'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 5, '5001', 'Main Business Revenue', 5, NULL, 1, 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '5001'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 6, '5401', 'Main Business Cost', 4, NULL, 1, 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '5401'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 7, '5601', 'Management Expense', 5, NULL, 1, 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '5601'
);

INSERT INTO `account_subject` (`id`, `subject_code`, `subject_name`, `subject_type`, `parent_id`, `level`, `balance_direction`, `status`, `has_auxiliary`)
SELECT 8, '2221', 'Tax Payable', 2, NULL, 1, 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `account_subject` WHERE `subject_code` = '2221'
);

INSERT INTO `trade_partner` (`id`, `partner_code`, `partner_name`, `partner_type`, `contact_name`, `phone`, `address`, `status`, `remark`)
SELECT 1, 'CUST001', '示例客户', 1, '张三', '13800000001', '上海市浦东新区', 1, '默认客户示例'
WHERE NOT EXISTS (
  SELECT 1 FROM `trade_partner` WHERE `partner_code` = 'CUST001'
);

INSERT INTO `trade_partner` (`id`, `partner_code`, `partner_name`, `partner_type`, `contact_name`, `phone`, `address`, `status`, `remark`)
SELECT 2, 'SUP001', '示例供应商', 2, '李四', '13800000002', '杭州市西湖区', 1, '默认供应商示例'
WHERE NOT EXISTS (
  SELECT 1 FROM `trade_partner` WHERE `partner_code` = 'SUP001'
);

INSERT INTO `settlement_method` (`id`, `method_code`, `method_name`, `status`, `sort`, `remark`)
SELECT 1, 'CASH', '现金', 1, 1, '默认结算方式'
WHERE NOT EXISTS (
  SELECT 1 FROM `settlement_method` WHERE `method_code` = 'CASH'
);

INSERT INTO `settlement_method` (`id`, `method_code`, `method_name`, `status`, `sort`, `remark`)
SELECT 2, 'BANK', '银行转账', 1, 2, '默认结算方式'
WHERE NOT EXISTS (
  SELECT 1 FROM `settlement_method` WHERE `method_code` = 'BANK'
);

INSERT INTO `settlement_method` (`id`, `method_code`, `method_name`, `status`, `sort`, `remark`)
SELECT 3, 'ALIPAY', '支付宝', 1, 3, '默认结算方式'
WHERE NOT EXISTS (
  SELECT 1 FROM `settlement_method` WHERE `method_code` = 'ALIPAY'
);

INSERT INTO `accounting_period` (`id`, `period`, `start_date`, `end_date`, `status`, `remark`)
SELECT 1, '202601', '2026-01-01', '2026-01-31', 2, '已结账期间示例'
WHERE NOT EXISTS (
  SELECT 1 FROM `accounting_period` WHERE `period` = '202601'
);

INSERT INTO `accounting_period` (`id`, `period`, `start_date`, `end_date`, `status`, `remark`)
SELECT 2, '202602', '2026-02-01', '2026-02-28', 1, '当前启用期间示例'
WHERE NOT EXISTS (
  SELECT 1 FROM `accounting_period` WHERE `period` = '202602'
);

-- Registration related tables:
-- user: stores account, encrypted password, nickname, email, mobile, dept and status
-- role: stores role definitions, self-registration defaults to EMPLOYEE
-- user_role: stores the mapping between users and roles
