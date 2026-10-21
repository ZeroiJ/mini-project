-- Synthetic demo history for the QR + GPS attendance project.
--
-- WHAT THIS IS: six past attendance sessions and fifteen check-ins for the
-- seeded demo students, so the tables are populated for screenshots, the
-- Report page and dashboard testing. These are NOT real class records --
-- present them as sample/demo data, not as actual attendance history.
--
-- HOW TO RUN: Supabase dashboard -> SQL Editor -> paste -> Run.
-- The backend must have started at least once, so that SeedConfig created the
-- demo users and Hibernate created the tables.
-- Safe to re-run: existing rows are skipped (no duplicates).

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM users WHERE identifier = 'prof@demo') THEN
    RAISE EXCEPTION 'Demo users are missing. Start the backend once, then run this script again.';
  END IF;
END $$;

-- 1) Six past sessions (10 minutes each), spread over the last 24 days.
INSERT INTO attendance_sessions
  (token, subject, classroom_lat, classroom_lng, radius_meters, starts_at, ends_at, created_by)
SELECT v.token, 'FSJP-A', 19.0760, 72.8777, 100,
       now() - make_interval(days => v.days_ago) - interval '5 minutes',
       now() - make_interval(days => v.days_ago) + interval '5 minutes',
       (SELECT id FROM users WHERE identifier = 'prof@demo')
FROM (VALUES
  ('d0d0a1b2c3e4', 24),
  ('d0d0b2c3d4e5', 21),
  ('d0d0c3d4e5f6', 14),
  ('d0d0d4e5f6a7', 10),
  ('d0d0e5f6a7b8',  4),
  ('d0d0f6a7b8c9',  2)
) AS v(token, days_ago)
WHERE NOT EXISTS (
  SELECT 1 FROM attendance_sessions s WHERE s.token = v.token
);

-- 2) Fifteen check-ins. Coordinates are offset from the classroom by the
--    recorded distance (northwards), so distance and coordinates agree.
--    Attendance pattern: s1 attended all six, s2 five, s3 four.
INSERT INTO attendance_records
  (session_id, student_id, lat, lng, distance_meters, status, submitted_at)
SELECT s.id, u.id,
       19.0760 + (v.distance_m / 111320.0),
       72.8777,
       v.distance_m,
       'PRESENT',
       s.starts_at + interval '1 minute'
FROM (VALUES
  ('d0d0a1b2c3e4', 's1@demo', 18.5),
  ('d0d0a1b2c3e4', 's2@demo', 44.2),
  ('d0d0b2c3d4e5', 's1@demo', 12.1),
  ('d0d0b2c3d4e5', 's2@demo', 57.8),
  ('d0d0b2c3d4e5', 's3@demo', 33.0),
  ('d0d0c3d4e5f6', 's1@demo', 26.4),
  ('d0d0c3d4e5f6', 's3@demo', 71.3),
  ('d0d0d4e5f6a7', 's1@demo',  9.7),
  ('d0d0d4e5f6a7', 's2@demo', 38.6),
  ('d0d0d4e5f6a7', 's3@demo', 62.9),
  ('d0d0e5f6a7b8', 's1@demo', 21.0),
  ('d0d0e5f6a7b8', 's2@demo', 48.4),
  ('d0d0e5f6a7b8', 's3@demo', 15.3),
  ('d0d0f6a7b8c9', 's1@demo', 30.8),
  ('d0d0f6a7b8c9', 's2@demo', 55.1)
) AS v(token, identifier, distance_m)
JOIN attendance_sessions s ON s.token = v.token
JOIN users u ON u.identifier = v.identifier
WHERE NOT EXISTS (
  SELECT 1 FROM attendance_records r
  WHERE r.session_id = s.id AND r.student_id = u.id
);

-- 3) Show the seeded sessions. Type one of these ids into report.html
--    to see its attendance table.
SELECT s.id, s.token, s.subject,
       to_char(s.starts_at AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI') AS starts_at_ist,
       count(r.id) AS attendees
FROM attendance_sessions s
LEFT JOIN attendance_records r ON r.session_id = s.id
WHERE s.token LIKE 'd0d0%'
GROUP BY s.id, s.token, s.subject, s.starts_at
ORDER BY s.starts_at DESC;
