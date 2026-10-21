# QR + GPS Attendance — FSJP mini project (2 people)

## Split
- Teammate A: `backend/` + `db/` (Spring Boot, Postgres schema/seed).
- Teammate B: `frontend/` + Cloudflare Pages + this README demo.

## Run locally
```bash
# No DB setup needed: H2 in-memory runs by default.
# backend needs Maven once: download from maven.apache.org, then:
cd backend && mvn spring-boot:run
# frontend
cd frontend && npx serve -p 8000
# open http://localhost:8000, set localStorage.API_BASE="http://localhost:8080"
```

## Deploy
- Frontend: Cloudflare Pages, root `frontend/`, no build.
- Backend: Render web service, **runtime Docker**, root `backend/`, env `DB_URL/DB_USER/DB_PASS/FRONTEND_URL`.
- DB: Supabase Postgres (free, no card). Use the **session pooler** string (port 5432, IPv4) — the direct host is IPv6-only. `DB_URL` = that string with a `jdbc:postgresql://` prefix and `?sslmode=require`; `DB_USER` = `postgres.<project-ref>`. Tables are created on boot; importing `db/schema.sql` is optional.

## Viva demo (5 steps)
1. Professor login `prof@demo/prof123` -> Start 10-min session -> QR shows.
2. Student login `s1@demo/s123` -> scan QR (open qrUrl on phone) -> Submit GPS.
3. Second submit -> 409 already marked.
4. Walk far / spoof coords -> 422 out of range.
5. Professor Refresh -> table + CSV.

Demo logins: prof@demo/prof123, s1-3@demo/s123.

## ponytail notes (deliberate simplifications)
- Auth = demo tokens, not JWT — ceiling: single-instance; upgrade to JWT.
- Haversine in `GeoService` only; no anti-spoof.
