# Quality And Release Readiness

> A short checklist for reviewers and maintainers. The goal is to make every release reproducible, testable and easy to
> inspect before it reaches `main`.

## Verification Command

```bash
mvn -B clean verify -Dspring.profiles.active=dev
```

This command compiles the project, runs all tests, builds the application jar, generates the JaCoCo report and checks
the configured coverage gate.

## Current Quality Signals

| Area | Signal |
| --- | --- |
| Test suite | 905 tests pass locally with the `dev` profile. |
| Coverage gate | JaCoCo enforces at least 70% line coverage. |
| Current line coverage | 80.7% from the latest local report. |
| Integration tests | PostgreSQL, Redis and MinIO are covered with Testcontainers. |
| Migrations | Flyway validates schema state on startup. |
| CI | GitHub Actions runs on PRs and pushes to `develop` and `main`. |
| Coverage visibility | CI posts a JaCoCo report to PRs and uploads the HTML report as an artifact. |

## Test Strategy

- **Unit tests** cover services, utilities, validators and security helpers.
- **Controller tests** verify request/response contracts, validation and access rules.
- **Integration tests** run against real infrastructure dependencies through Testcontainers.
- **Profile tests** check production exposure rules, including disabled Swagger and test-data endpoints.
- **Migration coverage** comes from application context startup against a real PostgreSQL container.

## Pull Request Checklist

- [ ] Branch name clearly describes the release or feature.
- [ ] Commits use Conventional Commits.
- [ ] `mvn -B clean verify -Dspring.profiles.active=dev` passes locally.
- [ ] CI is green on GitHub.
- [ ] OpenAPI docs open in the `dev` profile.
- [ ] Database migrations are reviewed in order and have clear names.
- [ ] Release notes mention user-visible changes, migrations, config changes and known risks.
- [ ] No secrets, local `.env` values or generated `target/` files are committed.

## Known Technical Risks

| Risk | Status |
| --- | --- |
| `spring.jpa.open-in-view` uses the Spring Boot default. | Keep as a separate hardening task because disabling it can expose lazy-loading regressions. |
| Dev seed migration is versioned as `V900`. | Acceptable for dev profile; keep it out of `prod`. |
| Payment webhooks are public callbacks. | Keep provider signature checks aligned with the active provider. |
| Docker volumes preserve local state. | Use documented reset commands before testing migration changes. |
