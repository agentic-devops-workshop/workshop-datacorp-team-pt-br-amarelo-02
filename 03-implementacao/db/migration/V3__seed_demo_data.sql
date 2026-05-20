-- ============================================================================
-- V3__seed_demo_data.sql
-- ============================================================================
-- Dados de demonstração para o ambiente local (docker compose).
-- NÃO aplicar em produção. Use perfil Spring `local` ou `demo`.
--
-- Conteúdo:
--   - 10 beneficiários (8 ACTIVE, 1 SUSPENDED, 1 CANCELLED) cobrindo regiões variadas
--   - 4 dependentes
--   - 1 ciclo de pagamento (competência 202605)
--   - Pagamentos para os 8 beneficiários ACTIVE
--   - 2 descontos (1 contribuição, 1 judicial)
--
-- Os CPFs abaixo são válidos pelo módulo 11 e foram gerados aleatoriamente
-- apenas para fins de teste — não correspondem a pessoas reais.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Beneficiários
-- ----------------------------------------------------------------------------
INSERT INTO beneficiary.beneficiary
    (cpf, name, birth_date, region_code, family_income, status, program_id, nis, num_dependents) VALUES
    ('39053344705', 'MARIA SILVA SANTOS',          '1965-03-12',  5, 450.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'BPC'),  '12345678901', 2),
    ('22233344455', 'JOAO PEREIRA SOUZA',          '1958-07-22', 14, 280.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'BPC'),  '12345678902', 0),
    ('11144477735', 'ANA COSTA OLIVEIRA',          '1972-11-05',  6, 650.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'FAM'),  '12345678903', 3),
    ('52998224725', 'CARLOS ALMEIDA RIBEIRO',      '1980-01-30', 17, 920.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'FAM'),  '12345678904', 1),
    ('70140293078', 'BEATRIZ FERNANDES LIMA',      '1990-09-14', 25, 1200.00,'A',
        (SELECT id FROM social_program.social_program WHERE code = 'FAM'),  '12345678905', 2),
    ('15350946056', 'PEDRO HENRIQUE MARTINS',      '1955-05-18', 19, 380.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'BPC'),  '12345678906', 0),
    ('30883347067', 'LUCIA MENDES BARBOSA',        '1968-12-03', 10, 540.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'SECA'), '12345678907', 4),
    ('99988877766', 'JOSE FERREIRA DA SILVA',      '1962-02-28',  1, 260.00, 'A',
        (SELECT id FROM social_program.social_program WHERE code = 'SECA'), '12345678908', 1),
    ('86446485053', 'PAULO ROBERTO CAVALCANTI',    '1970-06-10',  2, 720.00, 'S',
        (SELECT id FROM social_program.social_program WHERE code = 'FAM'),  '12345678909', 0),
    ('45317828791', 'TERESA RODRIGUES NUNES',      '1949-08-25', 21, 1800.00,'C',
        NULL, '12345678910', 0);

-- ----------------------------------------------------------------------------
-- Dependentes
-- ----------------------------------------------------------------------------
INSERT INTO beneficiary.dependent (beneficiary_id, name, birth_date, relationship) VALUES
    ((SELECT id FROM beneficiary.beneficiary WHERE cpf = '39053344705'), 'LUCAS SILVA SANTOS',     '2010-04-15', 'SON'),
    ((SELECT id FROM beneficiary.beneficiary WHERE cpf = '39053344705'), 'JULIA SILVA SANTOS',     '2012-08-20', 'DAUGHTER'),
    ((SELECT id FROM beneficiary.beneficiary WHERE cpf = '11144477735'), 'MARCOS COSTA OLIVEIRA',  '2008-01-10', 'SON'),
    ((SELECT id FROM beneficiary.beneficiary WHERE cpf = '30883347067'), 'CLARA MENDES BARBOSA',   '2015-11-30', 'DAUGHTER');

-- ----------------------------------------------------------------------------
-- Ciclo de pagamento — competência maio/2026
-- ----------------------------------------------------------------------------
INSERT INTO payment.payment_cycle (competence, cutoff_date, generated_by, status)
    VALUES ('202605', DATE '2026-04-30', 'system.batch', 'OPEN');

-- ----------------------------------------------------------------------------
-- Pagamentos para os 8 beneficiários ACTIVE.
-- gross_amount já calculado: base_amount * regional_factor * income_factor
-- (na app real, o PaymentService calcula — aqui pré-computamos para a demo).
-- ----------------------------------------------------------------------------
INSERT INTO payment.payment
    (cycle_id, beneficiary_id, beneficiary_cpf, payment_type, gross_amount, deduction_total, status)
SELECT
    (SELECT id FROM payment.payment_cycle WHERE competence = '202605'),
    b.id,
    b.cpf,
    'N',
    ROUND(sp.base_amount * rf.factor * ib.factor, 2),
    0.00,
    'PENDING'
FROM beneficiary.beneficiary b
JOIN social_program.social_program sp     ON sp.id = b.program_id
JOIN reference.regional_factor rf         ON rf.region_code = b.region_code
JOIN reference.income_bracket ib
    ON b.family_income >= ib.min_income
   AND (ib.max_income IS NULL OR b.family_income <= ib.max_income)
WHERE b.status = 'A';

-- ----------------------------------------------------------------------------
-- Exemplo de descontos: contribuição social no primeiro pagamento + judicial
-- ----------------------------------------------------------------------------
WITH primeiro AS (
    SELECT p.id, p.gross_amount
    FROM payment.payment p
    JOIN beneficiary.beneficiary b ON b.id = p.beneficiary_id
    WHERE b.cpf = '11144477735'
)
INSERT INTO payment.payment_deduction (payment_id, deduction_type, amount, percentage, process_number)
SELECT id, 'CONTRIB', ROUND(gross_amount * 0.030, 2), 3.00, NULL FROM primeiro
UNION ALL
SELECT id, 'JUDICIAL', 50.00, NULL, '0001234-56.2025.8.26.0100' FROM primeiro;

-- Atualiza deduction_total dos pagamentos com base nos descontos
UPDATE payment.payment p
SET deduction_total = COALESCE((
    SELECT SUM(amount) FROM payment.payment_deduction WHERE payment_id = p.id
), 0);

-- ----------------------------------------------------------------------------
-- Eventos de auditoria — criação dos beneficiários
-- ----------------------------------------------------------------------------
INSERT INTO audit.audit_event (aggregate_type, aggregate_id, event_type, actor, payload)
SELECT 'BENEFICIARY', id, 'CREATED', 'seed.demo',
       jsonb_build_object('cpf', cpf, 'status', status)
FROM beneficiary.beneficiary;
