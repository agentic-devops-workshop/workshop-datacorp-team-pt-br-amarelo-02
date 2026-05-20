# ADR-001: Adoção de Monólito Modular

## Status

Aceita

## Data

2026-05-20

## Contexto

O SIFAP legado é um sistema monolítico em Natural/Adabas com 15 programas altamente acoplados. A modernização precisa preservar 16+ regras de negócio críticas (financeiras) enquanto permite evolução independente de módulos. O time tem 3 horas para implementar no Estágio 3, o que inviabiliza a complexidade operacional de microsserviços.

## Opções Consideradas

### Opção 1: Monólito Modular (package-by-feature)

- **Prós:** Deploy único simplifica operações; fronteiras internas claras permitem futura extração; menor overhead de rede; transações locais entre módulos; viável no tempo do workshop
- **Contras:** Exige disciplina para não violar fronteiras; risco de acoplamento se não houver enforcement

### Opção 2: Microsserviços desde o início

- **Prós:** Independência total de deploy; escalabilidade granular
- **Contras:** Complexidade operacional alta (service mesh, distributed tracing, eventual consistency); impossível implementar em 3h; overhead de rede em cálculos financeiros que exigem consistência forte

### Opção 3: Monólito tradicional (package-by-layer)

- **Prós:** Mais simples de começar
- **Contras:** Reproduz o acoplamento do legado; dificulta evolução futura; sem fronteiras claras entre domínios

## Decisão

**Adotar Monólito Modular com 4 bounded contexts**: Cadastro (`beneficiary`), Pagamento (`payment`), Elegibilidade (`eligibility`) e Auditoria (`audit`). Cada contexto é um módulo Maven com seu próprio pacote de domínio, application e infrastructure. Comunicação cross-module via interfaces Java (não chamadas HTTP).

## Consequências

### Positivas

- Deploy e operação simples (um JAR, um banco)
- Transações ACID locais para cálculos financeiros críticos (BR-001 a BR-005)
- Fronteiras claras permitem extração futura para microsserviços via Strangler Fig
- Implementável no tempo disponível do Estágio 3

### Negativas

- Exige revisão de código para evitar violação de fronteiras (mitigação: ArchUnit tests)
- Escala é uniforme — todos os módulos escalam juntos (aceitável para o volume do SIFAP)

## Requisitos Relacionados

- REQ-PAY-001 a REQ-PAY-011 (módulo payment)
- REQ-ELG-001 a REQ-ELG-004 (módulo eligibility)
- REQ-AUD-001 (módulo audit)
- REQ-SEC-001 (cross-cutting)
