# ADR-002: Migração de Dados Adabas para PostgreSQL

## Status

Aceita

## Data

2026-05-20

## Contexto

O SIFAP legado armazena dados em 4 arquivos Adabas (BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL, AUDITORIA) com campos MU (multi-valor) e PE (grupos periódicos) que não mapeiam diretamente para modelo relacional. A migração precisa preservar semântica dos dados, incluindo a ordenação por CPF que sistemas downstream dependem (BR-013/MYS-002).

## Opções Consideradas

### Opção 1: Big Bang — migrar tudo de uma vez

- **Prós:** Corte único, sem período de dupla manutenção
- **Contras:** Risco altíssimo para sistema financeiro crítico; rollback difícil; requer janela de indisponibilidade

### Opção 2: Strangler Fig com coexistência temporária

- **Prós:** Migração incremental por módulo; rollback possível por funcionalidade; menor risco operacional; validação progressiva
- **Contras:** Período de dupla manutenção; necessidade de sync temporário entre Adabas e PostgreSQL

### Opção 3: CQRS com event sourcing

- **Prós:** Auditoria nativa; replay de eventos
- **Contras:** Complexidade desproporcional para o escopo; curva de aprendizado alta; impossível no tempo do workshop

## Decisão

**Adotar Strangler Fig com PostgreSQL 16** como banco-alvo. Mapeamento:

| Adabas | PostgreSQL |
|--------|-----------|
| Campos A (alpha) | `VARCHAR` / `TEXT` |
| Campos N (numeric) | `NUMERIC` / `BIGINT` |
| Campos P (packed) | `NUMERIC(precision, scale)` — financeiro usa `NUMERIC(15,2)` |
| Campos D (date) | `DATE` |
| MU (multi-valor) | `@ElementCollection` ou coluna `JSONB` |
| PE (grupo periódico) | Tabela filha com `@OneToMany` |
| Super-descriptors | Índice composto (`CREATE INDEX`) |

Aritmética financeira usa `BigDecimal` com `RoundingMode.DOWN` para preservar comportamento de truncamento do legado (BR-001, MYS-004).

## Consequências

### Positivas

- PostgreSQL 16 oferece JSONB para dados semi-estruturados (MU/PE) sem perda de query
- Constraint `UNIQUE(cpf, competencia)` resolve BR-014 em nível de banco
- `ORDER BY cpf ASC` explícito preserva BR-013
- Flyway para versionamento de migrations

### Negativas

- Período de coexistência requer atenção a consistency (mitigação: módulo de sync read-only do legado)
- Campos PE com muitos registros podem exigir paginação (mitigação: limite de registros por PE group)

## Requisitos Relacionados

- REQ-PAY-003 (unique constraint)
- REQ-PAY-004 (ordenação CPF)
- REQ-PAY-006, REQ-PAY-007 (tabelas parametrizáveis no banco)
- REQ-PAY-008 a REQ-PAY-010 (cálculos financeiros com BigDecimal)
