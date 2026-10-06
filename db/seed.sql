-- Demo seed. Passwords are plain for MVP (hash in prod). ponytail: ceiling = demo-only auth.
INSERT INTO users (name, identifier, password_hash, role) VALUES
 ('Prof Demo','prof@demo','prof123','PROFESSOR'),
 ('Aarav','s1@demo','s123','STUDENT'),
 ('Diya','s2@demo','s123','STUDENT'),
 ('Kabir','s3@demo','s123','STUDENT');
-- Sample classroom: Mumbai Univ. Change to your classroom coords.
-- INSERT INTO attendance_sessions (token, subject, classroom_lat, classroom_lng, radius_meters, ends_at, created_by)
-- VALUES ('demo123','FSJP-A',19.0760,72.8777,100, DATE_ADD(NOW(), INTERVAL 10 MINUTE), 1);
