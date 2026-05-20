<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

| ID      | Descrição                                                       | Onde Encontrado                                  | Impacto Potencial                  | Confiança |
| ------- | --------------------------------------------------------------- | ------------------------------------------------ | ---------------------------------- | --------- |
| MYS-001 | 27 UFs — posição 27 é DF ou duplicação?                         | VALBENEF.NSN#L62-L88, BATCHPGT.NSN#L138-L165     | Beneficiários em UF inválida rejeitados | MÉDIA |
| MYS-002 | "Sistemas downstream dependem desta ordenação por CPF"          | BATCHPGT.NSN#L196-L202                           | 🔴 Quebra de integração com TEF/SIAFI | ALTA   |
| MYS-003 | TIPO-PGTO D (Décimo) e T (Terceiro) — lógica completa onde?    | CALCBENF.NSN#L26, BATCHPGT.NSN#L26               | Cálculo de 13º incorreto           | ALTA   |
| MYS-004 | Truncamento manual `* 100 / 100` em vez de ROUND nativo         | CALCDSCT.NSN#L127-L133, CALCBENF.NSN#L207-L215   | Centavos divergentes acumulam      | ALTA   |
| MYS-005 | `#VLR-TEMP (N11)` integer em cálculo financeiro                 | CALCDSCT.NSN#L31, CALCBENF.NSN#L51               | Overflow se VLR > R$99.999,99      | MÉDIA  |
| MYS-006 | RELAUDIT.NSN — lógica de auditoria não analisada                | RELAUDIT.NSN (programa inteiro)                  | 🔴 Compliance CGU/TCU             | ALTA   |
| MYS-007 | `#DIAS-MES(2) = 29` sempre — fev em ano não-bissexto?           | VALBENEF.NSN#L98-L110                            | Datas inválidas aceitas            | MÉDIA  |
| MYS-008 | Tipo `C` (contrib) vs `I` (imposto) — diferença prática         | CALCDSCT.NSN#L157-L173                           | Cálculo de imposto incorreto       | MÉDIA  |
| MYS-009 | `NUM-DEPENDENTES` parcialmente aplicado em CALCBENF             | CALCBENF.NSN#L185-L200, BATCHPGT.NSN#L218-L233   | Benefício menor que deveria        | ALTA   |
| MYS-010 | Status `D` (DELETED) — soft delete ou hard delete?              | VALBENEF.NSN#L155-L160                           | FK em PAGAMENTO pode quebrar       | MÉDIA  |

## Detalhamento dos Mistérios

### MYS-001: 27 UFs — Posição 27 é DF ou Duplicação?

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L62-L88`
- **Trecho de código**:

```natural
1 #UF-TAB (A2/27)
...
MOVE 'AC' TO #UF-TAB(1)
MOVE 'AL' TO #UF-TAB(2)
...
MOVE 'TO' TO #UF-TAB(27)
```

- **O que esperávamos**: 26 estados + 1 DF = 27 UFs.
- **O que o código faz**: VALBENEF tem DF na posição 7. BATCHPGT tem `#TAB-REG(23) = DF`. **Ordem diferente entre programas.**
- **Hipótese do time**: DF está incluído; entretanto a ordem das UFs difere entre VALBENEF (alfabética) e BATCHPGT/CALCBENF (regional), o que é fonte de erro.
- **Risco se ignorarmos**: COD-REGIAO entre 1–25 mapeia para UFs diferentes em cada programa. Cálculo regional inconsistente.

---

### MYS-002: Sistemas Downstream Dependem da Ordenação por CPF

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L196-L202`
- **Trecho de código**:

```natural
* PROCESSAMENTO PRINCIPAL
* LEITURA EM ORDEM ALFABETICA POR CPF (OTIMIZACAO 1999)
* NOTA: SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO
READ BENEFICIARIO-V BY CPF
```

- **O que esperávamos**: Ordem do batch é detalhe de implementação interna.
- **O que o código faz**: Ordem CPF é **contrato implícito** com sistemas externos não identificados.
- **Hipótese do time**: TEF/SIAFI consome o lote e confere sequência; alternativa é relatório de auditoria que assume ordem.
- **Risco se ignorarmos**: 🔴 Em PostgreSQL, sem `ORDER BY cpf` explícito a ordem do `SELECT` não é garantida. Downstream pode rejeitar lote inteiro.

---

### MYS-003: Tipos D (Décimo) e T (Terceiro) — Lógica Completa Onde?

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L26`, `BATCHPGT.NSN#L26`
- **Trecho de código**:

```natural
2 TIPO-PGTO (A1)  /* N=NORMAL D=DECIMO T=TERCEIRO */
```

- **O que esperávamos**: 3 tipos de pagamento implementados.
- **O que o código faz**: Apenas tipo `N` (NORMAL) e fragmento de tipo `D` (13º em dezembro) visíveis. Tipo `T` (TERCEIRO) sem código encontrado.
- **Hipótese do time**: Lógica de tipo T pode estar em `CALCCORR.NSN` (correção/retroativo) — não lido em profundidade.
- **Risco se ignorarmos**: ALTO — beneficiários com pagamento tipo `T` ganham/perdem em dezembro.

---

### MYS-004: Truncamento Manual em Vez de ROUND/TRUNC Nativo

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L127-L133`
- **Trecho de código**:

```natural
COMPUTE #VLR-MAX-DSCT = #VLR-BRUTO * 0.30
* TRUNCAR
COMPUTE #VLR-TEMP = #VLR-MAX-DSCT * 100
COMPUTE #VLR-MAX-DSCT = #VLR-TEMP / 100
```

- **O que esperávamos**: Uso de `ROUND` ou ajuste por `*.N` (truncamento nativo Natural).
- **O que o código faz**: Trunca manualmente via multiplicação/divisão por 100 e atribuição a campo integer.
- **Hipótese do time**: Intencional — truncar nunca excede o teto de 30%; arredondar poderia.
- **Risco se ignorarmos**: Java com `Math.round()` diverge do legado em centavos. Acumula em ~500k pagamentos/mês.

---

### MYS-005: `#VLR-TEMP (N11)` Integer em Cálculo Financeiro

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L31`
- **Trecho de código**:

```natural
1 #VLR-TEMP (N11)
```

- **O que esperávamos**: Campo N9.2 (com casas decimais) para valor financeiro.
- **O que o código faz**: Integer N11 — usado como buffer para truncamento (`X * 100`).
- **Hipótese do time**: Padrão de truncamento; N11 suporta até R$99.999.999,99 após `* 100`.
- **Risco se ignorarmos**: Se VLR-BRUTO crescer (inflação/correção), pode overflow silencioso.

---

### MYS-006: RELAUDIT.NSN — Onde Está a Lógica de Auditoria?

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN` (não analisado em profundidade)
- **Trecho de código**: pendente de leitura.
- **O que esperávamos**: Audit log de cada mudança em PAGAMENTO/BENEFICIARIO (quem, quando, antes/depois).
- **O que o código faz**: Desconhecido — programa existe mas não foi lido em profundidade. DDM `AUDITORIA.ddm` está presente.
- **Hipótese do time**: RELAUDIT é relatório sobre tabela AUDITORIA preenchida por triggers ou pelos programas de cadastro.
- **Risco se ignorarmos**: 🔴 Compliance CGU/TCU — sistema sem rastreabilidade pode ser rejeitado em auditoria.

---

### MYS-007: Fevereiro com 29 Dias Hardcoded

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L98-L110`
- **Trecho de código**:

```natural
MOVE 31 TO #DIAS-MES(1)
MOVE 29 TO #DIAS-MES(2)   /* CONSIDERA BISSEXTO */
MOVE 31 TO #DIAS-MES(3)
```

- **O que esperávamos**: Cálculo de bissexto baseado no ano.
- **O que o código faz**: Sempre permite 29 de fevereiro, mesmo em ano não-bissexto.
- **Hipótese do time**: Simplificação intencional — beneficiários com DT-NASC=29/02 em ano não-bissexto são aceitos.
- **Risco se ignorarmos**: BAIXO — dados existentes continuam válidos; nova lógica pode rejeitar.

---

### MYS-008: Tipo `C` (Contrib) vs `I` (Imposto) — Diferença Prática

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L157-L173`
- **Trecho de código**:

```natural
VALUE 'I'
  COMPUTE #VLR-DSCT-ITEM = #VLR-BRUTO * (BENEFICIARIO-V.PCT-DSCT(#IDX) / 100)
  ADD #VLR-DSCT-ITEM TO #VLR-TOTAL-DSCT
```

- **O que esperávamos**: Distinção clara entre contribuição social e imposto.
- **O que o código faz**: Tipo C usa alíquota da tabela `#ALIQ-CONTRIB`; tipo I usa percentual cadastrado por beneficiário.
- **Hipótese do time**: C = contribuição obrigatória padronizada; I = retenção variável (acordo individual ou ordem fiscal).
- **Risco se ignorarmos**: Cálculo de imposto retido divergente; potencial passivo fiscal.

---

### MYS-009: NUM-DEPENDENTES Parcialmente Aplicado no Cálculo

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L185-L200`
- **Trecho de código**:

```natural
IF #NUM-DEP = 0
  MOVE 1.0000 TO #FATOR-FAM
ELSE
  IF #NUM-DEP <= 2
    COMPUTE #FATOR-FAM = 1.0000 + (#NUM-DEP * 0.0500)
```

- **O que esperávamos**: Dependentes aplicados consistentemente em todo cálculo de benefício.
- **O que o código faz**: CALCBENF aplica fator familiar. BATCHPGT replica a lógica. Mas CADDEPEND não foi analisado em profundidade — pode haver regra adicional.
- **Hipótese do time**: Fator familiar implementado, mas pode haver benefício extra por dependente menor de idade (CADDEPEND).
- **Risco se ignorarmos**: ALTO — benefício menor que devido se ignorarmos lógica adicional em CADDEPEND.

---

### MYS-010: Status `D` (DELETED) — Soft Delete ou Hard Delete?

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L155-L160`
- **Trecho de código**:

```natural
IF #STATUS NE 'A' AND #STATUS NE 'S' AND #STATUS NE 'C'
    AND #STATUS NE 'I' AND #STATUS NE 'D'
  MOVE 'STATUS INVALIDO' TO #MSG-ERRO(#QTD-ERROS)
```

- **O que esperávamos**: Hard delete remove o registro.
- **O que o código faz**: Status `D` é aceito pela validação — ou seja, soft delete (registro permanece com status `D`).
- **Hipótese do time**: Soft delete intencional para preservar histórico de pagamentos (FK).
- **Risco se ignorarmos**: MÉDIO — em Java/PostgreSQL, definir se `BENEFICIARIO.status='D'` é filtrado por padrão em queries ou requer `WHERE status != 'D'`.

---

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Encontrados:

1. [x] **Easter Egg 1:** CPFs com todos os dígitos zerados (`000.000.000-00`, `111.111.111-11`, etc.) são tratados como **CPFs de teste do governo** em `VALBENEF.NSN#L218-L223`.
2. [ ] Easter Egg 2: não encontrado ainda.
3. [ ] Easter Egg 3: não encontrado ainda.

## Resumo

- Total de mistérios encontrados: **10**
- Confiança alta: **5** (MYS-002, MYS-003, MYS-004, MYS-006, MYS-009)
- Confiança média: **5** (MYS-001, MYS-005, MYS-007, MYS-008, MYS-010)
- Confiança baixa: **0**
- Easter eggs encontrados: **1 / 3**
- **Bloqueadores para Estágio 2**: MYS-002, MYS-003, MYS-006, MYS-009

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

