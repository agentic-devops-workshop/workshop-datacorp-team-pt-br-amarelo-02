# SPECIFICATION — SIFAP 2.0

**Time**: PT-BR Amarelo 02
**Data**: 2026-05-20
**Versão**: 1.0
**Status**: Em revisão — aguardando sign-off do Par 1 (PO)

---

## Bounded Contexts

| Contexto | Módulo Java | Responsabilidade |
|----------|-------------|-----------------|
| Cadastro | `beneficiary` | CRUD de beneficiários, dependentes e programas sociais |
| Pagamento | `payment` | Geração de ciclo, cálculo de benefício e descontos |
| Elegibilidade | `eligibility` | Validações de entrada, regras de corte e status |
| Auditoria | `audit` | Registro imutável de eventos de negócio |

---

## Requisitos EARS

### Módulo: Pagamento (payment)

```yaml
REQ-PAY-001:
  pattern: event-driven
  text: "Quando um ciclo de pagamento mensal for iniciado, o SIFAP deve gerar registros de pagamento apenas para beneficiários com status ACTIVE."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L88-L142
  acceptance:
    - "10 beneficiários ativos + 2 suspensos produzem exatamente 10 registros de pagamento."
    - "Beneficiários com status S, C, I ou D não geram registro."
```

```yaml
REQ-PAY-002:
  pattern: ubiquitous
  text: "O SIFAP deve iniciar todo registro de pagamento com status PENDING."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L156
  acceptance:
    - "Novo pagamento criado possui campo status = PENDING."
    - "Nenhum pagamento é criado com status diferente de PENDING."
```

```yaml
REQ-PAY-003:
  pattern: unwanted
  text: "O SIFAP não deve gerar pagamento duplicado para o mesmo CPF e competência."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L207-L211
  acceptance:
    - "Segunda execução do ciclo para a mesma competência não cria registros duplicados."
    - "Constraint UNIQUE(cpf, competencia) existe no banco."
```

```yaml
REQ-PAY-004:
  pattern: event-driven
  text: "Quando o lote de pagamentos for processado, o SIFAP deve ordenar beneficiários por CPF em ordem crescente antes da geração."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L196-L202
  acceptance:
    - "Registros de pagamento no resultado do batch estão em ordem crescente de CPF."
    - "Query usa ORDER BY cpf ASC explicitamente."
```

```yaml
REQ-PAY-005:
  pattern: state-driven
  text: "Enquanto vigente uma competência, o SIFAP deve considerar elegível apenas o beneficiário que estava ACTIVE no último dia do mês anterior."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L180-L190
  acceptance:
    - "Beneficiário ativado no dia 1 do mês corrente não entra no ciclo atual."
    - "Beneficiário suspenso no dia 1 do mês corrente ainda recebe (estava ACTIVE no corte)."
```

```yaml
REQ-PAY-006:
  pattern: event-driven
  text: "Quando o benefício for calculado, o SIFAP deve aplicar o fator regional correspondente à UF do beneficiário sobre o valor base."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L138-L165
  acceptance:
    - "Beneficiário do AC (fator 1.35) com base 1000 recebe valor ajustado de 1350."
    - "Tabela de 27 fatores regionais é parametrizável no banco."
```

```yaml
REQ-PAY-007:
  pattern: event-driven
  text: "Quando o benefício for calculado, o SIFAP deve aplicar o fator de renda familiar por faixa sobre o valor base."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L167-L177
  acceptance:
    - "Renda familiar < R$300 aplica fator 1.00 (sem redução)."
    - "Renda familiar > R$1500 aplica fator 0.40."
    - "Tabela de 5 faixas é parametrizável no banco."
```

### Módulo: Descontos (payment — subdomínio)

```yaml
REQ-PAY-008:
  pattern: unwanted
  text: "O SIFAP não deve permitir que o total de descontos não judiciais exceda 30% do valor bruto do pagamento."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148
  acceptance:
    - "Desconto não judicial de 35% é truncado para 30%."
    - "Tipos C, I, S, P, A respeitam o teto."
```

```yaml
REQ-PAY-009:
  pattern: event-driven
  text: "Quando um desconto judicial for aplicado, o SIFAP deve somar o valor ao total sem aplicar o teto de 30%."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L156-L160
  acceptance:
    - "Desconto judicial de 50% é aceito integralmente."
    - "Mistura de judicial (20%) + não judicial (25%) totaliza 45% aceito."
```

```yaml
REQ-PAY-010:
  pattern: ubiquitous
  text: "O SIFAP deve calcular a contribuição social usando faixas progressivas: até R$500 → 3%, até R$1000 → 5%, até R$2000 → 7%, acima de R$2000 → 9%."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L45-L60
  acceptance:
    - "Valor bruto de R$800 aplica alíquota de 5%."
    - "Valor bruto de R$3000 aplica alíquota de 9%."
```

```yaml
REQ-PAY-011:
  pattern: ubiquitous
  text: "O SIFAP deve aplicar desconto sindical fixo de 1% sobre o valor bruto."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L165
  acceptance:
    - "Valor bruto de R$1000 gera desconto sindical de R$10."
```

### Módulo: Elegibilidade (eligibility)

```yaml
REQ-ELG-001:
  pattern: ubiquitous
  text: "O SIFAP deve validar CPF usando o algoritmo módulo 11 da Receita Federal e rejeitar CPFs inválidos."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L114-L135
  acceptance:
    - "CPF 000.000.000-00 é rejeitado."
    - "CPF com dígito verificador inválido retorna erro 400."
    - "CPF válido é aceito sem erro."
```

```yaml
REQ-ELG-002:
  pattern: unwanted
  text: "O SIFAP não deve aceitar UF fora da lista oficial das 27 unidades federativas."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L76-L102
  acceptance:
    - "UF 'XX' retorna erro de validação."
    - "UF 'SP' é aceita."
```

```yaml
REQ-ELG-003:
  pattern: state-driven
  text: "Enquanto o cadastro estiver em validação, o SIFAP deve exigir que o campo nome contenha ao menos nome e sobrenome separados por espaço."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L162-L180
  acceptance:
    - "Nome 'Maria' (sem sobrenome) é rejeitado."
    - "Nome 'Maria Silva' é aceito."
```

```yaml
REQ-ELG-004:
  pattern: event-driven
  text: "Quando uma data de nascimento for informada, o SIFAP deve validar o formato AAAAMMDD e rejeitar datas inválidas."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L143-L160
  acceptance:
    - "Data 20260230 (30 de fevereiro) é rejeitada."
    - "Data 20000229 (ano bissexto) é aceita."
```

### Módulo: Tipos de Pagamento (payment — subdomínio)

```yaml
REQ-PAY-012:
  pattern: event-driven
  text: "Quando o mês de competência for dezembro, o SIFAP deve gerar pagamento do tipo DÉCIMO (D) calculando o 13º salário com a fórmula: VLR_13 = VLR_BASE × FATOR_REGIONAL × FATOR_IDADE, truncado em 2 casas decimais."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L243-L250
  acceptance:
    - "Em dezembro, pagamento é gerado com tipo D."
    - "VLR_13 usa fórmula específica (sem fator renda nem fator família)."
    - "Valor é truncado (não arredondado) em 2 casas decimais."
```

```yaml
REQ-PAY-013:
  pattern: complex
  text: "Enquanto o mês for dezembro e quando o programa social for do tipo A, o SIFAP deve adicionar abono natalino de 15% sobre o valor do benefício normal ao valor bruto do pagamento DÉCIMO."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L252-L259
  acceptance:
    - "Programa tipo A em dezembro: VLR_BRUTO = VLR_BENF + VLR_13 + (VLR_BENF × 0.15)."
    - "Programa tipo diferente de A em dezembro: VLR_BRUTO = VLR_BENF + VLR_13 (sem abono)."
    - "Abono é truncado em 2 casas decimais."
```

```yaml
REQ-PAY-014:
  pattern: ubiquitous
  text: "O SIFAP deve classificar pagamentos nos tipos: N (NORMAL — mensal padrão), D (DÉCIMO — 13º salário em dezembro). O tipo T (TERCEIRO) deve ser registrado como enum válido mas não processado até que a lógica de cálculo seja definida."
  source_legacy: 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L236-L243
  acceptance:
    - "Enum PaymentType contém N, D e T."
    - "Meses 1-11 geram tipo N."
    - "Dezembro gera tipo D."
    - "Tipo T é aceito na persistência mas não gerado automaticamente pelo batch."
```

### Módulo: Auditoria e Segurança (audit + cross-cutting)

```yaml
REQ-AUD-001:
  pattern: ubiquitous
  text: "O SIFAP deve registrar um evento de auditoria para toda alteração em entidades de domínio, contendo estado anterior e posterior em formato JSON."
  source_legacy: "[GREENFIELD] O legado possui RELAUDIT.NSN para relatórios, mas não registra alterações em tempo real. Auditoria real-time é requisito regulatório moderno."
  acceptance:
    - "Alteração de status de beneficiário gera registro em audit_event."
    - "Registro contém campos: entity_type, entity_id, action, before_state, after_state, timestamp, user_id."
```

```yaml
REQ-SEC-001:
  pattern: ubiquitous
  text: "O SIFAP deve autenticar todas as chamadas de API usando OAuth2 com tokens JWT."
  source_legacy: "[GREENFIELD] O legado usava autenticação por sessão de terminal 3270. A API REST moderna requer autenticação stateless por token."
  acceptance:
    - "Requisição sem header Authorization retorna 401."
    - "Token expirado retorna 401."
    - "Token válido permite acesso ao recurso."
```

---

## Resumo de Cobertura

| Módulo | REQ-IDs | Regras legadas cobertas |
|--------|---------|------------------------|
| Pagamento | REQ-PAY-001 a REQ-PAY-014 | BR-001 a BR-006, BR-011 a BR-016 |
| Elegibilidade | REQ-ELG-001 a REQ-ELG-004 | BR-007 a BR-010 |
| Auditoria | REQ-AUD-001 | [GREENFIELD] |
| Segurança | REQ-SEC-001 | [GREENFIELD] |
| **Total** | **20 REQ-IDs** | **16 BRs cobertas + 2 greenfield** |

---

## Rastreabilidade Legado → Requisito

| BR-ID | REQ-ID | Programa Fonte |
|-------|--------|---------------|
| BR-001 | REQ-PAY-008 | CALCDSCT.NSN#L142-L148 |
| BR-002 | REQ-PAY-009 | CALCDSCT.NSN#L156-L160 |
| BR-003 | REQ-PAY-011 | CALCDSCT.NSN#L165 |
| BR-004 | REQ-PAY-010 | CALCDSCT.NSN#L45-L60 |
| BR-005 | REQ-PAY-001 | BATCHPGT.NSN#L88-L142 |
| BR-006 | REQ-PAY-002 | BATCHPGT.NSN#L156 |
| BR-007 | REQ-ELG-001 | VALBENEF.NSN#L114-L135 |
| BR-008 | REQ-ELG-004 | VALBENEF.NSN#L143-L160 |
| BR-009 | REQ-ELG-003 | VALBENEF.NSN#L162-L180 |
| BR-010 | REQ-ELG-002 | VALBENEF.NSN#L76-L102 |
| BR-011 | REQ-PAY-006 | BATCHPGT.NSN#L138-L165 |
| BR-012 | REQ-PAY-007 | BATCHPGT.NSN#L167-L177 |
| BR-013 | REQ-PAY-004 | BATCHPGT.NSN#L196-L202 |
| BR-014 | REQ-PAY-003 | BATCHPGT.NSN#L207-L211 |
| BR-015 | REQ-PAY-012, REQ-PAY-013, REQ-PAY-014 | CALCBENF.NSN#L236-L259 |
| BR-016 | REQ-PAY-005 | BATCHPGT.NSN#L180-L190 |
