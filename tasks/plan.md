# Implementation Plan: QR + GPS Attendance (2-person MVP)

## Overview
Professor starts a short-lived session -> QR holds `student.html?s=TOKEN` -> student logs in, shares GPS, submits once -> backend enforces expiry, Haversine radius, one-record rule -> professor sees/downloads report. Frontend on Cloudflare Pages, Spring Boot API on Render, MySQL (H2 locally).

## Architecture Decisions
- Spring Boot 3 + JPA, H2 default, MySQL via env profile (no local MySQL needed).
- No JWT: demo `X-User-Token` = `tok_{userId}` stored in memory map + DB lookup. Explainable in viva, replaceable later.
- QR URL contract: `${FRONTEND}/student.html?s=TOKEN`. Backend returns `qrUrl` built from `FRONTEND_URL` env.
- All validation server-side (expiry, distance, role, duplicate).

## Task List
### Phase 1: Foundation
- [x] Task 1: DB schema + seed + docs contract
- [ ] Task 2: Backend entities/repos/auth
- [ ] Task 3: Session + attendance APIs + Haversine
### Checkpoint: Foundation
- [ ] Backend boots with H2, login works via curl
### Phase 2: Core
- [ ] Task 4: Frontend login/professor/student/report
- [ ] Task 5: Cloudflare Pages config + README demo script
### Checkpoint: Core
- [ ] End-to-end: start session -> QR -> submit -> report shows 1 row
### Phase 3: Polish
- [ ] Task 6: CSV export, countdown, error messages
### Checkpoint: Complete
- [ ] All acceptance criteria met, ready for viva

## Risks
| Risk | Impact | Mitigation |
| GPS spoof in demo | Med | Note as limitation, server-side radius check |
| Render cold start | Low | Keep backend tiny, H2 fallback |
| No mvn locally | Low | Maven Wrapper committed |

## Open Questions
- Real class/subject list? -> later via seed.sql, not now.
