<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

| ID | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
|---|---|---|---|---|---|
| BR-001 | Teto de descontos não-judiciais em 30% | CALCDSCT.NSN#L142-L148 | PAGAMENTO.VLR-BRUTO, PAGAMENTO.VLR-DESCONTO | **CRÍTICO** | Tipos C,I,S,P,A têm teto. J (judicial) é isento. |
| BR-002 | Desconto judicial sem teto | CALCDSCT.NSN#L156-L160 | BENEFICIARIO.DESCONTOS.TIPO-DSCT='J' | **CRÍTICO** | Inclusão 2007. Ordem judicial pode ser até 100%+. |
| BR-003 | Desconto sindical fixo em 1% | CALCDSCT.NSN#L165 | PAGAMENTO.VLR-BRUTO | **MÉDIO** | Percentual hardcoded (0.01). Não paramétrico. |
| BR-004 | Contribuição social por faixa de valor | CALCDSCT.NSN#L45-L60 | PAGAMENTO.VLR-BRUTO | **CRÍTICO** | Tabela: até R$500→3%, R$1000→5%, R$2000→7%, >R$2000→9%. |
| BR-005 | Geração mensal de pagamentos ACTIVE | BATCHPGT.NSN#L88-L142 | BENEFICIARIO.STATUS, BENEFICIARIO.CPF | **CRÍTICO** | 1º dia útil. Só status='A'. Ordem por CPF obrigatória. |
| BR-006 | Status inicial de pagamento | BATCHPGT.NSN#L156 | PAGAMENTO.STATUS-PGTO | **ALTO** | Todo novo pagamento inicia com status='P' (PENDING). |
| BR-007 | Validação de CPF com módulo 11 | VALBENEF.NSN#L114-L135 | BENEFICIARIO.CPF | **CRÍTICO** | Rejeita 000.000.000-00 e dígito verificador inválido. |
| BR-008 | Validação de data de nascimento | VALBENEF.NSN#L143-L160 | BENEFICIARIO.DT-NASCIMENTO | **MÉDIO** | Valida formato AAAAMMDD. Fevereiro com 29 dias (sempre bissexto?). |
| BR-009 | Validação de nome (nome + sobrenome) | VALBENEF.NSN#L162-L180 | BENEFICIARIO.NOME | **MÉDIO** | Deve conter espaço (separa nome e sobrenome). |
| BR-010 | Validação de UF | VALBENEF.NSN#L76-L102 | BENEFICIARIO.UF | **MÉDIO** | Tabela de 27 UFs (AC, AL, ..., TO). Rejeita UF fora da tabela. |
| BR-011 | Fator regional por UF | BATCHPGT.NSN#L138-L165, CALCBENF | BENEFICIARIO.COD-REGIAO | **ALTO** | 27 valores hardcoded (1.35 AC até 1.00 RO/RR). Ajusta VLR-BASE. |
| BR-012 | Fator de renda por faixa | BATCHPGT.NSN#L167-L177 | BENEFICIARIO.RENDA-FAMILIAR | **ALTO** | 5 faixas: <R$300→1.00, R$300-600→0.85, ..., >R$1500→0.40. Reduz benefício. |
| BR-013 | Processamento ordenado por CPF | BATCHPGT.NSN#L196-L202 | BENEFICIARIO.CPF | **CRÍTICO** | "SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO" — quais? |
| BR-014 | Evitar duplicação na mesma competência | BATCHPGT.NSN#L207-L211 | BENEFICIARIO.CPF, PAGAMENTO.COMPETENCIA | **CRÍTICO** | Compara CPF anterior (#CPF-ANT) para não duplicar. |
| BR-015 | Tipos de pagamento (N, D, T) | CALCBENF.NSN#L26, BATCHPGT.NSN#L26 | PAGAMENTO.TIPO-PGTO | **ALTO** | N=NORMAL, D=DÉCIMO(?), T=TERCEIRO(?) — lógica não vista. |
| BR-016 | Data de corte = último dia mês anterior | BATCHPGT.NSN#L180-L190 | BENEFICIARIO.STATUS + data | **CRÍTICO** | Beneficiário deve estar ACTIVE no último dia mês anterior para receber. |

> 16 regras catalogadas a partir da leitura inicial de 4 programas (BATCHPGT, CALCBENF, CALCDSCT, VALBENEF). Os 11 programas restantes podem revelar regras adicionais (estimativa: 24-30 ao final do mapeamento completo).

## Regras por Categoria

### Cálculos Financeiros

<!-- Liste aqui as regras relacionadas a cálculos de valores, benefícios, etc. -->

### Validações de Status

<!-- Liste aqui as regras de transição de status (A, S, C, I, D) -->

### Regras de Autorização

<!-- Liste aqui as regras de quem pode fazer o quê -->

### Regras de Negócio Temporais

<!-- Liste aqui regras com prazos, datas-limite, períodos -->

## Resumo Estatístico

- Total de regras encontradas: **16**
- Regras críticas: **9** (BR-001, BR-002, BR-004, BR-005, BR-007, BR-013, BR-014, BR-016, ordem CPF)
- Regras com duplicação: **0** confirmadas (tabelas TAB-REG e FAIXA-RENDA replicadas entre BATCHPGT e CALCBENF — a investigar como duplicação)
- Regras sem documentação (escondidas): **6** identificadas até agora (BR-003 sindical 1% hardcoded, BR-008 fev=29, BR-011 fatores regionais, BR-012 faixas de renda, BR-013 ordem CPF + downstream, BR-015 tipos D/T parciais)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

