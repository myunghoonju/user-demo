-- =============================================================================
-- 회원 서비스 DDL
-- DB: MySQL 8.x
-- 실행: mysql -u root -p user_demo < scripts/schema.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS user_demo
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE user_demo;

-- -----------------------------------------------------------------------------
-- members 테이블
-- name, phone_number: AES-256/GCM 암호화 저장 (PiiAttributeConverter)
-- email: 유니크 인덱스 (검색 가능, 평문 저장)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS members
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    email        VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(500) NOT NULL COMMENT 'AES-256 암호화',
    phone_number VARCHAR(500) NOT NULL COMMENT 'AES-256 암호화',
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_members_email (email),
    UNIQUE KEY uq_members_phone_number (phone_number),
    INDEX idx_members_status (status),
    INDEX idx_members_role (role)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- withdrawn_members 테이블
-- 탈퇴 회원 분리 보관 (개인정보보호법 준수)
-- name, phone_number: AES-256/GCM 암호화 유지
-- scheduled_deletion_at 만료 시 스케줄러가 물리 삭제
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS withdrawn_members
(
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    original_member_id    BIGINT       NOT NULL COMMENT '원래 members.id',
    email                 VARCHAR(255) NOT NULL,
    name                  VARCHAR(500) NOT NULL COMMENT 'AES-256 암호화',
    phone_number          VARCHAR(500) NOT NULL COMMENT 'AES-256 암호화',
    withdrawn_at          DATETIME(6)  NOT NULL,
    scheduled_deletion_at DATETIME(6)  NOT NULL COMMENT '보존 기간 만료일 (기본 1년)',
    PRIMARY KEY (id),
    INDEX idx_withdrawn_scheduled_deletion_at (scheduled_deletion_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
