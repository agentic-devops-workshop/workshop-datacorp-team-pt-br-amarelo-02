-- V1__init_schema.sql
-- Implements database schema for SIFAP 2.0 modular monolith
-- Maps Adabas DDMs to PostgreSQL per ADR-002

CREATE TABLE beneficiaries (
    id BIGSERIAL PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR(200) NOT NULL,
    dt_nascimento DATE,
    uf VARCHAR(2),
    cod_regiao INTEGER,
    renda_familiar NUMERIC(15,2),
    num_dependentes INTEGER DEFAULT 0,
    cod_programa INTEGER,
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_beneficiary_status CHECK (status IN ('ACTIVE','SUSPENDED','CANCELLED','INACTIVE','DELETED'))
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    cpf VARCHAR(11) NOT NULL,
    competencia VARCHAR(6) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    tipo_pgto VARCHAR(1) NOT NULL DEFAULT 'N',
    vlr_bruto NUMERIC(15,2) NOT NULL,
    vlr_desconto NUMERIC(15,2) DEFAULT 0,
    vlr_liquido NUMERIC(15,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_payment_cpf_competencia UNIQUE (cpf, competencia),
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED','DELIVERED')),
    CONSTRAINT chk_payment_type CHECK (tipo_pgto IN ('N','D','T'))
);

CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    before_state JSONB,
    after_state JSONB,
    user_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- REQ-PAY-004: index for CPF ordering in batch
CREATE INDEX idx_beneficiaries_status_cpf ON beneficiaries (status, cpf);
CREATE INDEX idx_payments_competencia ON payments (competencia);
CREATE INDEX idx_audit_entity ON audit_events (entity_type, entity_id);
