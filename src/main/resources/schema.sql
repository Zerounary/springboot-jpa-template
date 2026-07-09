CREATE TABLE users (
  id NUMBER(19) GENERATED AS IDENTITY,
  username VARCHAR2(64) NOT NULL,
  password_hash VARCHAR2(100) NOT NULL,
  nickname VARCHAR2(64),
  email VARCHAR2(128),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT pk_users PRIMARY KEY (id),
  CONSTRAINT uk_users_username UNIQUE (username)
);
