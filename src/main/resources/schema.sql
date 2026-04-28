CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'PATIENT',
  nickname VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
);

CREATE TABLE IF NOT EXISTS organizations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  org_code VARCHAR(64) NULL,
  org_name VARCHAR(128) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_organizations_org_name (org_name)
);

CREATE TABLE IF NOT EXISTS patients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  account_id BIGINT NULL,
  organization_id BIGINT NULL,
  patient_name VARCHAR(64) NULL,
  gender TINYINT NOT NULL,
  age INT NOT NULL,
  birth_date DATE NOT NULL,
  phone VARCHAR(32) NOT NULL,
  medical_institution VARCHAR(128) NOT NULL,
  nation VARCHAR(32) NULL,
  id_card VARCHAR(18) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_patients_user_id (user_id),
  KEY idx_patients_organization_id (organization_id),
  KEY idx_patients_medical_institution (medical_institution),
  KEY idx_patients_account_id (account_id),
  KEY idx_patients_id_card (id_card),
  KEY idx_patients_patient_name (patient_name),
  CONSTRAINT fk_patients_organization_id FOREIGN KEY (organization_id) REFERENCES organizations (id),
  CONSTRAINT fk_patients_account_id FOREIGN KEY (account_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS health_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  family_hypertension TINYINT NOT NULL,
  past_hypertension TINYINT NOT NULL,
  comorbidity VARCHAR(255) NOT NULL,
  drug_history TEXT NOT NULL,
  treatment_record TEXT NULL,
  allergy_history TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_health_records_patient_id (patient_id),
  CONSTRAINT fk_health_records_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS physical_exams (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  exam_time DATETIME NOT NULL,
  systolic_bp INT NOT NULL,
  diastolic_bp INT NOT NULL,
  bmi DECIMAL(5,2) NOT NULL,
  cholesterol DECIMAL(6,2) NOT NULL,
  fasting_blood_sugar DECIMAL(6,2) NOT NULL,
  height DECIMAL(5,2) NOT NULL,
  weight DECIMAL(5,2) NOT NULL,
  heart_rate INT NULL,
  liver_function DECIMAL(6,2) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_physical_exams_patient_id_exam_time (patient_id, exam_time),
  CONSTRAINT fk_physical_exams_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS questionnaires (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  questionnaire_time DATETIME NOT NULL,
  smoking TINYINT NOT NULL,
  drinking TINYINT NOT NULL,
  diet_preference TINYINT NOT NULL,
  exercise_frequency TINYINT NOT NULL,
  work_rest TINYINT NULL,
  stress_level TINYINT NULL,
  habit_remark TEXT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_questionnaires_patient_id_time (patient_id, questionnaire_time),
  CONSTRAINT fk_questionnaires_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS prediction_results (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  model_id BIGINT NULL,
  prediction_time DATETIME NOT NULL,
  prediction_prob DECIMAL(6,5) NOT NULL,
  prediction_label TINYINT NOT NULL,
  core_risk_factors TEXT NOT NULL,
  warning_status TINYINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_prediction_results_model_id (model_id),
  KEY idx_prediction_results_patient_id_time (patient_id, prediction_time),
  CONSTRAINT fk_prediction_results_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS health_guidances (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_user_id BIGINT NOT NULL,
  prediction_result_id BIGINT NULL,
  guidance_title VARCHAR(128) NOT NULL,
  guidance_content TEXT NOT NULL,
  guidance_level TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_health_guidances_patient_id (patient_id),
  KEY idx_health_guidances_doctor_user_id (doctor_user_id),
  KEY idx_health_guidances_prediction_result_id (prediction_result_id),
  CONSTRAINT fk_health_guidances_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id),
  CONSTRAINT fk_health_guidances_doctor_user_id FOREIGN KEY (doctor_user_id) REFERENCES users (id),
  CONSTRAINT fk_health_guidances_prediction_result_id FOREIGN KEY (prediction_result_id) REFERENCES prediction_results (id)
);

CREATE TABLE IF NOT EXISTS hypertension_fusion (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  age INT NOT NULL,
  gender TINYINT NOT NULL,
  systolic_bp INT NOT NULL,
  diastolic_bp INT NOT NULL,
  bmi DECIMAL(5,2) NOT NULL,
  cholesterol DECIMAL(6,2) NOT NULL,
  family_hypertension TINYINT NOT NULL,
  smoking TINYINT NOT NULL,
  diet_preference TINYINT NOT NULL,
  hypertension_label TINYINT NOT NULL,
  last_exam_time DATETIME NULL,
  last_questionnaire_time DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_hypertension_fusion_patient_id (patient_id),
  KEY idx_hypertension_fusion_user_id (user_id),
  CONSTRAINT fk_hypertension_fusion_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);

CREATE TABLE IF NOT EXISTS ml_models (
  id BIGINT NOT NULL AUTO_INCREMENT,
  model_name VARCHAR(128) NOT NULL,
  version_tag VARCHAR(64) NOT NULL,
  algorithm VARCHAR(64) NOT NULL,
  feature_columns TEXT NOT NULL,
  model_path VARCHAR(255) NOT NULL,
  param_json TEXT NULL,
  metric_json TEXT NULL,
  feature_importance_json TEXT NULL,
  total_count BIGINT NULL,
  train_count BIGINT NULL,
  test_count BIGINT NULL,
  auc DECIMAL(10,6) NULL,
  accuracy DECIMAL(10,6) NULL,
  trained_at DATETIME NOT NULL,
  is_active TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ml_models_version_tag (version_tag),
  KEY idx_ml_models_trained_at (trained_at),
  KEY idx_ml_models_is_active (is_active)
);

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'users'
        AND COLUMN_NAME = 'role'
    ),
    'SELECT 1',
    'ALTER TABLE users ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT ''PATIENT''' 
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'prediction_results'
        AND COLUMN_NAME = 'model_id'
    ),
    'SELECT 1',
    'ALTER TABLE prediction_results ADD COLUMN model_id BIGINT NULL'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'prediction_results'
        AND INDEX_NAME = 'idx_prediction_results_model_id'
    ),
    'SELECT 1',
    'ALTER TABLE prediction_results ADD INDEX idx_prediction_results_model_id (model_id)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.TABLE_CONSTRAINTS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'prediction_results'
        AND CONSTRAINT_NAME = 'fk_prediction_results_model_id'
        AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ),
    'SELECT 1',
    'ALTER TABLE prediction_results ADD CONSTRAINT fk_prediction_results_model_id FOREIGN KEY (model_id) REFERENCES ml_models (id)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND COLUMN_NAME = 'account_id'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD COLUMN account_id BIGINT NULL'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND COLUMN_NAME = 'organization_id'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD COLUMN organization_id BIGINT NULL'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND COLUMN_NAME = 'id_card'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD COLUMN id_card VARCHAR(18) NULL'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND INDEX_NAME = 'idx_patients_id_card'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD INDEX idx_patients_id_card (id_card)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND COLUMN_NAME = 'patient_name'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD COLUMN patient_name VARCHAR(64) NULL'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'patients'
        AND INDEX_NAME = 'idx_patients_patient_name'
    ),
    'SELECT 1',
    'ALTER TABLE patients ADD INDEX idx_patients_patient_name (patient_name)'
  )
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO organizations (id, org_code, org_name, created_at, updated_at)
VALUES
  (1, 'ORG_A', '机构A', NOW(), NOW()),
  (2, 'ORG_B', '机构B', NOW(), NOW()),
  (3, 'ORG_C', '机构C', NOW(), NOW())
ON DUPLICATE KEY UPDATE org_name = org_name;

INSERT INTO users (id, username, password_hash, role, nickname, email, created_at, updated_at)
VALUES
  (9999, 'admin', 'admin123', 'ADMIN', '管理员', NULL, NOW(), NOW()),
  (9998, 'doctor', 'doctor123', 'DOCTOR', '医生账号', NULL, NOW(), NOW()),
  (10001, 'patient1001', 'patient1001', 'PATIENT', '患者1001', NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE username = username;

UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
UPDATE users SET role = 'DOCTOR' WHERE username = 'doctor';
UPDATE users SET role = 'PATIENT' WHERE username = 'patient1001';

INSERT INTO patients (id, user_id, account_id, organization_id, patient_name, gender, age, birth_date, phone, medical_institution, nation, id_card, created_at, updated_at)
VALUES
  (1001, 'U1001', 10001, 1, '张三', 1, 55, '1969-01-01', '13800001001', '机构A', '汉', '110101196901011234', NOW(), NOW()),
  (1002, 'U1002', NULL, 1, '李四', 0, 42, '1982-01-01', '13800001002', '机构A', '汉', '110101198201012345', NOW(), NOW()),
  (1003, 'U1003', NULL, 2, '王五', 1, 68, '1956-01-01', '13800001003', '机构B', '汉', '110101195601013456', NOW(), NOW()),
  (1004, 'U1004', NULL, 2, '赵六', 0, 35, '1989-01-01', '13800001004', '机构B', '汉', '110101198901014567', NOW(), NOW()),
  (1005, 'U1005', NULL, 3, '钱七', 1, 72, '1952-01-01', '13800001005', '机构C', '汉', '110101195201015678', NOW(), NOW())
ON DUPLICATE KEY UPDATE user_id = user_id;

UPDATE patients SET account_id = 10001 WHERE id = 1001 AND account_id IS NULL;

INSERT INTO health_records (id, patient_id, family_hypertension, past_hypertension, comorbidity, drug_history, treatment_record, allergy_history, created_at, updated_at)
VALUES
  (2001, 1001, 1, 1, '1', '降压药', NULL, NULL, NOW(), NOW()),
  (2002, 1002, 0, 0, '0', '无', NULL, NULL, NOW(), NOW()),
  (2003, 1003, 1, 1, '2', '降压药', NULL, NULL, NOW(), NOW()),
  (2004, 1004, 1, 0, '0', '无', NULL, NULL, NOW(), NOW()),
  (2005, 1005, 0, 1, '2', '降压药', NULL, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_id = patient_id;

INSERT INTO physical_exams (id, patient_id, exam_time, systolic_bp, diastolic_bp, bmi, cholesterol, fasting_blood_sugar, height, weight, heart_rate, liver_function, created_at, updated_at)
VALUES
  (3001, 1001, '2024-01-01 10:00:00', 145, 95, 28.50, 6.20, 5.60, 170.00, 82.00, 75, NULL, NOW(), NOW()),
  (3002, 1002, '2024-01-01 10:00:00', 120, 80, 22.10, 4.80, 5.10, 160.00, 56.00, 72, NULL, NOW(), NOW()),
  (3003, 1003, '2024-01-01 10:00:00', 155, 100, 30.20, 7.50, 6.20, 168.00, 85.00, 78, NULL, NOW(), NOW()),
  (3004, 1004, '2024-01-01 10:00:00', 130, 85, 24.80, 5.50, 5.30, 158.00, 62.00, 70, NULL, NOW(), NOW()),
  (3005, 1005, '2024-01-01 10:00:00', 160, 105, 29.00, 7.00, 6.00, 172.00, 86.00, 80, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_id = patient_id;

INSERT INTO questionnaires (id, patient_id, questionnaire_time, smoking, drinking, diet_preference, exercise_frequency, work_rest, stress_level, habit_remark, created_at, updated_at)
VALUES
  (4001, 1001, '2024-01-01 11:00:00', 2, 2, 2, 1, 1, 2, NULL, NOW(), NOW()),
  (4002, 1002, '2024-01-01 11:00:00', 0, 0, 0, 2, 0, 1, NULL, NOW(), NOW()),
  (4003, 1003, '2024-01-01 11:00:00', 2, 1, 2, 0, 2, 3, NULL, NOW(), NOW()),
  (4004, 1004, '2024-01-01 11:00:00', 0, 0, 1, 2, 0, 1, NULL, NOW(), NOW()),
  (4005, 1005, '2024-01-01 11:00:00', 2, 2, 2, 0, 2, 3, NULL, NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_id = patient_id;

INSERT INTO health_guidances (id, patient_id, doctor_user_id, prediction_result_id, guidance_title, guidance_content, guidance_level, created_at, updated_at)
VALUES
  (6001, 1001, 9998, NULL, '控制盐分摄入', '建议每日低盐饮食，保持规律运动并监测晨起血压。', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_id = patient_id;

INSERT INTO hypertension_fusion (id, patient_id, user_id, age, gender, systolic_bp, diastolic_bp, bmi, cholesterol, family_hypertension, smoking, diet_preference, hypertension_label, last_exam_time, last_questionnaire_time, created_at, updated_at)
VALUES
  (5001, 1001, 'U1001', 55, 1, 145, 95, 28.50, 6.20, 1, 2, 2, 1, '2024-01-01 10:00:00', '2024-01-01 11:00:00', NOW(), NOW()),
  (5002, 1002, 'U1002', 42, 0, 120, 80, 22.10, 4.80, 0, 0, 0, 0, '2024-01-01 10:00:00', '2024-01-01 11:00:00', NOW(), NOW()),
  (5003, 1003, 'U1003', 68, 1, 155, 100, 30.20, 7.50, 1, 2, 2, 1, '2024-01-01 10:00:00', '2024-01-01 11:00:00', NOW(), NOW()),
  (5004, 1004, 'U1004', 35, 0, 130, 85, 24.80, 5.50, 1, 0, 1, 0, '2024-01-01 10:00:00', '2024-01-01 11:00:00', NOW(), NOW()),
  (5005, 1005, 'U1005', 72, 1, 160, 105, 29.00, 7.00, 0, 2, 2, 1, '2024-01-01 10:00:00', '2024-01-01 11:00:00', NOW(), NOW())
ON DUPLICATE KEY UPDATE patient_id = patient_id;
