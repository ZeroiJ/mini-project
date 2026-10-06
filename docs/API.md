# API contract (MVP)

Base: `API_BASE` in `frontend/config.js`.

## POST /api/auth/login
Req: `{"identifier":"prof@demo","password":"prof123"}`
Res: `{"token":"tok_1","id":1,"name":"Prof Demo","role":"PROFESSOR"}`
Err: 401 invalid credentials.

## POST /api/sessions (professor, header `X-User-Token`)
Req: `{"subject":"FSJP-A","durationMinutes":10,"lat":19.076,"lng":72.8777,"radius":100}`
Res: `{"id":1,"token":"abc123","qrUrl":"https://<pages>/student.html?s=abc123","endsAt":"..."}`
Err: 403 not professor.

## GET /api/sessions/{token} (public)
Res: `{"subject":"FSJP-A","endsAt":"...","serverNow":"...","expired":false}`

## POST /api/attendance (student, header `X-User-Token`)
Req: `{"sessionToken":"abc123","lat":19.0761,"lng":72.8778}`
Res: `{"ok":true,"distanceMeters":14.2}`
Err: 401 no token, 403 not student, 410 expired, 409 duplicate, 422 out-of-range (body has distance).

## GET /api/sessions/{id}/records (professor)
Res: `[{"student":"Aarav","identifier":"s1@demo","distanceMeters":14.2,"submittedAt":"..."}]`
`?format=csv` returns CSV download.
