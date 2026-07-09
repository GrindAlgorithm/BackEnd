# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Backend for **GrindAlgorithm** — a 백준(Baekjoon)-style, season-based competitive coding-judge site. Spring Boot 3.3.3 · Java 17 · Spring Data JPA / MariaDB 11.8 LTS · Spring Security · QueryDSL. The frontend is a **separate repo** at `../FrontEnd` (Vite + React + TS).

Current state: this is a **scaffold**. Only the `example` feature exists — it is the canonical template to copy when building real features. The real API surface (problems, submissions, seasons, rankings, OAuth2, etc.) is **specified but not yet implemented**; the spec lives in the frontend (see "Frontend contract" below).

## Commands

```bash
docker compose up -d     # local MariaDB 11.8 (first boot auto-applies src/main/resources/db/*.sql)
./gradlew build          # compile + test + generate QueryDSL Q-classes
./gradlew bootRun        # run app on http://localhost:8080 (default profile: local)
./gradlew test           # run all tests (JUnit 5)
./gradlew test --tests 'com.example.springboot.SpringbootApplicationTests'   # single test class
./gradlew test --tests '*.SpringbootApplicationTests.contextLoads'           # single test method
./gradlew clean          # needed after changing an @Entity so QueryDSL regenerates Q-classes
```

There is no lint step. Generated QueryDSL `Q*` classes land in `build/generated/` (do not edit; regenerated on build).

## Architecture

### Feature-package layout

Code is organized by **feature**, not by layer: `com.example.springboot.<feature>.{controller,service,repository,entity,dto}`. The `example` package shows the full stack — replicate its structure (and naming) for each new feature.

Request flow, traced through `example`:

```
Controller (@RestController, @RequestMapping("/api/v1/..."))
  → Service (interface)  →  ServiceImpl (@Service @Transactional)
    → Repository (extends JpaRepository)
      → Entity (@Entity)
```

### DTO conversion convention (important)

Three DTO roles, all converted via **static `of()` factory methods** — there is no mapper library:

- `XxxRequestDTO` — inbound request body/params.
- `XxxDTO` — the service-layer/internal model. `XxxDTO.of(entity)` builds it from an entity.
- `XxxResponseDTO` — outbound response. `XxxResponseDTO.of(dto)` builds it from the service DTO.

So the chain is `Entity → XxxDTO → XxxResponseDTO`. Controllers and services pass `XxxDTO`; entities never leave the service layer and response DTOs never enter it. Entities expose a static `createXxxEntity(...)` factory rather than public constructors.

### Response envelope

All controller responses wrap the payload in `ResponseResult<T>` (`util/ResponseResult.java`) via `ResponseResult.success(payload)` / `ResponseResult.error(payload)`. The `resultCode` string comes from `ResultCodeEnum` (`"0000"` success, `"9999"` fail). Add new codes there.

> Note: this success/fail envelope does **not** match the error envelope the frontend currently expects (`{ error: { code, message } }`, see contract doc §1.4). Reconcile the two before wiring real endpoints.

### Security (`config/SecurityConfig.java`)

- CSRF disabled; stateless-ish REST setup. CORS allows `http://localhost:3000` with credentials.
- `requestMatchers("/api/v1/example").permitAll()`; **everything else requires authentication**.
- `BCryptPasswordEncoder` bean provided.

> Known gaps to be aware of: (1) CORS origin is `localhost:3000`, but the frontend dev server runs on `5173` (and proxies `/api`, `/oauth2`) — align this when doing cross-origin work. (2) OAuth2 (GitHub/Google) and session-cookie auth are required by the contract but **not yet configured** here.

### Persistence (`src/main/resources/application.yml` + profiles)

- MariaDB 11.8 LTS via JPA/Hibernate. `hibernate.ddl-auto: none` and `generate-ddl: false` — **the schema is managed externally, not generated from entities.** When you add/change an entity, also update `src/main/resources/db/*.sql` and the DB by hand.
- `show-sql: true`. DB connection lives **only** in profile files: `application-local.yml` (gitignored; copy from `application-local.yml.example`). `spring.profiles.default: local`, so plain `bootRun` uses it.
- Local DB runs in Docker: `docker compose up -d` (MariaDB 11.8, db `example`, auto-inits from `src/main/resources/db/` on first volume creation; `docker compose down -v` to re-seed).

## Frontend contract — the source of truth for the real API

The backend must implement the contract defined on the frontend side. Before adding any real endpoint, read:

- `../FrontEnd/docs/BACKEND_INTEGRATION.md` — full endpoint/auth/error spec (Korean). Defines the screen↔endpoint mapping.
- `../FrontEnd/src/types/domain.ts` — response schemas (1:1 with the doc).
- `../FrontEnd/src/api/real.ts` + `client.ts` — exact paths/methods the frontend calls.

Key contract rules that constrain backend design:

- **Base path `/api/v1`**, JSON with **camelCase** fields, `null` for empty (not omitted), ISO 8601 timestamps.
- **Session-cookie auth** (`JSESSIONID`), requests sent with `credentials: 'include'`; **OAuth2** via Spring Security standard paths (`/oauth2/authorization/{github|google}` → `/login/oauth2/code/*`), redirect to `/` on success, `GET /me` to restore session. New OAuth users auto-register.
- **Problem body is IDE-only:** `GET /problems/{id}` returns metadata only (no statement/examples). The body is delivered **only** by `POST /problems/{id}/open`, which also records the open timestamp.
- **No permanent tier:** tier/score reset each season.
- **Judge0 must never be exposed to the client** — judging is server-side only.

UI text, comments, and the spec docs are in **Korean**; match that for user-facing strings.
