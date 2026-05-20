<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


# Glossário de Termos — SIFAP Legado (PREENCHIDO)

---

## A - Termos por Letra

### Abono Natalino (13º Salário)
**Campo legado:** `PAGAMENTO.VLR-ABONO`  
**Benefício adicional gerado em dezembro, calculado como 1/12 do valor bruto anual ou com fórmula diferenciada conforme programa social.**

### Arquivo
Nomenclatura: Arquivo 150 (BENEFICIARIO), 155 (PAGAMENTO auxiliar), 160 (PAGAMENTO principal)

### Atualizado
Quando um registro Adabas é modificado por `UPDATE` dentro de `END TRANSACTION`.

---

## B - Termos por Letra

### Batch
Processamento em lote, sem interação do usuário. No SIFAP, executa no **1º dia útil do mês**.

### Beneficiário
Pessoa física inscrita no SIFAP que recebe benefício social. Identificado por CPF (chave primária).

---

## C - Termos por Letra

### Competência
Campo `PAGAMENTO.COMPETENCIA` (N6) — formato AAAAMM. Mês de referência do pagamento.

### Contribuição Social
Desconto obrigatório que varia por faixa de valor (3%, 5%, 7%, 9% conforme faixa de bruto).

### CPF
Cadastro de Pessoas Físicas (N11). Validado por algoritmo módulo 11.

---

## D - Termos por Letra

### Data de Corte
Última data do mês anterior usada para determinar quem recebe pagamento neste ciclo.

### Desconto
Valores retidos do pagamento bruto (7 tipos: C, I, J, S, P, A). Máximo 30%, exceto judicial (J).

### DDM
Adabas Data Definition Module. Schema que define estrutura de dados em Adabas.

---

## E - Termos por Letra

### Escape
Palavra-chave Natural para sair de loop: `ESCAPE TOP` (loop interno), `ESCAPE BOTTOM` (próx. linha após loop).

---

## F - Termos por Letra

### Fator Regional
Multiplicador (1.00–1.40) que ajusta valor de benefício conforme UF (27 valores hardcoded).

### Faixa de Renda
5 faixas de renda familiar (até R$300, R$600, R$1000, R$1500, R$9999+) com fatores de redução (1.00–0.40).

### FIND
Comando Adabas para buscar registros: `FIND <view> WITH <campo> = <valor>`.

---

## M - Termos por Letra

### Módulo 11
Algoritmo de validação de CPF (pesos 10–2, 2 dígitos verificadores).

---

## P - Termos por Letra

### Pagamento
Registro de benefício a pagar a um beneficiário em uma competência. Status inicial: PENDING.

### PE (Periodic Group)
Estrutura Adabas que permite múltiplas ocorrências (equivalente a ARRAY).

### Programa Social
Tipo de benefício (tem VLR-BASE, FATOR-REAJUSTE, STATUS-PROG, RENDA-MAX).

---

## R - Termos por Letra

### READ
Comando Adabas para ler sequencialmente: `READ <view> BY <campo> [ASC|DESC]`.

---

## S - Termos por Letra

### Status (Beneficiário)
A=ACTIVE, S=SUSPENDED, C=CANCELLED, I=INACTIVE, D=DELETED (?)

### Status (Pagamento)
P=PENDING (inicial), A=APPROVED(?), X=CANCELLED(?), D=DELIVERED(?)

---

## T - Termos por Letra

### Truncar
Remover casas decimais sem arredondar. Implementado: `COMPUTE #VAL-TEMP = #VAL * 100; COMPUTE #VAL = #VAL-TEMP / 100`.

### TIPO-PGTO
Tipo de pagamento: N=NORMAL, D=DÉCIMO, T=TERCEIRO.

---

## V - Termos por Letra

### Validação
Rotina VALBENEF verifica: CPF (módulo 11), data nascimento, nome (espaço), UF (tabela de 27).

### VIEW
Projeção de arquivo Adabas (subset de colunas para otimização).

---

## Termos Adicionais (Natural/Adabas)

### NIS
Número de Identificação Social. Em alguns programas usado como chave alternativa ao CPF.

### ISN
Internal Sequence Number — identificador físico Adabas do registro. Usado em `GET <view> <isn>`.

### MU (Multi-Value)
Campo Adabas que aceita múltiplos valores na mesma linha (ex.: `MSG-ERRO(20)` em VALBENEF).

### COMPRESS
Comando Natural que concatena strings: `COMPRESS A B INTO C LEAVING NO SPACE`.

### END TRANSACTION (ET)
Commit Adabas. Persiste mudanças desde o último ET. Usado em BATCHPGT após STORE PAGAMENTO.

### *DATN
Variável de sistema Natural — data atual em formato AAAAMMDD.

### COD-REGIAO
Código numérico (1–27) que mapeia para uma UF na tabela `#TAB-REG` em BATCHPGT/CALCBENF.

### COD-PROGRAMA
Identificador do programa social (chave para PROGRAMA-SOCIAL.ddm).

## Observações

- **Prefixos de programa identificados:** `CAD` (cadastro), `VAL` (validação), `CALC` (cálculo), `BATCH` (lote), `REL` (relatório), `CONS` (consulta).
- **Sufixos de variáveis Natural:** `#VLR-*` (valor), `#TAB-*` (tabela), `#IDX/#I/#K` (índices), `#CPF-*` (controle de CPF), `#QTD-*` (contadores).
- **Convenção DDM:** nomes em MAIÚSCULAS com hífen (`VLR-BRUTO`, `DT-NASCIMENTO`); PE groups em plural (`DESCONTOS`).
- **Total de termos catalogados:** 32 (acima do mínimo de 30 do gate).

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

