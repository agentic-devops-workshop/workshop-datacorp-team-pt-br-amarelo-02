-- ============================================================================
-- V2__seed_reference_data.sql
-- ============================================================================
-- Dados de referência extraídos das tabelas hardcoded do legado Natural.
-- Estes dados são determinísticos e parte do domínio — não são "mocks".
--
-- Origem:
--   - 27 UFs + fatores regionais : BATCHPGT.NSN#L138-L165 (BR-010 / BR-011)
--   - 5 faixas de renda          : BATCHPGT.NSN#L167-L177 (BR-012)
--   - 4 faixas de contribuição   : CALCDSCT.NSN#L45-L60   (BR-004)
--   - 3 programas sociais base   : PROGRAMA-SOCIAL.ddm + business-rules-catalog.md
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 27 UFs com fator regional (BR-011)
-- Norte (1.30-1.35) > Nordeste (1.20-1.25) > Centro-Oeste (1.10-1.15)
--   > Sudeste (1.05-1.10) > Sul (1.00-1.05)
-- ----------------------------------------------------------------------------
INSERT INTO reference.regional_factor (region_code, uf, uf_name, factor) VALUES
    ( 1, 'AC', 'Acre',                1.35),
    ( 2, 'AL', 'Alagoas',              1.20),
    ( 3, 'AP', 'Amapá',                1.30),
    ( 4, 'AM', 'Amazonas',             1.30),
    ( 5, 'BA', 'Bahia',                1.20),
    ( 6, 'CE', 'Ceará',                1.25),
    ( 7, 'DF', 'Distrito Federal',     1.10),
    ( 8, 'ES', 'Espírito Santo',       1.05),
    ( 9, 'GO', 'Goiás',                1.10),
    (10, 'MA', 'Maranhão',             1.25),
    (11, 'MT', 'Mato Grosso',          1.15),
    (12, 'MS', 'Mato Grosso do Sul',   1.10),
    (13, 'MG', 'Minas Gerais',         1.05),
    (14, 'PA', 'Pará',                 1.30),
    (15, 'PB', 'Paraíba',              1.20),
    (16, 'PR', 'Paraná',               1.00),
    (17, 'PE', 'Pernambuco',           1.20),
    (18, 'PI', 'Piauí',                1.25),
    (19, 'RJ', 'Rio de Janeiro',       1.05),
    (20, 'RN', 'Rio Grande do Norte',  1.20),
    (21, 'RS', 'Rio Grande do Sul',    1.00),
    (22, 'RO', 'Rondônia',             1.00),
    (23, 'RR', 'Roraima',              1.00),
    (24, 'SC', 'Santa Catarina',       1.00),
    (25, 'SP', 'São Paulo',            1.05),
    (26, 'SE', 'Sergipe',              1.20),
    (27, 'TO', 'Tocantins',            1.30);

-- ----------------------------------------------------------------------------
-- 5 faixas de renda familiar (BR-012)
-- ----------------------------------------------------------------------------
INSERT INTO reference.income_bracket (id, min_income, max_income, factor) VALUES
    (1,    0.00,  300.00, 1.00),
    (2,  300.01,  600.00, 0.85),
    (3,  600.01, 1000.00, 0.70),
    (4, 1000.01, 1500.00, 0.55),
    (5, 1500.01,    NULL, 0.40);

-- ----------------------------------------------------------------------------
-- 4 faixas de contribuição social (BR-004)
-- até R$500 -> 3% | até R$1000 -> 5% | até R$2000 -> 7% | acima -> 9%
-- ----------------------------------------------------------------------------
INSERT INTO reference.contribution_bracket (id, min_amount, max_amount, rate) VALUES
    (1,    0.00,  500.00, 0.030),
    (2,  500.01, 1000.00, 0.050),
    (3, 1000.01, 2000.00, 0.070),
    (4, 2000.01,    NULL, 0.090);

-- ----------------------------------------------------------------------------
-- Programas sociais base (origem: PROGRAMA-SOCIAL.ddm + glossário)
-- ----------------------------------------------------------------------------
INSERT INTO social_program.social_program
    (code, name, base_amount, adjustment_factor, max_income, status) VALUES
    ('BPC',   'Benefício de Prestação Continuada',  1412.00, 1.000, 1500.00, 'A'),
    ('SECA',  'Auxílio Seca',                        600.00, 1.000, 1000.00, 'A'),
    ('FAM',   'Auxílio Família',                     400.00, 1.000,  800.00, 'A');
