<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: PT-BR Amarelo 02
**Data**: 20/05/2026
**Edição**: Workshop DataCorp — Modernização SIFAP
**Participantes**: 5 pares (PO+RE, EA+SA, TL+Dev, DBA+QA, DevOps+TW)

---

## 1. Sumário Executivo

O **SIFAP** (Sistema de Fiscalização e Administração de Pagamentos) é um sistema financeiro crítico que processa pagamentos mensais de benefícios sociais, em Natural/Adabas desde 1997, com batch executado no **1º dia útil do mês** gerando dezenas de milhares de registros. O código é bem comentado e tem histórico de alterações rastreável (1997–2015), mas é **altamente acoplado**: tabelas de fatores regionais (27 UFs), faixas de renda (5) e alíquotas (4) estão hardcoded em múltiplos programas. A criticidade é **alta** — qualquer erro de cálculo causa prejuízo direto ao beneficiário. A modernização exigirá externalizar configuração, clarificar contratos com sistemas downstream e implementar auditoria robusta em tempo real.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Sistema que **calcula, valida, registra e audita** pagamentos de benefícios sociais mensalmente.

- **Entrada:** Beneficiários cadastrados (CPF, renda familiar, região, dependentes, programa social).
- **Processamento:** Batch mensal → para cada beneficiário ACTIVE calcula valor bruto (fator regional × fator familiar × fator renda × fator idade × reajuste) → aplica descontos (contrib social, judicial, sindical, imposto, pensão, administrativo) com teto de 30% (exceto judicial).
- **Saída:** Registros de pagamento com status PENDING para processamento por sistemas downstream (TEF/SIAFI presumido).
- **Criticidade:** 🔴 CRÍTICA — integrado com sistemas financeiros federais.

### 2.2 Arquitetura Legada

- **Linguagem:** Natural (procedural, mainframe-style).
- **Base de dados:** Adabas (DDM-based) — 4 arquivos: BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL, AUDITORIA.
- **15 programas Natural** organizados em 5 famílias funcionais:
  - **Batch:** BATCHPGT, BATCHCON, BATCHREL
  - **Cálculo:** CALCBENF, CALCDSCT, CALCCORR
  - **Cadastro:** CADBENEF, CADPROG, CADDEPEND
  - **Validação:** VALBENEF, VALDOCS, VALELEG
  - **Consulta/Relatório:** CONSBENF, RELPGT, RELAUDIT
- **Fluxo crítico:** `Scheduler → BATCHPGT → CALCBENF → CALCDSCT → STORE PAGAMENTO`.

### 2.3 Usuários e Perfis

| Programa  | Usuário              | Perfil           | Frequência          |
| --------- | -------------------- | ---------------- | ------------------- |
| BATCHPGT  | Scheduler/DBA        | Automatizado     | Mensal (1º dia útil)|
| CADBENEF  | Operador             | Entrada manual   | Ad-hoc              |
| CONSBENF  | Consultor/Auditoria  | Leitura          | Ad-hoc              |
| RELPGT    | Gestor/CFO           | Leitura          | Mensal              |
| RELAUDIT  | Auditor Interno      | Leitura          | Mensal/quinzenal    |

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas (TOP 5 — ver catálogo completo)

1. **BR-001** — Teto de descontos não-judiciais em 30% (`CALCDSCT.NSN#L142-L148`).
2. **BR-002** — Desconto judicial sem teto (`CALCDSCT.NSN#L156-L160`).
3. **BR-004** — Contribuição social por faixa de valor — 4 alíquotas (`CALCDSCT.NSN#L45-L60`).
4. **BR-005 + BR-013** — Geração mensal ordenada por CPF; downstream depende da ordem (`BATCHPGT.NSN#L88-L202`).
5. **BR-007** — Validação de CPF módulo 11 com exceção para CPFs `000...` de teste (`VALBENEF.NSN#L114-L135`).

### 3.2 Dependências Complexas

- **Cadeia síncrona:** `BATCHPGT → CALCBENF → CALCDSCT` — falha de qualquer um aborta o ciclo.
- **Tabelas hardcoded duplicadas:** `#TAB-REG(27)` e `#FAIXA-RENDA(5)` aparecem idênticas em BATCHPGT e CALCBENF (risco de divergência).
- **Ordem CPF acoplada a downstream:** mencionada explicitamente em comentário, sem documentação de quem consome.
- **PE Group (`DESCONTOS`)** em BENEFICIARIO — loop multi-valor com 6 tipos de desconto, lógica complexa de vigência e teto.

### 3.3 Dívida Técnica Identificada

- [x] **Hardcoding de tabelas** — fator regional, faixas de renda e alíquotas exigem recompilação para mudar.
- [x] **Truncamento manual** — `COMPUTE #VLR-TEMP = X * 100; COMPUTE X = #VLR-TEMP / 100` em vez de função ROUND/TRUNC nativa.
- [x] **Duplicação de inicialização** — TAB-REG aparece em ≥2 programas.
- [x] **Sem auditoria em tempo real visível** — RELAUDIT existe mas lógica não analisada.
- [x] **Contrato downstream não documentado** — comentário diz "sistemas downstream dependem" sem listar quais.
- [x] **Sem testes automatizados** — qualquer mudança exige bateria manual.
- [x] **Tipos D/T de pagamento parcialmente implementados** — `TIPO-PGTO='D'` no schema, lógica de 13º incompleta em CALCBENF.

### 3.4 Gaps de Documentação

- Máquina de estados do pagamento (PENDING → ? → DELIVERED) não documentada.
- Fluxo de cancelamento e estorno de pagamento ausente.
- CALCCORR.NSN (correção) não analisado em profundidade.
- Soft vs hard delete (status `D`) não esclarecido.
- Formato/SLA do contrato com sistemas downstream desconhecido.

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

| ID      | Descrição                                                       | Risco para Migração |
| ------- | --------------------------------------------------------------- | ------------------- |
| MYS-001 | 27 UFs — qual está na posição 27? DF ou duplicação?             | MÉDIO               |
| MYS-002 | "Sistemas downstream dependem desta ordenação" — quais?         | 🔴 CRÍTICO          |
| MYS-003 | TIPO-PGTO `D` (Décimo) e `T` (Terceiro) — lógica completa onde? | ALTO                |
| MYS-004 | Truncamento manual em vez de ROUND — intencional?               | MÉDIO               |
| MYS-005 | `#VLR-TEMP (N11)` integer em cálculo financeiro — overflow?     | MÉDIO               |
| MYS-006 | RELAUDIT.NSN — onde está a lógica de auditoria?                 | 🔴 CRÍTICO          |
| MYS-007 | Fevereiro com 29 dias hardcoded — sempre bissexto?              | BAIXO               |
| MYS-008 | Tipo `C` (contrib) vs `I` (imposto) — diferença prática?        | ALTO                |
| MYS-009 | `NUM-DEPENDENTES` cadastrado mas uso parcial no cálculo         | ALTO                |
| MYS-010 | Status `D` (deleted) — soft delete ou hard delete?              | MÉDIO               |

### 4.2 Riscos para o Estágio 2

1. **🔴 Sistemas downstream não identificados** — sem contrato claro, EARS de batch ficam incompletas.
2. **🔴 11 programas ainda não lidos em profundidade** — CALCCORR, RELAUDIT, BATCHCON, BATCHREL podem conter regras críticas.
3. **🟡 Tabelas hardcoded** — manter hardcoded em Java perpetua a dívida; externalizar exige decisão arquitetural (config service vs banco).
4. **🟡 Ordem CPF** — em PostgreSQL, sem `ORDER BY` explícito a ordem não é garantida; precisa ADR.
5. **🟡 Truncamento financeiro** — `BigDecimal` em Java com `RoundingMode.DOWN` é mandatório; documentar em EARS.

---

## 5. Recomendações

### 5.1 O que migrar primeiro

| Prioridade | Funcionalidade                | Justificativa                                                |
| ---------- | ----------------------------- | ------------------------------------------------------------ |
| 1          | Cálculo de benefício (CALCBENF) | Base de tudo; impacto financeiro direto.                    |
| 2          | Cálculo de descontos (CALCDSCT) | Depende de #1; regra do teto 30% é complexa.                |
| 3          | Batch de geração (BATCHPGT)     | Orquestra #1 e #2; crítico operacional.                     |
| 4          | Validação de cadastro (VALBENEF)| Protege integridade; CPF módulo 11 é padrão.                |
| 5          | Relatório de pagamentos (RELPGT)| Suporte a downstream e auditoria; menos complexo.           |

### 5.2 O que descartar

- **Terminal 3270 UI** → substituir por Next.js + shadcn/ui.
- **Relatório em flat file** → API REST + UI web.
- **Inicialização duplicada de tabelas** → migrar para tabela `social_program_parameters` no PostgreSQL.

### 5.3 O que evoluir

- **Cálculo de benefício** → adicionar simulação (what-if) e versionamento de parâmetros.
- **Descontos judiciais** → workflow de aprovação com anexo de processo.
- **Batch** → retry, circuit breaker, observabilidade real-time, idempotência por competência.
- **Validação cadastral** → incluir endereço, telefone, elegibilidade cruzada.
- **Auditoria** → event sourcing em tabela `audit_event` + retenção configurável.

---

## 6. Métricas do Estágio

| Métrica                          | Valor                                       |
| -------------------------------- | ------------------------------------------- |
| Programas analisados             | 4 / 15 em profundidade + 11 mapeados via dependências |
| DDMs mapeados                    | 4 / 4 (BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL, AUDITORIA) |
| Regras de negócio encontradas    | 16                                          |
| Regras críticas                  | 9                                           |
| Regras escondidas encontradas    | 6 / 10                                      |
| Easter eggs encontrados          | 1 / 3 (CPF `000...` de teste em VALBENEF)   |
| Termos no glossário              | 32                                          |
| Mistérios catalogados            | 10                                          |
| Tempo total gasto                | ~4 horas                                    |

---

## 7. Notas para o Próximo Estágio

- Antes de escrever EARS, resolver **MYS-002** (downstream) e **MYS-006** (auditoria) — bloqueadores.
- Toda EARS deve carregar `source_legacy:` apontando para `.NSN` com faixa de linhas (regra do gate).
- Considerar **ADR-001**: como externalizar tabelas de fatores regionais e faixas de renda (config table no PostgreSQL com versionamento).
- Considerar **ADR-002**: como garantir ordem CPF no batch moderno (índice + `ORDER BY` explícito + contrato com downstream).
- Considerar **ADR-003**: `BigDecimal` com `RoundingMode.DOWN` para preservar comportamento de truncamento legado.

---

## Definição de Pronto deste relatório

- [ ] Todas as seções acima preenchidas (sem placeholders).
- [ ] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [ ] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [ ] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

