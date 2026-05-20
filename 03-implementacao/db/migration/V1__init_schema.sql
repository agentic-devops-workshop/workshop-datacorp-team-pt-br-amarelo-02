-- ============================================================================
-- V1__init_schema.sql
-- ============================================================================
-- Schema inicial do SIFAP 2.0 (módulos: reference, beneficiary, social_program,
-- payment, audit). Modular monolith — um schema por módulo.
--
-- Origem das tabelas:
--   - BENEFICIARIO.ddm     → beneficiary.beneficiary (+ beneficiary.dependent)
--   - PROGRAMA-SOCIAL.ddm  → social_program.social_program
--   - PAGAMENTO.ddm        → payment.payment_cycle / payment / payment_deduction
--   - AUDITORIA.ddm        → audit.audit_event
--   - Tabelas hardcoded em BATCHPGT.NSN/CALCBENF.NSN → schema reference
--
-- Convenção: tabelas de domínio em snake_case, colunas idem, FKs explícitas.
-- ============================================================================

CREATE SCHEMA IF NOT EXISTS reference;
CREATE SCHEMA IF NOT EXISTS beneficiary;
CREATE SCHEMA IF NOT EXISTS social_program;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS audit;

-- ----------------------------------------------------------------------------
-- reference.regional_factor — 27 UFs com fator regional (BR-010, BR-011)
-- Origem: tabela #TAB-REG hardcoded em BATCHPGT.NSN#L138-L165 e CALCBENF.NSN
-- ----------------------------------------------------------------------------
CREATE TABLE reference.regional_factor (
    region_code     SMALLINT     PRIMARY KEY,                       -- 1..27 (COD-REGIAO do legado)
    uf              CHAR(2)      NOT NULL UNIQUE,                   -- AC, AL, ..., TO
    uf_name         VARCHAR(40)  NOT NULL,
    factor          NUMERIC(4,2) NOT NULL CHECK (factor >= 0),      -- 1.00 .. 1.40
    legacy_source   VARCHAR(120) NOT NULL DEFAULT 'BATCHPGT.NSN#L138-L165'
);

COMMENT ON TABLE reference.regional_factor IS
  'Fator regional por UF. Substitui tabela #TAB-REG hardcoded no Natural. '
  'BR-010 (validação UF) + BR-011 (fator regional).';

-- ----------------------------------------------------------------------------
-- reference.income_bracket — 5 faixas de renda familiar (BR-012)
-- Origem: BATCHPGT.NSN#L167-L177
-- ----------------------------------------------------------------------------
CREATE TABLE reference.income_bracket (
    id              SMALLINT     PRIMARY KEY,
    min_income      NUMERIC(9,2) NOT NULL,
    max_income      NUMERIC(9,2),                                   -- NULL = sem teto
    factor          NUMERIC(4,2) NOT NULL CHECK (factor BETWEEN 0 AND 1),
    legacy_source   VARCHAR(120) NOT NULL DEFAULT 'BATCHPGT.NSN#L167-L177'
);

-- ----------------------------------------------------------------------------
-- reference.contribution_bracket — faixas de contribuição social (BR-004)
-- Origem: CALCDSCT.NSN#L45-L60
-- ----------------------------------------------------------------------------
CREATE TABLE reference.contribution_bracket (
    id              SMALLINT     PRIMARY KEY,
    min_amount      NUMERIC(11,2) NOT NULL,
    max_amount      NUMERIC(11,2),                                   -- NULL = sem teto
    rate            NUMERIC(4,3)  NOT NULL CHECK (rate BETWEEN 0 AND 1),
    legacy_source   VARCHAR(120)  NOT NULL DEFAULT 'CALCDSCT.NSN#L45-L60'
);

-- ----------------------------------------------------------------------------
-- social_program.social_program — origem: PROGRAMA-SOCIAL.ddm
-- ----------------------------------------------------------------------------
CREATE TABLE social_program.social_program (
    id              BIGSERIAL    PRIMARY KEY,
    code            VARCHAR(10)  NOT NULL UNIQUE,                   -- COD-PROGRAMA
    name            VARCHAR(80)  NOT NULL,
    base_amount     NUMERIC(11,2) NOT NULL CHECK (base_amount >= 0), -- VLR-BASE
    adjustment_factor NUMERIC(5,3) NOT NULL DEFAULT 1.000,           -- FATOR-REAJUSTE
    max_income      NUMERIC(9,2)  NOT NULL,                          -- RENDA-MAX
    status          CHAR(1)       NOT NULL DEFAULT 'A',              -- STATUS-PROG: A=ACTIVE, I=INACTIVE
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT social_program_status_ck CHECK (status IN ('A', 'I'))
);

-- ----------------------------------------------------------------------------
-- beneficiary.beneficiary — origem: BENEFICIARIO.ddm
-- ----------------------------------------------------------------------------
CREATE TABLE beneficiary.beneficiary (
    id              BIGSERIAL    PRIMARY KEY,
    cpf             CHAR(11)     NOT NULL UNIQUE,                    -- BR-007: módulo 11
    name            VARCHAR(100) NOT NULL,                           -- BR-009: nome + sobrenome
    birth_date      DATE         NOT NULL,                           -- BR-008
    region_code     SMALLINT     NOT NULL REFERENCES reference.regional_factor(region_code),
    family_income   NUMERIC(9,2) NOT NULL CHECK (family_income >= 0),
    status          CHAR(1)      NOT NULL DEFAULT 'A',               -- A/S/C/I/D
    program_id      BIGINT       REFERENCES social_program.social_program(id),
    nis             CHAR(11),
    num_dependents  SMALLINT     NOT NULL DEFAULT 0 CHECK (num_dependents >= 0),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT beneficiary_status_ck CHECK (status IN ('A', 'S', 'C', 'I', 'D'))
);

CREATE INDEX idx_beneficiary_status   ON beneficiary.beneficiary(status);
CREATE INDEX idx_beneficiary_program  ON beneficiary.beneficiary(program_id);

-- Dependentes (PE DEPENDENTES de BENEFICIARIO.ddm → tabela filha)
CREATE TABLE beneficiary.dependent (
    id              BIGSERIAL    PRIMARY KEY,
    beneficiary_id  BIGINT       NOT NULL REFERENCES beneficiary.beneficiary(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    birth_date      DATE         NOT NULL,
    relationship    VARCHAR(20)  NOT NULL                            -- ex.: SON, DAUGHTER, SPOUSE
);

CREATE INDEX idx_dependent_beneficiary ON beneficiary.dependent(beneficiary_id);

-- ----------------------------------------------------------------------------
-- payment.payment_cycle / payment / payment_deduction — alinhado ao V1 do
-- exemplo em 08-exemplos/V1__init_payment_module-exemplo.sql.
-- ----------------------------------------------------------------------------
CREATE TABLE payment.payment_cycle (
    id              BIGSERIAL    PRIMARY KEY,
    competence      CHAR(6)      NOT NULL UNIQUE,                    -- YYYYMM
    cutoff_date     DATE         NOT NULL,
    generated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    generated_by    VARCHAR(60)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN'
                    CHECK (status IN ('OPEN', 'CLOSED', 'CANCELLED'))
);

CREATE TABLE payment.payment (
    id              BIGSERIAL    PRIMARY KEY,
    cycle_id        BIGINT       NOT NULL REFERENCES payment.payment_cycle(id),
    beneficiary_id  BIGINT       NOT NULL REFERENCES beneficiary.beneficiary(id),
    beneficiary_cpf CHAR(11)     NOT NULL,
    payment_type    CHAR(1)      NOT NULL DEFAULT 'N'                -- N/D/T (BR-015)
                    CHECK (payment_type IN ('N', 'D', 'T')),
    gross_amount    NUMERIC(11,2) NOT NULL CHECK (gross_amount >= 0),
    deduction_total NUMERIC(11,2) NOT NULL DEFAULT 0 CHECK (deduction_total >= 0),
    net_amount      NUMERIC(11,2) GENERATED ALWAYS AS (gross_amount - deduction_total) STORED,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'DELIVERED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT payment_cycle_benef_uk UNIQUE (cycle_id, beneficiary_cpf)
);

CREATE INDEX idx_payment_status      ON payment.payment(status);
CREATE INDEX idx_payment_beneficiary ON payment.payment(beneficiary_id);
CREATE INDEX idx_payment_cycle       ON payment.payment(cycle_id);

CREATE TABLE payment.payment_deduction (
    id              BIGSERIAL    PRIMARY KEY,
    payment_id      BIGINT       NOT NULL REFERENCES payment.payment(id) ON DELETE CASCADE,
    deduction_type  VARCHAR(20)  NOT NULL
                    CHECK (deduction_type IN ('CONTRIB', 'TAX', 'JUDICIAL', 'UNION', 'PENSION', 'ADMIN')),
    amount          NUMERIC(11,2) NOT NULL CHECK (amount >= 0),
    percentage      NUMERIC(5,2),
    process_number  VARCHAR(20),
    CONSTRAINT payment_deduction_judicial_process_ck
        CHECK ((deduction_type <> 'JUDICIAL') OR (process_number IS NOT NULL))
);

-- ----------------------------------------------------------------------------
-- audit.audit_event — origem: AUDITORIA.ddm. Imutável.
-- ----------------------------------------------------------------------------
CREATE TABLE audit.audit_event (
    id              BIGSERIAL    PRIMARY KEY,
    aggregate_type  VARCHAR(40)  NOT NULL,                           -- BENEFICIARY/PAYMENT/PROGRAM
    aggregate_id    BIGINT       NOT NULL,
    event_type      VARCHAR(40)  NOT NULL,                           -- CREATED/UPDATED/STATUS_CHANGED
    actor           VARCHAR(60)  NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    payload         JSONB
);

CREATE INDEX idx_audit_aggregate ON audit.audit_event(aggregate_type, aggregate_id);
CREATE INDEX idx_audit_when      ON audit.audit_event(occurred_at);

REVOKE UPDATE, DELETE ON audit.audit_event FROM PUBLIC;
