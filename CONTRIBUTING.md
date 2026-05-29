# Contributing

## Workflow

1. Create a branch from `develop`.
2. Use a focused branch name:
   - `feat/<short-description>`
   - `fix/<short-description>`
   - `docs/<short-description>`
   - `test/<short-description>`
   - `refactor/<short-description>`
3. Keep changes small and reviewable.
4. Run verification locally.
5. Open a pull request into `develop`.

## Commit Messages

Use Conventional Commits:

```text
feat: add admin order filtering
fix: prevent expired reset token reuse
docs: add architecture documentation
test: cover cart total recalculation
refactor: simplify phone mapping
```

Allowed types:

- `feat`
- `fix`
- `docs`
- `test`
- `refactor`
- `chore`
- `ci`
- `build`

## Local Verification

```bash
mvn clean verify -Dspring.profiles.active=dev
```

## Pull Request Requirements

Every pull request should include:

- Summary of changes
- Testing notes
- Linked issue or task, if applicable
- API examples or screenshots, if relevant
- Documentation updates, if behavior changed

## Engineering Standards

- Keep controllers thin.
- Keep business logic in services.
- Keep persistence logic in repositories and specifications.
- Use DTOs for API contracts.
- Use MapStruct for mapping.
- Validate input at API boundaries.
- Add tests for meaningful behavior.
- Do not commit secrets, local environment files, IDE files or build output.

## Review Expectations

A pull request should be easy to review:

- One purpose per pull request.
- Clear names for classes, methods and commits.
- No unrelated formatting or refactoring.
- No hidden behavior changes.
- Tests should prove the intended behavior.
