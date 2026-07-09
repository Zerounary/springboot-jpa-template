CREATE TABLE ak_users (
  id NUMBER(19) GENERATED AS IDENTITY,
  username VARCHAR2(64) NOT NULL,
  password_hash VARCHAR2(100) NOT NULL,
  nickname VARCHAR2(64),
  email VARCHAR2(128),
  created_at TIMESTAMP default sysdate  NOT NULL,
  updated_at TIMESTAMP default sysdate  NOT NULL ,
  CONSTRAINT pk_ak_users PRIMARY KEY (id),
  CONSTRAINT uk_ak_users_username UNIQUE (username)
);
