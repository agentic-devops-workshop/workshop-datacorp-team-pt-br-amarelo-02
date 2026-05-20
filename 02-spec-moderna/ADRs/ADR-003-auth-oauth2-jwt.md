# ADR-003: Autenticação e Autorização com OAuth2/JWT

## Status

Aceita

## Data

2026-05-20

## Contexto

O SIFAP legado usava autenticação por sessão de terminal 3270 — sem API exposta, sem tokens, sem controle granular de permissões. O sistema moderno expõe REST API consumida por frontend Next.js e potencialmente por sistemas externos. Dados sensíveis (CPF, valores de benefício) exigem controle de acesso rigoroso. A LGPD e normas do TCU requerem rastreabilidade de quem acessou o quê.

## Opções Consideradas

### Opção 1: Sessão stateful com cookie

- **Prós:** Simples de implementar; familiar
- **Contras:** Não escala horizontalmente sem sticky sessions; não funciona para API-first; difícil de integrar com SPA

### Opção 2: OAuth2 com JWT (Spring Security)

- **Prós:** Padrão da indústria; stateless; suporta múltiplos clientes (web, mobile, service-to-service); integra com Azure AD / Entra ID; tokens carregam claims de permissão
- **Contras:** Complexidade inicial de configuração; necessidade de gestão de refresh tokens

### Opção 3: API Key simples

- **Prós:** Trivial de implementar
- **Contras:** Sem rotação automatizada; sem claims; sem suporte a múltiplos perfis; inseguro para sistema financeiro

## Decisão

**Adotar OAuth2 com JWT via Spring Security 6.** Perfis de acesso:

| Perfil | Permissões | Origem legada |
|--------|-----------|---------------|
| OPERATOR | CRUD beneficiários, gerar ciclo | Operador (CADBENEF, BATCHPGT) |
| AUDITOR | Leitura de auditoria e relatórios | Auditor (RELAUDIT, RELPGT) |
| ADMIN | Gestão de usuários e configurações | DBA/Admin (controle externo) |

Tokens JWT contêm claims: `sub`, `roles`, `iat`, `exp`. Access token com TTL de 15 min, refresh token com TTL de 8h.

## Consequências

### Positivas

- API stateless — escala horizontalmente sem estado de sessão
- Claims no token permitem autorização sem consulta ao banco a cada request
- Integração futura com Azure Entra ID via OIDC (Managed Identity para service-to-service)
- Auditoria nativa: `user_id` do token vai para `audit_event` (REQ-AUD-001)

### Negativas

- Configuração inicial de Spring Security OAuth2 Resource Server mais complexa (mitigação: starter + configuração declarativa)
- Revogação de token exige blacklist ou TTL curto (mitigação: access token de 15 min)

## Requisitos Relacionados

- REQ-SEC-001 (autenticação JWT obrigatória)
- REQ-AUD-001 (user_id no evento de auditoria vem do token)
