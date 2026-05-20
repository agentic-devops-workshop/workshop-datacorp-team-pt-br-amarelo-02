<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: PT-BR Amarelo 02
**Data**: 2026-05-20
**Edição**: Workshop DataCorp — Modernização SIFAP
**Par 1 (Product Owner) responsável**: Par 1 (PO + RE)

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade            | Decisão  | Justificativa | Regra de Negócio (BR-XXX) | Prioridade |
| --- | ------------------------- | -------- | ------------- | ------------------------- | ---------- |
| 1   | Cadastro de Beneficiários | Migrar   | Entidade central do sistema; fluxo CRUD essencial | BR-007, BR-008, BR-009, BR-010 | Alta |
| 2   | Consulta de Beneficiários | Migrar   | Necessário para operação diária e auditoria | — | Média |
| 3   | Geração de Ciclo Mensal   | Migrar   | Fluxo crítico financeiro; 1º dia útil | BR-005, BR-006, BR-013, BR-014, BR-016 | Alta |
| 4   | Processamento Batch       | Migrar   | Orquestração do ciclo; ordenação CPF para downstream | BR-013 | Alta |
| 5   | Cálculo de Benefícios     | Migrar   | Cálculo financeiro com fator regional e renda | BR-011, BR-012 | Alta |
| 6   | Cálculo de Descontos      | Migrar   | Teto 30%, judicial sem teto, faixas de contribuição | BR-001, BR-002, BR-003, BR-004 | Alta |
| 7   | Validação de CPF          | Migrar   | Proteção de integridade; módulo 11 obrigatório | BR-007 | Alta |
| 8   | Relatórios                | Evoluir  | Substituir flat file por API REST + UI web | BR-015 | Média |
| 9   | Auditoria                 | Evoluir  | Adicionar auditoria em tempo real (event-driven) | — | Alta |
| 10  | Gestão de Usuários        | Evoluir  | OAuth2/JWT em vez de sessão terminal | — | Alta |
| 11  | Interface Terminal 3270    | Descartar | Obsoleta; substituída por Next.js | — | — |
| 12  | Relatórios Flat File      | Descartar | Substituídos por API + dashboard web | — | — |

> Adicione linhas para cada funcionalidade identificada no `discovery-report.md` do Estágio 1.

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o SIFAP 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
| --- | ------------------- | ------------- | ---------- | ------------ |
| N1  | Autenticação OAuth2/JWT | Legado usava sessão terminal; API moderna exige token stateless (REQ-SEC-001) | Alta | Média |
| N2  | Auditoria em tempo real (event-driven) | Legado só gerava relatório posterior; regulatório exige rastreabilidade (REQ-AUD-001) | Alta | Média |
| N3  | Dashboard de acompanhamento | UX moderna para operador; substitui telas 3270 | Média | Baixa |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 7          | 58%        |
| Descartar | 2          | 17%        |
| Evoluir   | 3          | 25%        |
| **Total** | **12**     | 100%       |

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco | Probabilidade | Impacto | Mitigação |
| ----- | ------------- | ------- | --------- |
| Contrato downstream desconhecido (MYS-002) impacta REQ-PAY-004 | Alta | Alto | Manter ORDER BY CPF ASC explícito; documentar contrato assumido |
| Tipos D/T de pagamento sem lógica completa (BR-015) | Média | Médio | Implementar apenas tipo N no Estágio 3; D/T ficam como backlog |
| Tabelas hardcoded podem divergir entre módulos | Média | Alto | Externalizar em tabela PostgreSQL desde o início (ADR-002) |
| Truncamento financeiro diverge de arredondamento padrão | Baixa | Alto | Usar BigDecimal com RoundingMode.DOWN em todo cálculo |

## Aprovação

- [x] Par 1 (Product Owner) aprovou as decisões de escopo
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica
- [x] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [x] Time concordou com as prioridades

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

