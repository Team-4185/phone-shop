# API Documentation

> Local OpenAPI documentation for Gadget Room Backend. The screenshots below are captured from the `dev` profile and
> show the full Swagger UI in readable sections.

## Contract

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Saved development contract: [docs/api/openapi-dev.json](api/openapi-dev.json)

Swagger and OpenAPI JSON are intentionally disabled in the `prod` profile.

## Endpoint Groups

| Area | Base path | Notes |
| --- | --- | --- |
| Authentication | `/api/auth/**` | Registration, login, refresh token and password reset. |
| Logout | `/api/v1/logout` | Revokes the current token. |
| Catalog | `/api/v1/phones/**`, `/api/v1/filter/**` | Public catalog reads, admin catalog writes. |
| Customer | `/api/v1/users/**`, `/api/v1/me/**` | Profile, cart and favorites. |
| Reviews | `/api/v1/phones/{phoneId}/reviews/**` | Public reads, authenticated writes. |
| Orders | `/api/v1/orders/**` | Checkout, history, details and cancellation. |
| Payments | `/api/v1/payments/webhooks/**` | Provider callbacks. |
| Admin | `/api/v1/admin/**` | Products, orders, customers and dashboard analytics. |
| Images | `/api/v1/images/**` | Public image serving and metadata. |
| Delivery | `/api/v1/delivery/**` | Delivery providers and pickup points. |

## Swagger UI

![Swagger UI 01](assets/swagger/swagger-01.png)

![Swagger UI 02](assets/swagger/swagger-02.png)

![Swagger UI 03](assets/swagger/swagger-03.png)

![Swagger UI 04](assets/swagger/swagger-04.png)

![Swagger UI 05](assets/swagger/swagger-05.png)

![Swagger UI 06](assets/swagger/swagger-06.png)

![Swagger UI 07](assets/swagger/swagger-07.png)

![Swagger UI 08](assets/swagger/swagger-08.png)

![Swagger UI 09](assets/swagger/swagger-09.png)

![Swagger UI 10](assets/swagger/swagger-10.png)

![Swagger UI 11](assets/swagger/swagger-11.png)

![Swagger UI 12](assets/swagger/swagger-12.png)

![Swagger UI 13](assets/swagger/swagger-13.png)

![Swagger UI 14](assets/swagger/swagger-14.png)

![Swagger UI 15](assets/swagger/swagger-15.png)

![Swagger UI 16](assets/swagger/swagger-16.png)

![Swagger UI 17](assets/swagger/swagger-17.png)

![Swagger UI 18](assets/swagger/swagger-18.png)
