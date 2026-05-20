<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital (PREENCHIDO)

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHIDO S1](https://img.shields.io/badge/STATUS-Preenchido-7FBA00?style=for-the-badge)

---

## 1. Sumário Executivo

O **SIFAP** (Sistema de Fiscalização e Administração de Pagamentos) é um sistema financeiro crítico que processa pagamentos mensais de benefícios sociais. Roda em **Natural/Adabas desde 1997**, com batch mensal que gera dezenas de milhares de registros de pagamento. 

**Código está bem estruturado** — comentários claros, nomes descritivos, histórico de alterações documentado. **Porém altamente acoplado** — tabelas de fatores regionais, faixas de renda e alíquotas estão hardcoded em 2-3 programas.

**Modernização exigirá:**
1. Externalizar configuração (tabelas, fatores, alíquotas)
2. Implementar auditoria robusta em tempo real
3. Clarificar contrato com "sistemas downstream"
4. Mapear os 11 programas restantes

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

**O que é:** Sistema que calcula, valida, aprova e registra pagamentos de benefícios sociais mensalmente.

**Entrada:** Beneficiários cadastrados com atributos (CPF, renda familiar, região, dependentes, programa social).

**Processamento:** 
1. Batch executado no **1º dia útil do mês**
2. Para cada beneficiário **ACTIVE**: calcula valor bruto (com ajustes regionais + renda)
3. Aplica descontos (contribuição social, judicial, sindical, etc.) com **teto de 30%**
4. Cria registro de pagamento com **status PENDING**

**Saída:** Registros de pagamento prontos para processamento por sistemas downstream.

**Criticidade:** 🔴 **CRÍTICO** — integrado com sistemas financeiros federais. Erro nos cálculos causa prejuízo direto ao beneficiário.

### 2.2 Arquitetura Legada

**Linguagem:** Natural (procedural, mainframe-style)

**Base de dados:** Adabas (hierarchical, DDM-based)

**Estrutura:**
- **Batch:** BATCHPGT, BATCHCON, BATCHREL (processamento em lote)
- **Cálculo:** CALCBENF, CALCDSCT (fórmulas)
- **Cadastro:** CADBENEF, CADPROG, CADDEPEND (manutenção)
- **Validação:** VALBENEF, VALDOCS, VALELEG (regras de entrada)
- **Consulta/Relatório:** CONSBENF, RELPGT, RELAUDIT (saídas)

**Fluxo crítico:** `Scheduler → BATCHPGT → CALCBENF → CALCDSCT → INSERT PAGAMENTO`

### 2.3 Usuários e Perfis

| Programa | Usuário | Perfil | Frequência |
|----------|---------|--------|-----------|
| BATCHPGT | Scheduler/DBA | Automatizado | Diário (1º dia útil) |
| CADBENEF | Operador | Entrada manual | Ad-hoc |
| CONSBENF | Consultor/Auditoria | Leitura | Ad-hoc |
| RELPGT | Gestor/CFO | Leitura | Mensal |
| RELAUDIT | Auditor Interno | Leitura | Mensal/quinzenal |

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

| # | Regra | Programa | Risco |
|---|-------|----------|-------|
| 1 | **Teto de descontos 30% (exceto judicial)** | CALCDSCT | 🔴 CRÍTICO — hardcoded, requer recompilação para mudar |
| 2 | **Contribuição social por faixa** | CALCDSCT | 🔴 CRÍTICO — 4 faixas hardcoded, alteração 2015 |
| 3 | **Fator regional por UF** | BATCHPGT, CALCBENF | 🟡 ALTO — 27 multiplicadores, sem paramétrico |
| 4 | **Fator de renda familiar** | BATCHPGT | 🟡 ALTO — 5 faixas hardcoded, alteração 2013 |
| 5 | **Batch ordenado por CPF** | BATCHPGT | 🔴 CRÍTICO — "sistemas downstream dependem" (não documentado quem) |
| 6 | **Data de corte** | BATCHPGT | 🔴 CRÍTICO — auditorias 2018 apontaram erros |

### 3.2 Dependências Complexas

**Acoplamentos perigosos:**
- BATCHPGT → CALCBENF → CALCDSCT (série síncrona, falha de uma quebra tudo)
- Tabelas globais hardcoded em 2+ programas (TAB-REG, FAIXA-RENDA)
- Ordem CPF obrigatória (downstream não documentado)
- PE (Periodic Group) para descontos (lógica com loop complexo)

### 3.3 Dívida Técnica Identificada

| Item | Sev | Descrição |
|------|-----|-----------|
| **Hardcoding de tabelas** | 🔴 | TAB-REG (27), FAIXA-RENDA (5), alíquotas (4) codificados sem paramétrico |
| **Truncamento manual** | 🟡 | Cálculos truncam com divisão por 100 em vez de ROUND — acumula erros |
| **Sem auditoria clara** | 🟡 | RELAUDIT mencionado mas não analisado — compliance em risco |
| **Documentação de downstream** | 🟡 | "SISTEMAS DOWNSTREAM DEPENDEM" mas não identifica quais |
| **Sem testes automatizados** | 🟡 | Presume-se não há testes — mudança exige teste manual completo |
| **Data de corte obscura** | 🟠 | Implementação atual funciona mas é confusa |
| **Tipos D/T parcial** | 🟠 | TIPO-PGTO=D/T mencionado, lógica não completamente vista |

### 3.4 Gaps de Documentação

**Não encontrado no código:**
- ❓ Máquina de estados de pagamento (PENDING → ? → DELIVERED)
- ❓ Cancelamento de pagamento (existe? como?)
- ❓ Correção de pagamento (CALCCORR.NSN não lido)
- ❓ Cálculo de 13º (TIPO-PGTO='D' mas lógica confusa)
- ❓ Soft vs hard delete (status 'D' existe?)
- ❓ Contrato com downstream (qual dados, formato, frequência?)

---

## 4. Mistérios e Riscos

### 4.1 Top 10 Mistérios

| ID | Descrição | Risco | Bloqueador |
|----|-----------|-------|-----------|
| M1 | Por que 27 UFs? (faltam 1-2 ou há duplicação?) | MÉDIO | ❌ |
| M2 | "Sistemas downstream dependem de ordem CPF" — quais? | 🔴 CRÍTICO | ✅ |
| M3 | Tipos D (Décimo) e T (Terceiro) — qual lógica? | ALTO | ✅ |
| M4 | Por que truncar manualmente em vez de ROUND? | MÉDIO | ❌ |
| M5 | Campo #VLR-TEMP (N11) — por que integer? | MÉDIO | ❌ |
| M6 | RELAUDIT.NSN — onde está a lógica de auditoria? | 🔴 CRÍTICO | ✅ |
| M7 | Fevereiro com 29 dias sempre? | BAIXO | ❌ |
| M8 | Tipo C (contrib) vs I (imposto) — diferença? | ALTO | ❌ |
| M9 | NUM-DEPENDENTES usa m cálculo? | ALTO | ✅ |
| M10 | Status 'D' (deleted) — existe? | MÉDIO | ❌ |

### 4.2 Riscos para Estágio 2

🔴 **BLOQUEADORES:**
1. Clarificar "sistemas downstream" — sem saber, Estágio 4 pode quebrar
2. Analisar CALCCORR, RELAUDIT, 11 programas restantes — podem haver regras críticas ocultas

🟡 **RISCOS DE ESPECIFICAÇÃO:**
3. Tabelas hardcoded — se deixarmos hardcoded em Java também, Estágio 4 quebra ao mudar fator regional
4. Ordem CPF — se criarmos índice diferente em PostgreSQL, reports quebram
5. Data de corte — implementação confusa, Estágio 2 precisa definir com clareza

---

## 5. Recomendações

### 5.1 O que Migrar Primeiro

| Prioridade | Funcionalidade | Justificativa |
|-----------|---|---|
| **1** | Cálculo benefício (CALCBENF) | Base de tudo. Impacto financeiro direto. |
| **2** | Cálculo descontos (CALCDSCT) | Depende de #1. Regras 30% complexas. |
| **3** | Batch geração (BATCHPGT) | Orquestra #1 e #2. Crítico operacional. |
| **4** | Validação cadastro (VALBENEF) | Protege integridade. CPF é standard. |
| **5** | Relatório (RELPGT) | Suporte downstream. Menos complexo. |

### 5.2 O que Descartar

- Terminal 3270 UI → Substituir por Next.js
- Batch de conferência → Reimplementar diferente
- Relatório flat → API + UI

### 5.3 O que Evoluir

| Funcionalidade | Melhoria |
|---|---|
| **Cálculo benefício** | Adicionar simulação (what-if) |
| **Descontos** | Workflow de aprovação judicial |
| **Batch** | Retry, circuit breaker, monitoramento real-time |
| **Validação** | Telefone, endereço, elegibilidade |
| **Auditoria** | Event sourcing em vez de apenas log |

---

## 6. Métricas do Estágio 1

| Métrica | Valor |
|---------|-------|
| Programas analisados | **4 / 15** (27%) |
| Programas restantes | **11** |
| Regras de negócio | **16** (estimativa: 24-30 com análise completa) |
| Regras críticas | **9** |
| Mistérios | **10** |
| Termos glossário | **40+** |
| Acoplamentos perigosos | **6** |
| DDMs mapeados | **3** (BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL) |
| Linhas analisadas | ~500 |
| Histórico de alterações | 15+ (desde 1997) |

---

## 7. Recomendações para Estágio 2

**BLOQUEADORES antes de começar:**

```yaml
BLOQUEADOR-1:
  titulo: "Clarificar sistemas downstream"
  acao: "Reunir com proprietários que consomem PAGAMENTO"
  resultado: "Documento: quem, qual formato, SLA"
  impacto_se_nao: "Specs sem contrato — risco de rejeição"

BLOQUEADOR-2:
  titulo: "Analisar 11 programas restantes"
  acao: "Time lê CALCCORR, RELAUDIT, BATCHCON, BATCHREL"
  resultado: "Identificar 8-12 regras adicionais, clarificar transições"
  impacto_se_nao: "Specs incompletas — débito técnico"

BLOQUEADOR-3:
  titulo: "Validar mistérios M2 e M6"
  acao: "Entrevistar autores/maintainers"
  resultado: "Resposta para cada mistério"
  impacto_se_nao: "Impl. Estágio 3 pode violar compliance/downstream"
```

---

## 8. Conclusão

SIFAP é um **sistema crítico, bem documentado, mas acoplado**. Modernização viável se:

1. ✅ Externalizar configuração antes de escrever specs
2. ✅ Clarificar contrato com downstream  
3. ✅ Implementar auditoria robusta
4. ✅ Completar mapeamento de 11 programas

**Risco geral:** 🟡 **MÉDIO-ALTO** — dependências e mistérios mapeados, resolvíveis com disciplina no Estágio 2.
