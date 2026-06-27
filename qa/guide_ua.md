# QA Guide

> Цей гайд пояснює, як локально запустити Gadget Room Backend і перевірити API без роботи з production-даними.

## Вимоги

- Docker Desktop встановлений і запущений.
- Git встановлений.
- Файл `test.env` лежить у корені проєкту або в папці `qa`.
- Порти з `test.env` вільні на вашому комп'ютері.

## Запуск застосунку

З папки `qa` запустіть один із скриптів:

```bash
./setup.sh
```

```bat
setup.bat
```

Скрипт піднімає backend і всі залежності через Docker Compose.

## Очищення локального середовища

Використовуйте reset, коли змінилися міграції, seed-дані застаріли або контейнери не стартують через старі volumes:

```bash
./setup.sh reset
```

```bat
setup.bat reset
```

Команда видаляє Docker volumes тільки для compose-проєкту `gadget-room-backend` і запускає чисте середовище.

## Що запускається локально

| Контейнер | Призначення |
| --- | --- |
| Backend | Spring Boot REST API, який тестується. |
| PostgreSQL | Бізнес-дані: користувачі, телефони, кошики, замовлення, відгуки і міграції. |
| Redis | Відкликані access і refresh токени. |
| MinIO | Бінарні файли зображень товарів. |

## Корисні URL

| Інструмент | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |
| Health | `http://localhost:8080/actuator/health` |

Якщо у вашому `test.env` вказані інші порти, використовуйте їх.

## Підключення до бази даних

Можна використовувати будь-який PostgreSQL-клієнт: DBeaver, pgAdmin або DataGrip.

| Параметр | Значення |
| --- | --- |
| Host | `localhost` |
| Port | `DB_PORT` з `test.env` |
| Database | `DB_NAME` з `test.env` |
| Username | `DB_USERNAME` з `test.env` |
| Password | `DB_PASSWORD` з `test.env` |

База доступна тільки тоді, коли Docker-середовище запущене.

## Доступ до MinIO

Відкрийте MinIO Console: `http://localhost:9001`.

| Параметр | Значення |
| --- | --- |
| Username | `MINIO_USERNAME` з `test.env` |
| Password | `MINIO_PASSWORD` з `test.env` |

Bucket `images` створюється після першого успішного завантаження зображення товару.

## Рекомендований smoke test

1. Відкрийте Swagger UI.
2. Зареєструйтесь або увійдіть через `/api/auth/**`.
3. Авторизуйте Swagger через отриманий bearer token.
4. Виконайте запит `/api/v1/phones` і перевірте, що каталог повертає дані.
5. Додайте товар у `/api/v1/me/cart/put`.
6. Створіть замовлення через `/api/v1/orders/checkout`.
7. Перевірте `/actuator/health`.

Для admin-only endpoint-ів використовуйте користувача з роллю admin із seed-даних або створіть його для тестового запуску.

## Troubleshooting

- **Swagger не відкривається:** перевірте, що backend-контейнер запущений, а порт `PORT` з `test.env` вільний.
- **Помилка міграцій:** запустіть reset-команду замість ручного видалення контейнерів.
- **Немає підключення до PostgreSQL:** перевірте `DB_PORT` і стан Docker Desktop.
- **Не завантажуються зображення:** перевірте MinIO credentials і порт `MINIO_STORAGE_PORT`.
