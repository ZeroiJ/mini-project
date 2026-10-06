-- MVP schema (MySQL 8). H2-compatible types.
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  identifier VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL
);
CREATE TABLE IF NOT EXISTS attendance_sessions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token VARCHAR(64) NOT NULL UNIQUE,
  subject VARCHAR(100) NOT NULL,
  classroom_lat DOUBLE NOT NULL,
  classroom_lng DOUBLE NOT NULL,
  radius_meters INT NOT NULL DEFAULT 100,
  starts_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ends_at TIMESTAMP NOT NULL,
  created_by BIGINT NOT NULL,
  FOREIGN KEY (created_by) REFERENCES users(id)
);
CREATE INDEX idx_session_token ON attendance_sessions(token);
CREATE TABLE IF NOT EXISTS attendance_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  lat DOUBLE NOT NULL,
  lng DOUBLE NOT NULL,
  distance_meters DOUBLE NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_session_student (session_id, student_id),
  FOREIGN KEY (session_id) REFERENCES attendance_sessions(id),
  FOREIGN KEY (student_id) REFERENCES users(id)
);
