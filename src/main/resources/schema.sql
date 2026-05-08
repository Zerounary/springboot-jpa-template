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
  daily_appointment_limit INT NULL DEFAULT 20,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (doctor_id),
  UNIQUE KEY uk_doctor_user_id (user_id),
  KEY idx_doctor_dept_id (dept_id)
);

SELECT IF(COUNT(*) = 0, 'ALTER TABLE doctor_info ADD COLUMN daily_appointment_limit INT NULL DEFAULT 20 AFTER schedule', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'doctor_info' AND COLUMN_NAME = 'daily_appointment_limit';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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

SELECT IF(COUNT(*) = 0, 'ALTER TABLE medical_record ADD COLUMN record_status TINYINT NOT NULL DEFAULT 1 AFTER treatment_plan', 'SELECT 1')
INTO @sql
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'medical_record' AND COLUMN_NAME = 'record_status';
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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
SELECT 'admin', 'admin123', '管理员', 'admin@example.com', '系统管理员', 1, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE username = 'admin'
);

UPDATE users
SET password_hash = 'admin123',
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

-- Insert sample departments
INSERT INTO hospital_department (dept_name, dept_code, parent_id, description, sort, status, create_time, update_time, is_deleted)
SELECT * FROM (
SELECT '内科' as dept_name, 'NK' as dept_code, 0 as parent_id, '内科科室' as description, 1 as sort, 1 as status, NOW() as create_time, NOW() as update_time, 0 as is_deleted UNION ALL
SELECT '外科', 'WK', 0, '外科科室', 2, 1, NOW(), NOW(), 0 UNION ALL
SELECT '妇产科', 'FCK', 0, '妇产科科室', 3, 1, NOW(), NOW(), 0 UNION ALL
SELECT '儿科', 'EK', 0, '儿科科室', 4, 1, NOW(), NOW(), 0 UNION ALL
SELECT '眼科', 'YK', 0, '眼科科室', 5, 1, NOW(), NOW(), 0 UNION ALL
SELECT '耳鼻喉科', 'EBHK', 0, '耳鼻喉科科室', 6, 1, NOW(), NOW(), 0 UNION ALL
SELECT '皮肤科', 'PFK', 0, '皮肤科科室', 7, 1, NOW(), NOW(), 0 UNION ALL
SELECT '中医科', 'ZK', 0, '中医科科室', 8, 1, NOW(), NOW(), 0 UNION ALL
SELECT '口腔科', 'KQK', 0, '口腔科科室', 9, 1, NOW(), NOW(), 0 UNION ALL
SELECT '急诊科', 'JZK', 0, '急诊科科室', 10, 1, NOW(), NOW(), 0
) AS dept_data
WHERE NOT EXISTS (SELECT 1 FROM hospital_department);

-- Insert sample doctors
INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'doctor001', 'doctor123', '李明医生', 'liming@hospital.com', '李明', 2, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'doctor001');

INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'doctor002', 'doctor123', '王芳医生', 'wangfang@hospital.com', '王芳', 2, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'doctor002');

INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'doctor003', 'doctor123', '张伟医生', 'zhangwei@hospital.com', '张伟', 2, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'doctor003');

INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'doctor004', 'doctor123', '刘静医生', 'liujing@hospital.com', '刘静', 2, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'doctor004');

INSERT INTO users (username, password_hash, nickname, email, real_name, role_type, status, is_deleted, created_at, updated_at)
SELECT 'doctor005', 'doctor123', '陈涛医生', 'chentao@hospital.com', '陈涛', 2, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'doctor005');

-- Insert doctor info
INSERT INTO doctor_info (user_id, dept_id, job_title, specialty, introduction, registration_fee, schedule, create_time, update_time, is_deleted)
SELECT 
    u.id, 
    hd.dept_id, 
    '主任医师' as job_title,
    '心血管疾病、高血压、冠心病' as specialty,
    '从事内科临床工作20年，擅长心血管疾病的诊治' as introduction,
    50.00 as registration_fee,
    '周一、三、五上午' as schedule,
    NOW(), NOW(), 0
FROM users u, hospital_department hd
WHERE u.username = 'doctor001' AND hd.dept_code = 'NK'
AND NOT EXISTS (SELECT 1 FROM doctor_info WHERE user_id = u.id);

INSERT INTO doctor_info (user_id, dept_id, job_title, specialty, introduction, registration_fee, schedule, create_time, update_time, is_deleted)
SELECT 
    u.id, 
    hd.dept_id, 
    '副主任医师' as job_title,
    '妇科炎症、月经不调、不孕不育' as specialty,
    '从事妇产科工作15年，经验丰富' as introduction,
    45.00 as registration_fee,
    '周二、四上午' as schedule,
    NOW(), NOW(), 0
FROM users u, hospital_department hd
WHERE u.username = 'doctor002' AND hd.dept_code = 'FCK'
AND NOT EXISTS (SELECT 1 FROM doctor_info WHERE user_id = u.id);

INSERT INTO doctor_info (user_id, dept_id, job_title, specialty, introduction, registration_fee, schedule, create_time, update_time, is_deleted)
SELECT 
    u.id, 
    hd.dept_id, 
    '主治医师' as job_title,
    '小儿感冒、肺炎、消化不良' as specialty,
    '儿科专家，对儿童常见病有独到见解' as introduction,
    40.00 as registration_fee,
    '周一至周五全天' as schedule,
    NOW(), NOW(), 0
FROM users u, hospital_department hd
WHERE u.username = 'doctor003' AND hd.dept_code = 'EK'
AND NOT EXISTS (SELECT 1 FROM doctor_info WHERE user_id = u.id);

INSERT INTO doctor_info (user_id, dept_id, job_title, specialty, introduction, registration_fee, schedule, create_time, update_time, is_deleted)
SELECT 
    u.id, 
    hd.dept_id, 
    '住院医师' as job_title,
    '白内障、青光眼、视网膜疾病' as specialty,
    '眼科硕士，专注眼部疾病治疗' as introduction,
    60.00 as registration_fee,
    '周三、五下午' as schedule,
    NOW(), NOW(), 0
FROM users u, hospital_department hd
WHERE u.username = 'doctor004' AND hd.dept_code = 'YK'
AND NOT EXISTS (SELECT 1 FROM doctor_info WHERE user_id = u.id);

INSERT INTO doctor_info (user_id, dept_id, job_title, specialty, introduction, registration_fee, schedule, create_time, update_time, is_deleted)
SELECT 
    u.id, 
    hd.dept_id, 
    '主治医师' as job_title,
    '皮肤病、湿疹、银屑病' as specialty,
    '皮肤科专家，擅长各类皮肤病治疗' as introduction,
    35.00 as registration_fee,
    '周二、四下午' as schedule,
    NOW(), NOW(), 0
FROM users u, hospital_department hd
WHERE u.username = 'doctor005' AND hd.dept_code = 'PFK'
AND NOT EXISTS (SELECT 1 FROM doctor_info WHERE user_id = u.id);

-- Insert sample patients
INSERT INTO users (username, password_hash, nickname, email, real_name, phone, id_card, gender, role_type, status, is_deleted, created_at, updated_at)
SELECT 'patient001', 'patient123', '张三', 'zhangsan@email.com', '张三', '13800138001', '110101199001011234', 1, 3, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'patient001');

INSERT INTO users (username, password_hash, nickname, email, real_name, phone, id_card, gender, role_type, status, is_deleted, created_at, updated_at)
SELECT 'patient002', 'patient123', '李四', 'lisi@email.com', '李四', '13800138002', '110101199002022345', 2, 3, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'patient002');

INSERT INTO users (username, password_hash, nickname, email, real_name, phone, id_card, gender, role_type, status, is_deleted, created_at, updated_at)
SELECT 'patient003', 'patient123', '王五', 'wangwu@email.com', '王五', '13800138003', '110101199003033456', 1, 3, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'patient003');

INSERT INTO users (username, password_hash, nickname, email, real_name, phone, id_card, gender, role_type, status, is_deleted, created_at, updated_at)
SELECT 'patient004', 'patient123', '赵六', 'zhaoliu@email.com', '赵六', '13800138004', '110101199004044567', 2, 3, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'patient004');

INSERT INTO users (username, password_hash, nickname, email, real_name, phone, id_card, gender, role_type, status, is_deleted, created_at, updated_at)
SELECT 'patient005', 'patient123', '钱七', 'qianqi@email.com', '钱七', '13800138005', '110101199005055678', 1, 3, 1, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'patient005');

-- Insert patient info
INSERT INTO patient_info (user_id, birth_date, age, blood_type, marital_status, address, emergency_contact, emergency_phone, allergy_history, past_medical_history, create_time, update_time, is_deleted)
SELECT u.id, '1990-01-01' as birth_date, TIMESTAMPDIFF(YEAR, '1990-01-01', CURDATE()) as age, 'A' as blood_type, 1 as marital_status, '北京市朝阳区建国路1号' as address, '张父' as emergency_contact, '13900139001' as emergency_phone, '青霉素过敏' as allergy_history, '高血压病史' as past_medical_history, NOW(), NOW(), 0
FROM users u WHERE u.username = 'patient001'
AND NOT EXISTS (SELECT 1 FROM patient_info WHERE user_id = u.id);

INSERT INTO patient_info (user_id, birth_date, age, blood_type, marital_status, address, emergency_contact, emergency_phone, allergy_history, past_medical_history, create_time, update_time, is_deleted)
SELECT u.id, '1992-02-02' as birth_date, TIMESTAMPDIFF(YEAR, '1992-02-02', CURDATE()) as age, 'B' as blood_type, 2 as marital_status, '北京市海淀区中关村大街2号' as address, '李母' as emergency_contact, '13900139002' as emergency_phone, '花粉过敏' as allergy_history, '糖尿病史' as past_medical_history, NOW(), NOW(), 0
FROM users u WHERE u.username = 'patient002'
AND NOT EXISTS (SELECT 1 FROM patient_info WHERE user_id = u.id);

INSERT INTO patient_info (user_id, birth_date, age, blood_type, marital_status, address, emergency_contact, emergency_phone, allergy_history, past_medical_history, create_time, update_time, is_deleted)
SELECT u.id, '1993-03-03' as birth_date, TIMESTAMPDIFF(YEAR, '1993-03-03', CURDATE()) as age, 'O' as blood_type, 1 as marital_status, '北京市东城区王府井大街3号' as address, '王妻' as emergency_contact, '13900139003' as emergency_phone, '无' as allergy_history, '无' as past_medical_history, NOW(), NOW(), 0
FROM users u WHERE u.username = 'patient003'
AND NOT EXISTS (SELECT 1 FROM patient_info WHERE user_id = u.id);

INSERT INTO patient_info (user_id, birth_date, age, blood_type, marital_status, address, emergency_contact, emergency_phone, allergy_history, past_medical_history, create_time, update_time, is_deleted)
SELECT u.id, '1994-04-04' as birth_date, TIMESTAMPDIFF(YEAR, '1994-04-04', CURDATE()) as age, 'AB' as blood_type, 2 as marital_status, '北京市西城区金融街4号' as address, '赵夫' as emergency_contact, '13900139004' as emergency_phone, '海鲜过敏' as allergy_history, '哮喘病史' as past_medical_history, NOW(), NOW(), 0
FROM users u WHERE u.username = 'patient004'
AND NOT EXISTS (SELECT 1 FROM patient_info WHERE user_id = u.id);

INSERT INTO patient_info (user_id, birth_date, age, blood_type, marital_status, address, emergency_contact, emergency_phone, allergy_history, past_medical_history, create_time, update_time, is_deleted)
SELECT u.id, '1995-05-05' as birth_date, TIMESTAMPDIFF(YEAR, '1995-05-05', CURDATE()) as age, 'A' as blood_type, 1 as marital_status, '北京市丰台区南三环西路5号' as address, '钱子' as emergency_contact, '13900139005' as emergency_phone, '无' as allergy_history, '无' as past_medical_history, NOW(), NOW(), 0
FROM users u WHERE u.username = 'patient005'
AND NOT EXISTS (SELECT 1 FROM patient_info WHERE user_id = u.id);

-- Insert sample registration records
INSERT INTO registration_record (registration_no, patient_id, doctor_id, dept_id, schedule_date, time_slot, registration_fee, pay_status, registration_status, visit_serial_number, remark, create_time, update_time, is_deleted)
SELECT * FROM (
SELECT 'R000001' as registration_no, 1 as patient_id, 1 as doctor_id, 1 as dept_id, CURDATE() as schedule_date, '上午' as time_slot, 50.00 as registration_fee, 1 as pay_status, 1 as registration_status, 15 as visit_serial_number, '复诊' as remark, NOW() as create_time, NOW() as update_time, 0 as is_deleted UNION ALL
SELECT 'R000002', 2, 2, 3, CURDATE(), '下午', 45.00, 1, 1, 8, '初诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000003', 3, 1, 1, CURDATE() + INTERVAL 1 DAY, '上午', 50.00, 0, 0, NULL, '初诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000004', 4, 2, 3, CURDATE() + INTERVAL 1 DAY, '下午', 45.00, 1, 0, NULL, '复诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000005', 1, 1, 1, CURDATE() + INTERVAL 2 DAY, '上午', 50.00, 0, 0, NULL, '初诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000006', 2, 2, 3, CURDATE() + INTERVAL 2 DAY, '下午', 45.00, 1, 0, NULL, '复诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000007', 3, 1, 1, CURDATE() + INTERVAL 3 DAY, '上午', 50.00, 0, 0, NULL, '初诊', NOW(), NOW(), 0 UNION ALL
SELECT 'R000008', 4, 2, 3, CURDATE() + INTERVAL 3 DAY, '下午', 45.00, 1, 0, NULL, '复诊', NOW(), NOW(), 0
) AS reg_data
WHERE NOT EXISTS (SELECT 1 FROM registration_record);

-- Insert sample medical records
INSERT INTO medical_record (patient_id, doctor_id, dept_id, registration_id, visit_date, chief_complaint, present_illness, past_history, physical_examination, auxiliary_examination, diagnosis, treatment_plan, record_status, create_time, update_time, is_deleted)
VALUES 
(1, 1, 1, 1, NOW(), '反复头痛头晕3年，加重1周', '患者3年前开始出现头痛头晕，近期加重，伴有恶心呕吐', '高血压病史3年', '血压160/100mmHg，心率85次/分', '头颅CT未见异常', '高血压病3级', '降压药物治疗，定期监测血压', 1, NOW(), NOW(), 0),
(2, 2, 3, 2, NOW() - INTERVAL 1 DAY, '月经不调半年', '患者半年来月经周期紊乱，经量增多', '无特殊病史', '妇科检查正常', 'B超检查子宫附件正常', '月经失调', '中药调理，注意休息', 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0),
(3, 3, 4, 3, NOW() - INTERVAL 2 DAY, '发热咳嗽3天', '患者3天前开始发热，体温最高38.5℃，伴咳嗽', '无特殊病史', '体温37.8℃，咽部充血，双肺呼吸音粗', '血常规：白细胞计数偏高', '急性上呼吸道感染', '抗感染治疗，多饮水', 1, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(4, 1, 1, 4, NOW() - INTERVAL 3 DAY, '胸闷气短1个月', '患者1个月来出现胸闷气短，活动后加重', '高血压病史5年', '血压150/95mmHg，双肺呼吸音清', '心电图：窦性心律，ST段改变', '冠心病', '扩冠药物治疗，低盐饮食', 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0);

-- Insert sample health monitoring data
INSERT INTO health_monitor (patient_id, monitor_date, systolic_pressure, diastolic_pressure, blood_glucose, heart_rate, body_temperature, weight, remark, create_time, update_time, is_deleted)
VALUES 
(1, NOW() - INTERVAL 7 DAY, 140, 90, 5.8, 75, 36.5, 70.5, '晨起测量', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 0),
(1, NOW() - INTERVAL 6 DAY, 135, 85, 5.6, 72, 36.4, 70.3, '晨起测量', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 0),
(1, NOW() - INTERVAL 5 DAY, 145, 92, 6.1, 78, 36.6, 70.7, '下午测量', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
(2, NOW() - INTERVAL 7 DAY, 120, 75, 7.2, 68, 36.3, 58.2, '空腹血糖', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 0),
(2, NOW() - INTERVAL 6 DAY, 118, 73, 6.8, 70, 36.4, 58.0, '空腹血糖', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 0),
(2, NOW() - INTERVAL 5 DAY, 122, 78, 7.5, 69, 36.5, 58.3, '餐后血糖', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
(3, NOW() - INTERVAL 3 DAY, 110, 70, 5.2, 85, 37.8, 25.6, '发热期间', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
(3, NOW() - INTERVAL 2 DAY, 115, 72, 5.4, 76, 36.8, 25.4, '体温正常', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(4, NOW() - INTERVAL 7 DAY, 150, 95, 5.9, 80, 36.5, 65.8, '晨起测量', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 0),
(4, NOW() - INTERVAL 6 DAY, 148, 92, 6.0, 78, 36.4, 65.6, '晨起测量', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY, 0);

-- Insert sample system news
INSERT INTO system_news (title, author, cover_image, content, view_count, is_top, status, publish_time, create_time, update_time, is_deleted)
VALUES 
('医院最新通知：春节期间就诊安排', '院办', NULL, '春节期间医院正常营业，急诊24小时开放，门诊时间调整为上午8:00-12:00，下午14:00-17:00。', 156, 1, 1, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY, 0),
('健康科普：如何预防春季流感', '王医生', NULL, '春季是流感高发季节，建议大家注意个人卫生，勤洗手，多通风，避免去人群密集场所。如出现发热、咳嗽等症状，请及时就医。', 89, 0, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, 0),
('医院引进先进医疗设备', '设备科', NULL, '我院最新引进的64排CT和3.0T磁共振设备已正式投入使用，将大大提高诊断准确性和效率。', 67, 0, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, 0),
('专家门诊时间调整通知', '门诊部', NULL, '李明主任医师门诊时间调整为周一、三、五上午，王芳副主任医师门诊时间调整为周二、四上午，请患者合理安排就诊时间。', 45, 0, 1, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0);

-- Medication Management Tables
-- Add record_id column if table already exists (for backward compatibility)
ALTER TABLE prescriptions ADD COLUMN record_id BIGINT AFTER prescription_id;
ALTER TABLE prescriptions ADD INDEX idx_prescriptions_record_id (record_id);

CREATE TABLE IF NOT EXISTS prescriptions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  prescription_id VARCHAR(64) NOT NULL,
  record_id BIGINT,
  patient_id BIGINT NOT NULL,
  doctor_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  treatment_plan TEXT,
  visit_date DATETIME,
  start_date DATETIME,
  end_date DATETIME,
  instructions TEXT,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  reminder_times TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_prescriptions_id (prescription_id),
  KEY idx_prescriptions_patient_id (patient_id),
  KEY idx_prescriptions_doctor_id (doctor_id),
  KEY idx_prescriptions_record_id (record_id),
  FOREIGN KEY (patient_id) REFERENCES users(id),
  FOREIGN KEY (doctor_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS prescription_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  prescription_id VARCHAR(64) NOT NULL,
  medication_name VARCHAR(255) NOT NULL,
  dosage VARCHAR(100) NOT NULL,
  frequency VARCHAR(100) NOT NULL,
  duration VARCHAR(100),
  note TEXT,
  quantity INT,
  unit VARCHAR(50),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_prescription_items_prescription_id (prescription_id)
);

CREATE TABLE IF NOT EXISTS medication_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  record_id VARCHAR(64) NOT NULL,
  prescription_id VARCHAR(64) NOT NULL,
  patient_id BIGINT NOT NULL,
  medication_name VARCHAR(255) NOT NULL,
  planned_time DATETIME,
  taken_at DATETIME NOT NULL,
  dosage VARCHAR(100),
  frequency VARCHAR(100),
  status VARCHAR(20) NOT NULL DEFAULT 'TAKEN',
  notes TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_medication_records_id (record_id),
  KEY idx_medication_records_patient_id (patient_id),
  KEY idx_medication_records_prescription_id (prescription_id),
  KEY idx_medication_records_taken_at (taken_at),
  FOREIGN KEY (patient_id) REFERENCES users(id)
);

-- Insert sample medication data
INSERT IGNORE INTO prescriptions (prescription_id, patient_id, doctor_id, title, treatment_plan, visit_date, start_date, end_date, instructions, status, reminder_times, created_at, updated_at)
VALUES 
('PRE001', 3, 2, '上呼吸道感染治疗', '抗生素治疗 + 对症治疗', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, NOW() + INTERVAL 7 DAY, '饭后服用，完成疗程', 'ACTIVE', '["08:00", "14:00", "20:00"]', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
('PRE002', 4, 1, '慢性胃炎治疗', '胃黏膜保护 + 抑酸治疗', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY, NOW() + INTERVAL 21 DAY, '餐前30分钟服用', 'ACTIVE', '["07:30", "19:30"]', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY);

INSERT IGNORE INTO prescription_items (prescription_id, medication_name, dosage, frequency, duration, note, quantity, unit, created_at, updated_at)
VALUES 
('PRE001', '阿莫西林胶囊', '0.5g', '每日3次', '7天', '饭后服用', 21, '粒', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
('PRE001', '溴己新片', '8mg', '每日3次', '7天', '饭后服用', 21, '片', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
('PRE002', '奥美拉唑肠溶胶囊', '20mg', '每日2次', '21天', '餐前30分钟服用', 42, '粒', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY),
('PRE002', '铝碳酸镁片', '2片', '每日3次', '21天', '饭后咀嚼吞服', 63, '片', NOW() - INTERVAL 15 DAY, NOW() - INTERVAL 15 DAY);

INSERT IGNORE INTO medication_records (record_id, prescription_id, patient_id, medication_name, planned_time, taken_at, dosage, frequency, status, notes, created_at, updated_at)
VALUES 
('REC001', 'PRE001', 3, '阿莫西林胶囊', '2024-01-15 08:00:00', '2024-01-15 08:15:00', '0.5g', '每日3次', 'TAKEN', '按时服用', '2024-01-15 08:15:00', '2024-01-15 08:15:00'),
('REC002', 'PRE001', 3, '溴己新片', '2024-01-15 08:00:00', '2024-01-15 08:10:00', '8mg', '每日3次', 'TAKEN', '按时服用', '2024-01-15 08:10:00', '2024-01-15 08:10:00'),
('REC003', 'PRE001', 3, '阿莫西林胶囊', '2024-01-15 14:00:00', '2024-01-15 14:30:00', '0.5g', '每日3次', 'TAKEN', '延迟30分钟服用', '2024-01-15 14:30:00', '2024-01-15 14:30:00'),
('REC004', 'PRE002', 4, '奥美拉唑肠溶胶囊', '2024-01-14 07:30:00', '2024-01-14 07:25:00', '20mg', '每日2次', 'TAKEN', '提前5分钟服用', '2024-01-14 07:25:00', '2024-01-14 07:25:00'),
('REC005', 'PRE002', 4, '铝碳酸镁片', '2024-01-14 08:00:00', NULL, '2片', '每日3次', 'MISSED', '忘记服用', '2024-01-14 20:00:00', '2024-01-14 20:00:00');

CREATE TABLE IF NOT EXISTS doctor_daily_appointment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  doctor_id BIGINT NOT NULL,
  appointment_date DATE NOT NULL,
  daily_limit INT NOT NULL DEFAULT 20,
  booked_count INT NOT NULL DEFAULT 0,
  remaining_count INT NOT NULL DEFAULT 20,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_doctor_date (doctor_id, appointment_date),
  KEY idx_daily_appointment_doctor_id (doctor_id),
  KEY idx_daily_appointment_date (appointment_date)
);
