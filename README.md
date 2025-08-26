Translatio – Translation CMS

Status: In progress. This project is actively being developed; features and APIs may change.

This repository contains a Translation CMS with a Ktor backend, Kotlin Multiplatform UI, PostgreSQL storage, import/export, audit history, and Docker Compose setup.

Highlights
- Auth: Username + password login (admin can be seeded via env). OAuth via GitHub, Google, Microsoft, and Apple (configure via env). 
- Manage: Create string keys, add per-locale translations, manage locales.
- Import: Upload Android strings.xml, Flutter .arb, or iOS .strings; review before committing.
- Export: Generate Android XML, Flutter ARB, iOS .strings, and Web JSON; exports are versioned.
- History: Basic audit trail for key actions (create, update, import, export).
- Storage: PostgreSQL via Exposed ORM.
- Docker: docker-compose with services: db (Postgres), cms (Ktor server), web (Nginx serving Compose Web UI + proxy), web-builder (builds the UI on first run).

Quick start (Docker)
1. Prerequisites: Docker and Docker Compose.
2. By default, only Postgres is enabled in docker-compose.yml. Uncomment cms, web, and web-builder services to run the full stack.
3. From repo root, run: `docker compose up --build`
4. Web UI: Open http://localhost/ (served by Nginx). The UI is the Compose Multiplatform Wasm app, proxied to the server API at /api.
   - Server direct (optional): http://localhost:8080/ (Ktor server). In the Docker setup, the server can run API-only and be fronted by Nginx.
5. Default seeded admin (for demo): ADMIN_USER=admin, ADMIN_PASS=admin123 (set in docker-compose.yml). Change for production.

Configuration
- DATABASE_URL (default: jdbc:postgresql://db:5432/translatio)
- POSTGRES_USER (default: postgres)
- POSTGRES_PASSWORD (default: postgres)
- ADMIN_USER / ADMIN_PASS – will create the user if it does not exist.
- PUBLIC_URL – public base URL used for OAuth callbacks (e.g., http://localhost:8080)
- GitHub: GITHUB_CLIENT_ID, GITHUB_CLIENT_SECRET
- Google: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
- Microsoft: MICROSOFT_CLIENT_ID, MICROSOFT_CLIENT_SECRET
- Apple: APPLE_CLIENT_ID, APPLE_CLIENT_SECRET (JWT client secret)

Project layout
- /server – Ktor CMS server (Ktor 3.x, Exposed ORM, Koin DI)
- /composeApp, /shared, /iosApp – KMP app modules (Compose Multiplatform UI in shared)
- /shared-model – shared Kotlin models between server and client
- /postman – Postman collection and environments
- /resources – project resources (e.g., icons, configs)
- /kotlin-js-store – npm cache for Kotlin/JS builds (if present)
- docker-compose.yml, nginx.conf – local infra

Compose Web UI (KMP)
- The shared module (shared/src/commonMain) contains a simple web UI in Compose (webui/WebUi.kt) and a Ktor client (webui/Api.kt) that talks to the server JSON API under /api.
- Platforms:
  - Desktop/JVM and Android use base URL http://localhost:8080 by default.
  - Wasm JS uses a relative base URL "" (same-origin). To use the UI in a browser, serve the compiled wasm app from the same origin as the server (e.g., behind a proxy) or adjust platformBaseUrl() as needed.
- Sessions: The Ktor client installs HttpCookies with AcceptAllCookiesStorage so login persists across requests via the TRANSLATIO_SESSION cookie.

Architecture (high level)
- Backend: Ktor 3.x on JVM, Exposed ORM, PostgreSQL. Koin for DI. Sessions for auth cookies. Resourceful routing under /api.
- Frontend: Kotlin Multiplatform with Compose Multiplatform UI (web via Wasm JS; potentially desktop/Android). Ktor client with JSON serialization.
- Shared: Data models and some logic shared across client and server via shared-model and shared modules.
- Infra: Docker Compose for local Postgres and optional server/UI; Nginx serves static web UI and proxies /api to Ktor.
- Auth: Session-based login; optional OAuth providers (GitHub, Google, Microsoft, Apple) via environment-configured credentials.

Notes
- OAuth (GitHub, Google, Microsoft, Apple) can be integrated by adding Ktor OAuth configuration and env secrets; placeholders are ready in the auth pipeline.
- Export versions are tracked; downloading any format does not mutate data; “Create New Export Version” records a new version for traceability.

Postman collection
- Import postman/Translatio.postman_collection.json into Postman.
- Choose an environment from postman/environments:
  - "Translatio - Docker (Nginx)" uses baseUrl http://localhost (Nginx) where API is under /api.
  - "Translatio - Direct Server (8080)" uses baseUrl http://localhost:8080 (direct Ktor server) where API is under /api.
- The collection exercises:
  - Health: GET /api, GET /api/health
  - Auth: POST /api/register (email becomes username; role forced to viewer), POST /api/login, POST /api/logout, GET /api/providers
  - Locales: GET /api/locales, POST /api/locales
- Sessions: Postman will retain the TRANSLATIO_SESSION cookie between requests so that locales endpoints work after login.
- Default admin credentials configured in docker-compose: admin / admin123 (edit environment variables if changed).