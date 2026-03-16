CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  nickname VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
);

CREATE TABLE IF NOT EXISTS patients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id VARCHAR(64) NOT NULL,
  gender TINYINT NOT NULL,
  age INT NOT NULL,
  birth_date DATE NOT NULL,
  phone VARCHAR(32) NOT NULL,
  medical_institution VARCHAR(128) NOT NULL,
  nation VARCHAR(32) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_patients_user_id (user_id),
  KEY idx_patients_medical_institution (medical_institution)
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
  prediction_time DATETIME NOT NULL,
  prediction_prob DECIMAL(6,5) NOT NULL,
  prediction_label TINYINT NOT NULL,
  core_risk_factors TEXT NOT NULL,
  warning_status TINYINT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_prediction_results_patient_id_time (patient_id, prediction_time),
  CONSTRAINT fk_prediction_results_patient_id FOREIGN KEY (patient_id) REFERENCES patients (id)
);
