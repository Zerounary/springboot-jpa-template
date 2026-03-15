CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  real_name VARCHAR(20) NULL,
  phone VARCHAR(11) NULL,
  id_card VARCHAR(18) NULL,
  gender TINYINT NULL,
  role_type TINYINT NOT NULL DEFAULT 3,
  status TINYINT NOT NULL DEFAULT 1,
  avatar VARCHAR(255) NULL,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_phone (phone),
  UNIQUE KEY uk_users_id_card (id_card)
);

SET @db := DATABASE();

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN real_name VARCHAR(20) NULL', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'real_name';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN phone VARCHAR(11) NULL', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'phone';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN id_card VARCHAR(18) NULL', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'id_card';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN gender TINYINT NULL', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'gender';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN role_type TINYINT NOT NULL DEFAULT 3', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role_type';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN status TINYINT NOT NULL DEFAULT 1', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'status';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN avatar VARCHAR(255) NULL', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'ALTER TABLE users ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'is_deleted';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'CREATE UNIQUE INDEX uk_users_phone ON users(phone)', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND INDEX_NAME = 'uk_users_phone';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT IF(COUNT(*) = 0, 'CREATE UNIQUE INDEX uk_users_id_card ON users(id_card)', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND INDEX_NAME = 'uk_users_id_card';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_role (
  role_id BIGINT NOT NULL AUTO_INCREMENT,
  role_name VARCHAR(30) NOT NULL,
  role_code VARCHAR(30) NOT NULL,
  description VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (role_id),
  UNIQUE KEY uk_sys_role_name (role_name),
  UNIQUE KEY uk_sys_role_code (role_code)
);

CREATE TABLE IF NOT EXISTS sys_permission (
  perm_id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT NOT NULL DEFAULT 0,
  perm_name VARCHAR(50) NOT NULL,
  perm_code VARCHAR(100) NULL,
  perm_type TINYINT NOT NULL,
  path VARCHAR(255) NULL,
  icon VARCHAR(100) NULL,
  sort INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (perm_id),
  UNIQUE KEY uk_sys_permission_code (perm_code)
);

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_user_role_user_id (user_id),
  KEY idx_sys_user_role_role_id (role_id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sys_role_perm_role_id (role_id),
  KEY idx_sys_role_perm_perm_id (perm_id)
);

CREATE TABLE IF NOT EXISTS hospital_department (
  dept_id BIGINT NOT NULL AUTO_INCREMENT,
  dept_name VARCHAR(50) NOT NULL,
  dept_code VARCHAR(30) NOT NULL,
  parent_id BIGINT NOT NULL DEFAULT 0,
  description VARCHAR(255) NULL,
  sort INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (dept_id),
  UNIQUE KEY uk_department_name (dept_name),
  UNIQUE KEY uk_department_code (dept_code)
);

CREATE TABLE IF NOT EXISTS doctor_info (
  doctor_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  job_title VARCHAR(30) NOT NULL,
  specialty VARCHAR(255) NOT NULL,
  introduction TEXT NULL,
  registration_fee DECIMAL(10,2) NOT NULL,
  schedule VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (doctor_id),
  UNIQUE KEY uk_doctor_user_id (user_id),
  KEY idx_doctor_dept_id (dept_id)
);

CREATE TABLE IF NOT EXISTS patient_info (
  patient_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  birth_date DATE NULL,
  age INT NULL,
  blood_type VARCHAR(10) NULL,
  marital_status TINYINT NULL,
  address VARCHAR(255) NULL,
  emergency_contact VARCHAR(20) NULL,
  emergency_phone VARCHAR(11) NULL,
  allergy_history TEXT NULL,
  past_medical_history TEXT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (patient_id),
  UNIQUE KEY uk_patient_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS registration_record (
  registration_id BIGINT NOT NULL AUTO_INCREMENT,
  registration_no VARCHAR(30) NOT NULL,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  schedule_date DATE NOT NULL,
  time_slot VARCHAR(30) NOT NULL,
  registration_fee DECIMAL(10,2) NOT NULL,
  pay_status TINYINT NOT NULL DEFAULT 0,
  registration_status TINYINT NOT NULL DEFAULT 0,
  visit_serial_number INT NULL,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (registration_id),
  UNIQUE KEY uk_registration_no (registration_no),
  KEY idx_registration_patient_id (patient_id),
  KEY idx_registration_doctor_id (doctor_id),
  KEY idx_registration_dept_id (dept_id)
);

CREATE TABLE IF NOT EXISTS medical_record (
  record_id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  registration_id BIGINT NULL,
  visit_date DATETIME NOT NULL,
  chief_complaint TEXT NOT NULL,
  present_illness TEXT NULL,
  past_history TEXT NULL,
  physical_examination TEXT NULL,
  auxiliary_examination TEXT NULL,
  diagnosis VARCHAR(255) NOT NULL,
  treatment_plan TEXT NULL,
  record_status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (record_id),
  KEY idx_record_patient_id (patient_id),
  KEY idx_record_doctor_id (doctor_id),
  KEY idx_record_dept_id (dept_id),
  KEY idx_record_registration_id (registration_id)
);

CREATE TABLE IF NOT EXISTS health_monitor (
  monitor_id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  monitor_date DATETIME NOT NULL,
  systolic_pressure INT NULL,
  diastolic_pressure INT NULL,
  blood_glucose DECIMAL(5,2) NULL,
  heart_rate INT NULL,
  body_temperature DECIMAL(3,1) NULL,
  weight DECIMAL(5,2) NULL,
  remark VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (monitor_id),
  KEY idx_monitor_patient_id (patient_id)
);

CREATE TABLE IF NOT EXISTS patient_cluster (
  cluster_id BIGINT NOT NULL AUTO_INCREMENT,
  cluster_version VARCHAR(30) NOT NULL,
  patient_id BIGINT NOT NULL,
  cluster_group INT NOT NULL,
  group_name VARCHAR(50) NOT NULL,
  feature_vector TEXT NULL,
  cluster_time DATETIME NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (cluster_id),
  KEY idx_cluster_patient_id (patient_id)
);

CREATE TABLE IF NOT EXISTS system_news (
  news_id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(30) NOT NULL,
  cover_image VARCHAR(255) NULL,
  content LONGTEXT NOT NULL,
  view_count INT NOT NULL DEFAULT 0,
  is_top TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  publish_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (news_id)
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
  log_id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(50) NULL,
  operation_module VARCHAR(50) NOT NULL,
  operation_type VARCHAR(30) NOT NULL,
  operation_content VARCHAR(500) NULL,
  request_method VARCHAR(10) NULL,
  request_url VARCHAR(255) NULL,
  ip_address VARCHAR(50) NULL,
  operation_status TINYINT NOT NULL,
  error_msg TEXT NULL,
  operation_time DATETIME NOT NULL,
  cost_time BIGINT NULL,
  PRIMARY KEY (log_id),
  KEY idx_oplog_user_id (user_id)
);

INSERT INTO sys_role (role_name, role_code, description)
SELECT '系统管理员', 'ADMIN', '初始化管理员角色'
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role WHERE role_code = 'ADMIN'
);

INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'admin', '$2a$12$oGAygCf6f5qUOfhItnFdVOoEY4S4t9B3e6DbapEtGF7Noy2cHBGbe', '管理员', 'admin@example.com', '系统管理员', 1, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE username = 'admin'
);

UPDATE users
SET password_hash = '$2a$12$oGAygCf6f5qUOfhItnFdVOoEY4S4t9B3e6DbapEtGF7Noy2cHBGbe',
    nickname = '管理员',
    email = 'admin@example.com',
    real_name = '系统管理员',
    role_type = 1,
    status = 1,
    is_deleted = 0,
    updated_at = NOW()
WHERE username = 'admin';

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.role_id
FROM users u
JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1
    FROM sys_user_role sur
    WHERE sur.user_id = u.id AND sur.role_id = r.role_id
  );
