# Security Model

> Security notes for Gadget Room Backend. This document focuses on the runtime behavior that reviewers should verify
> before promoting a release.

## Authentication

- The API uses stateless JWT authentication.
- Access and refresh tokens are issued through `/api/auth/**`.
- Logout revokes the current token through `/api/v1/logout`.
- Revoked tokens are stored in Redis until expiration.
- Passwords are hashed with BCrypt.
- Password reset uses a dedicated reset-token flow.

## Authorization

| Endpoint group | Access model |
| --- | --- |
| `/api/auth/**` | Public |
| `/api/v1/logout` | Authenticated |
| `/api/v1/filter/**` | Public |
| `GET /api/v1/phones/**` | Public |
| Phone writes | Admin |
| `/api/v1/images/**` | Public read |
| `/api/v1/delivery/**` | Public |
| `/api/v1/orders/**` | Authenticated, except configured admin-only legacy creation |
| `/api/v1/me/**` | Authenticated |
| `/api/v1/users/me/**` | Authenticated |
| `/api/v1/users/**` | Admin |
| `/api/v1/admin/**` | Admin |
| `/actuator/health/**` | Public |
| Other actuator endpoints | Admin and explicitly exposed only |

## Production Exposure

- Swagger UI is disabled in `prod`.
- `/v3/api-docs` is disabled in `prod`.
- `/api/v1/test-data/**` is not registered in `prod`.
- Public health probes stay available for infrastructure checks.
- Health details are not exposed to anonymous callers.

## Rate Limiting

Auth-sensitive endpoints are rate-limited per client and endpoint:

- login;
- refresh token;
- forgot password;
- reset password.

Configuration:

```text
AUTH_RATE_LIMIT_REQUESTS_PER_WINDOW=10
AUTH_RATE_LIMIT_WINDOW=1m
```

## Data Protection

- Secrets are injected through environment variables.
- Local `.env` and `test.env` files are ignored by Git.
- Product image binaries are stored in MinIO, not PostgreSQL.
- PostgreSQL stores business metadata and references to object storage keys.
- CORS is environment-driven and should be narrowed for deployed environments.

## Reviewer Checklist

- [ ] Production profile disables Swagger and test-data endpoints.
- [ ] Admin-only routes stay under `/api/v1/admin/**` or explicit admin matchers.
- [ ] New public endpoints are intentional and documented.
- [ ] New auth endpoints are covered by rate limits where appropriate.
- [ ] New secrets are added to env examples, never committed as real values.
- [ ] Webhook validation is tested for the selected payment provider.
