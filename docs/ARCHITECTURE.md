# Architecture

```
Phone (student) --HTTPS--> Cloudflare Pages (static HTML/JS)
        |                         | fetch API_BASE
        +---> QR: /student.html?s=TOKEN
                                          |
Cloudflare DNS/HTTPS --> Spring Boot (Render) --> MySQL (Aiven/Railway, H2 locally)
```

- Workers can't run JVM, so Spring Boot stays on Render; Cloudflare hosts frontend + DNS/HTTPS (+ optional Turnstile later).
- Session token = random 12-char, expiry checked with server clock, not browser.
- GPS: browser sends lat/lng, backend computes Haversine, rejects if > radius.
- One-record rule: UNIQUE(session_id, student_id) + pre-check for clean 409.
