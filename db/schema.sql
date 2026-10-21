-- Schema (PostgreSQL, e.g. Neon). JPA also creates these tables on boot
-- (ddl-auto=update), so importing this file is optional.
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  identifier VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS attendance_sessions (
  id BIGSERIAL PRIMARY KEY,
  token VARCHAR(64) NOT NULL UNIQUE,
  subject VARCHAR(100) NOT NULL,
  classroom_lat DOUBLE PRECISION NOT NULL,
  classroom_lng DOUBLE PRECISION NOT NULL,
  radius_meters INT NOT NULL DEFAULT 100,
  starts_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_session_token ON attendance_sessions(token);
CREATE TABLE IF NOT EXISTS attendance_records (
  id BIGSERIAL PRIMARY KEY,
  session_id BIGINT NOT NULL REFERENCES attendance_sessions(id),
  student_id BIGINT NOT NULL REFERENCES users(id),
  lat DOUBLE PRECISION NOT NULL,
  lng DOUBLE PRECISION NOT NULL,
  distance_meters DOUBLE PRECISION NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
  submitted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_session_student UNIQUE (session_id, student_id)
);
