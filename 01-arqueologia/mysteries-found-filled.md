<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado (PREENCHIDO)

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Reference](https://img.shields.io/badge/TIPO-Reference-1A1A1A?style=for-the-badge) ![PREENCHIDO S1](https://img.shields.io/badge/STATUS-Preenchido-7FBA00?style=for-the-badge)

---

## M1 · Por Que 27 UFs? (Faltam 1-2 UFs ou Há Duplicação?)

**Severidade:** 🟡 MÉDIO

**Localização:** [VALBENEF.NSN#L76-L102](legado-sifap/natural-programs/VALBENEF.NSN), [BATCHPGT.NSN#L138-L165](legado-sifap/natural-programs/BATCHPGT.NSN)

**Código:**
```natural
1 #UF-TAB (A2/27)
...
FOR #I = 1 TO 27
  MOVE 'AC' TO #UF-TAB(1)  /* Acre */
  MOVE 'AL' TO #UF-TAB(2)  /* Alagoas */
  ...
  MOVE 'TO' TO #UF-TAB(27) /* Tocantins */
END-FOR
```

**Questão:** Brasil tem 26 estados + 1 DF = 27. Qual UF está na posição 27? É DF ou outro?

**Hipóteses:**
1. ✅ DF está incluído como posição 27 (mais provável)
2. ❓ Uma UF está duplicada
3. ❓ Uma UF obsoleta está mantida por compatibilidade

**Impacto:** Se DF não está válido, beneficiários em DF são rejeitados.

**Resolução:** [ ] Verificar arquivo .ddm | [ ] Consultar especialista | [ ] Testar com UF='DF'

---

## M2 · "Sistemas Downstream Dependem Desta Ordenação" — Quais?

**Severidade:** 🔴 **CRÍTICO** — Risco de Ruptura Arquitetural

**Localização:** [BATCHPGT.NSN#L196-L202](legado-sifap/natural-programs/BATCHPGT.NSN)

**Código:**
```natural
* PROCESSAMENTO PRINCIPAL
* LEITURA EM ORDEM ALFABETICA POR CPF (OTIMIZACAO 1999)
* NOTA: SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO
READ BENEFICIARIO-V BY CPF
  ADD 1 TO #QTD-PROCESSADOS
  ...
```

**Questão:** Quais sistemas dependem da ordem CPF?

**Hipóteses:**
1. Sistema de transferência eletrônica (TEF) confere sequência?
2. Relatório de auditoria assume ordem?
3. Integração com SIAFI requer ordem?
4. Sistema de balanceamento de carga assume ordem?

**Impacto:**
- 🔴 Se migrarmos para PostgreSQL, ORDER BY CPF pode não ser garantido sem índice
- Se downstream confere ordem e não acha, rejeita lote inteiro
- Estágio 4 pode descobrir isso em produção

**Resolução:**
```
[ ] BLOQUEADOR — Entrevistar dono de cada sistema downstream
[ ] Questão: ordem CPF é mandatória ou nice-to-have?
[ ] Documentação: ADR em Estágio 2 sobre como garantir ordem
```

---

## M3 · Tipos de Pagamento D e T — Qual a Lógica?

**Severidade:** 🟡 ALTO

**Localização:** [CALCBENF.NSN#L26](legado-sifap/natural-programs/CALCBENF.NSN), [BATCHPGT.NSN#L26](legado-sifap/natural-programs/BATCHPGT.NSN)

**Código:**
```natural
1 PAGAMENTO-V VIEW OF PAGAMENTO
  2 TIPO-PGTO (A1)  /* N=NORMAL D=DECIMO T=TERCEIRO */
```

**Questão:** O que significa D (Décimo) e T (Terceiro)?

**Hipóteses:**
1. D = 13º salário (abono natalino em dezembro)
2. T = terceira parcela de quê? Abono? Benefício especial?
3. São ciclos adicionais mensais, ou componentes de um único mês?

**Encontrado:** Código menciona "ABONO NATALINO" (2009) mas lógica de cálculo diferenciada não vista.

**Impacto:** Se D=13º e não implementarmos corretamente, beneficiários ganham/perdem em dezembro.

**Resolução:**
```
[ ] BLOQUEADOR — Ler CALCCORR.NSN (pode estar lá a lógica)
[ ] Questão: como é gerado tipo D e T? Pelo batch ou outro programa?
[ ] Teste: qual é a frequência? Quais meses?
```

---

## M4 · Por Que Truncar Manualmente em Vez de ROUND?

**Severidade:** 🟠 MÉDIO

**Localização:** [CALCDSCT.NSN#L127-L133](legado-sifap/natural-programs/CALCDSCT.NSN)

**Código:**
```natural
* CALC TETO MAXIMO DESCONTO - 30% DO BRUTO
COMPUTE #VLR-MAX-DSCT = #VLR-BRUTO * 0.30
* TRUNCAR
COMPUTE #VLR-TEMP = #VLR-MAX-DSCT * 100
COMPUTE #VLR-MAX-DSCT = #VLR-TEMP / 100
```

**Questão:** Por que não usar ROUND?

**Hipótese:** Razão histórica (Adabas 1999?) ou intencional (truncar nunca exceder 30%)?

**Impacto:** Se migrarmos com `Math.round()`, resultados diferem em centavos. Acumula ao longo de meses (centavo × 500k beneficiários = milhões).

**Exemplo:**
```
VLR-BRUTO = 999.99
30% = 299.997
Truncado = 299.99  ✅
Arredondado = 300.00  ❌ (excede teto!)
```

**Resolução:**
```
[ ] Questão: é intencional truncar ou arredondar?
[ ] Teste: processar 100 pagamentos com fracionários, comparar
[ ] Documentação: EARS clara ("deve truncar, não arredondar")
```

---

## M5 · Campo #VLR-TEMP (N11) — Por Que Integer em Cálculo?

**Severidade:** 🟠 MÉDIO

**Localização:** [CALCDSCT.NSN#L31](legado-sifap/natural-programs/CALCDSCT.NSN)

**Código:**
```natural
1 #VLR-TEMP (N11)  /* Integer! Por que não N9.2? */
```

**Questão:** Por que não N9.2 para cálculo financeiro?

**Impacto:** Se #VLR-BRUTO > 99,999.99, multiplicação por 100 pode overflow.

**Resolução:**
```
[ ] Verificar: qual é o máximo teórico de VLR-BRUTO?
[ ] Documentar domínio de valores esperados
```

---

## M6 · RELAUDIT.NSN — Onde Está a Lógica de Auditoria?

**Severidade:** 🔴 **CRÍTICO** — Risco de Compliance

**Localização:** [RELAUDIT.NSN](legado-sifap/natural-programs/RELAUDIT.NSN) (não analisado ainda)

**Questão:** Como funciona a rastreabilidade de mudanças?

**O que não vimos:**
- ❓ Quem grava em audit log? BATCHPGT? CALCDSCT? CADBENEF?
- ❓ Qual informação é gravada? Antes/depois de cada campo?
- ❓ Quem tem acesso a RELAUDIT?
- ❓ Retenção de dados — quanto tempo mantém?

**Impacto:**
- 🔴 Se não entendermos auditoria atual, não conseguimos garantir compliance em Estágio 3
- Órgãos auditores (CGU, TCU) podem rejeitar sistema sem rastreamento
- Erro não rastreado = problema legal

**Resolução:**
```
[ ] BLOQUEADOR — Ler RELAUDIT.NSN completamente
[ ] Questão: existe tabela de auditoria? Ou é relatório sobre logs?
[ ] Documentação: mapear quem/quando/o-quê é auditado
[ ] EARS: "toda mudança em PAGAMENTO deve ser auditada com before/after"
```

---

## M7 · Fevereiro com 29 Dias (Sempre Bissexto?)

**Severidade:** 🟢 BAIXO

**Localização:** [VALBENEF.NSN#L104-L115](legado-sifap/natural-programs/VALBENEF.NSN)

**Código:**
```natural
1 #DIAS-MES (N2/12)
...
MOVE 31 TO #DIAS-MES(1)
MOVE 29 TO #DIAS-MES(2)  /* CONSIDERA BISSEXTO */
MOVE 31 TO #DIAS-MES(3)
```

**Questão:** Validação sempre permite 29/02, mesmo em anos não-bissextos?

**Exemplo:** Data 29/02/2023 (não-bissexto) — aceita ou rejeita?

**Impacto:** Baixo. Beneficiários com data 29/02 em ano não-bissexto ficariam com data inválida.

**Resolução:** [ ] Testar | [ ] Se aceita, implementar fórmula bissexto no Estágio 3

---

## M8 · Tipo C (Contrib) vs I (Imposto) — Diferença?

**Severidade:** 🟡 ALTO

**Localização:** [CALCDSCT.NSN#L157-L173](legado-sifap/natural-programs/CALCDSCT.NSN)

**Código:**
```natural
VALUE 'C'
  * CONTRIBUICAO (OBRIGATORIO)
  COMPUTE #VLR-DSCT-ITEM = #VLR-BRUTO * #ALIQ-CONTRIB(#K)
  ADD #VLR-DSCT-ITEM TO #VLR-TOTAL-DSCT

VALUE 'I'
  * IMPOSTO RETIDO
  COMPUTE #VLR-DSCT-ITEM = #VLR-BRUTO * (BENEFICIARIO-V.PCT-DSCT(#IDX) / 100)
  ADD #VLR-DSCT-ITEM TO #VLR-TOTAL-DSCT
```

**Questão:** Qual é a diferença prática?

- Tipo C: Alíquota vem de tabela hardcoded, baseada em faixa de valor bruto
- Tipo I: Alíquota vem de campo cadastrado, sempre aplicada

**Hipótese:** C = contribuição social (automática), I = imposto de renda (variável por beneficiário)

**Resolução:** [ ] Teste com beneficiário real | [ ] EARS separada para cada tipo

---

## M9 · NUM-DEPENDENTES Está no Schema mas Não Vejo Uso

**Severidade:** 🟡 ALTO

**Localização:** [CALCBENF.NSN#L17](legado-sifap/natural-programs/CALCBENF.NSN), [BATCHPGT.NSN#L17](legado-sifap/natural-programs/BATCHPGT.NSN)

**Código:**
```natural
1 BENEFICIARIO-V VIEW OF BENEFICIARIO
  2 NUM-DEPENDENTES (N2)
```

**Questão:** NUM-DEPENDENTES afeta cálculo de benefício?

**O que esperamos:** Mais dependentes = benefício aumenta

**O que vimos:** Nenhuma lógica em CALCBENF que use NUM-DEPENDENTES

**Impacto:** 🟡 Se dependentes afetam mas não implementamos, benefícios saem errados.

**Resolução:**
```
[ ] BLOQUEADOR — Analisar CALCBENF completamente
[ ] Ler CADBENEF para ver como dependentes são cadastrados
[ ] Questão: existe lógica em programa diferente?
```

---

## M10 · Status 'D' (Deleted) — Existe ou É Documentação Obsoleta?

**Severidade:** 🟠 MÉDIO

**Questão:** Beneficiário pode ter status='D'? Soft delete ou hard delete?

**Documentado:**
- A = ACTIVE
- S = SUSPENDED
- C = CANCELLED
- I = INACTIVE
- D = DELETED (presumido)

**Impacto:**
- Se soft delete: BATCHPGT ignora, nunca processa
- Se hard delete: chave estrangeira em PAGAMENTO pode quebrar

**Resolução:** [ ] Teste | [ ] Questão: quantos beneficiários com status='D' existem? | [ ] EARS clara

---

## Tabela de Mistérios (Resumida)

| ID | Descrição | Sev | Bloqueador |
|----|-----------|-----|-----------|
| M1 | 27 UFs? | 🟡 MÉDIO | ❌ |
| **M2** | **Sistemas downstream?** | 🔴 CRÍTICO | ✅ |
| **M3** | **Tipos D e T?** | 🟡 ALTO | ✅ |
| M4 | Truncar vs round? | 🟠 MÉDIO | ❌ |
| M5 | #VLR-TEMP N11? | 🟠 MÉDIO | ❌ |
| **M6** | **RELAUDIT.NSN?** | 🔴 CRÍTICO | ✅ |
| M7 | Fevereiro 29? | 🟢 BAIXO | ❌ |
| M8 | Tipo C vs I? | 🟡 ALTO | ❌ |
| **M9** | **NUM-DEPENDENTES?** | 🟡 ALTO | ✅ |
| M10 | Status D? | 🟠 MÉDIO | ❌ |

---

## Próximas Etapas

### Imediatamente (Antes do Estágio 2)

**BLOQUEADORES** = [M2, M3, M6, M9]

- [ ] Reunião com dono de sistemas downstream (M2)
- [ ] Ler CALCCORR.NSN e RELAUDIT.NSN (M3, M6)
- [ ] Analisar CALCBENF focar em NUM-DEPENDENTES (M9)

### Estágio 2 (Especificação)

Para cada mistério resolvido, escrever **EARS correspondente**:
- M2 → "Quando batch é executado, deve processar beneficiários em ordem CPF"
- M3 → "Tipo D de pagamento calcula…"
- M6 → "Toda mudança em PAGAMENTO deve ser auditada…"
- M9 → "Beneficiário com X dependentes tem benefício aumentado de…"
