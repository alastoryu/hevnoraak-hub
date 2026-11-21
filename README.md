# hevnoraak-hub

Hub central de consultas externas do **Hevnoraak**. Uma única instalação atende todos os clientes: concentra integrações com órgãos e terceiros, certificados e cache.

## Endpoints (v1)

| Endpoint | Fonte | Observação |
| --- | --- | --- |
| `GET /api/v1/cnpj/{cnpj}` | BrasilAPI | Aceita CNPJ numérico e alfanumérico (julho/2026), com ou sem pontos e hífen |
| `GET /api/v1/cep/{cep}` | ViaCEP | Com ou sem hífen |

Próximos: Junta Comercial, Prefeitura, REDESIM.

## Contrato de resposta

Toda consulta devolve o mesmo envelope:

```json
{
  "status": "ENCONTRADO | NAO_ENCONTRADO",
  "dados": { "...": "DTO normalizado da consulta" },
  "fonte": "BrasilAPI",
  "consultadoEm": "2026-11-21T02:40:00Z",
  "evidencia": "resposta original da fonte, para anexar à auditoria do caso"
}
```

| HTTP | Quando | O que o orquestrador faz |
| --- | --- | --- |
| 200 | Consulta feita (encontrado ou não) | Segue o fluxo |
| 400 | Documento inválido (dígito verificador, formato) | Devolve para correção |
| 503 + `tentarNovamente: true` | Fonte fora do ar ou lenta (timeout 10 s) | Job assíncrono com retry |

Resultados encontrados ficam em cache por 24 h (Caffeine).

## Segurança

Cada instalação do Hevnoraak é um client confidencial no realm `hevnoraak-hub` do Keycloak e obtém token por **client credentials**. O Hub exige o escopo `hub.consulta` (`SCOPE_hub.consulta`).

## Rodando localmente

```bash
./mvnw spring-boot:run      # http://localhost:8090, Swagger em /swagger-ui.html
```

| Variável | Padrão |
| --- | --- |
| `KEYCLOAK_ISSUER` | `http://localhost:8180/realms/hevnoraak-hub` |
| `BRASILAPI_URL` | `https://brasilapi.com.br` |
| `VIACEP_URL` | `https://viacep.com.br` |

## Testes

```bash
./mvnw verify
```

Os testes de integração sobem uma fonte falsa local (BrasilAPI e ViaCEP), sem acesso à internet.
