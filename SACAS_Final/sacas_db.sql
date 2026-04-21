-- ================================================================
--  SACAS - Student Academic Consultation Appointment System
--  Database Schema  |  Run this in MySQL Workbench / phpMyAdmin
-- ================================================================

CREATE DATABASE IF NOT EXISTS sacas_db;
USE sacas_db;

-- ---------------------------------------------------------------
-- 1. USERS  (central login table for all roles)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(60)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,  -- stores SHA-256 hash (64 hex chars),
    role       ENUM('ADMIN','STUDENT','INSTRUCTOR') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------
-- 2. STUDENTS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS students (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    student_no     VARCHAR(20) NOT NULL UNIQUE,  -- auto-generated: STU-YYYY-NNN
    firstname      VARCHAR(60) NOT NULL,
    middlename     VARCHAR(60),
    lastname       VARCHAR(60) NOT NULL,
    suffix         VARCHAR(10),
    address        TEXT,
    age            INT,
    birthdate      VARCHAR(20),
    mothers_name   VARCHAR(100),
    mothers_occ    VARCHAR(100),
    fathers_name   VARCHAR(100),
    fathers_occ    VARCHAR(100),
    elementary     VARCHAR(100),
    junior_high    VARCHAR(100),
    senior_high    VARCHAR(100),
    course         VARCHAR(100),
    schedule       VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- 3. INSTRUCTORS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS instructors (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    instructor_no VARCHAR(20) NOT NULL UNIQUE,  -- auto-generated: INS-YYYY-NNN
    firstname     VARCHAR(60) NOT NULL,
    middlename    VARCHAR(60),
    lastname      VARCHAR(60) NOT NULL,
    suffix        VARCHAR(10),
    address       TEXT,
    age           INT,
    birthdate     VARCHAR(20),
    course        VARCHAR(100),   -- department/course taught
    schedule      VARCHAR(50),
    timeslot      VARCHAR(50),
    subject1      VARCHAR(100),
    subject2      VARCHAR(100),
    subject3      VARCHAR(100),
    subject4      VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- 4. AVAILABILITY  (instructor sets which days/times they are free)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS availability (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    instructor_id INT NOT NULL,
    day_of_week   VARCHAR(15) NOT NULL,   -- Monday … Friday
    time_slot     VARCHAR(20) NOT NULL,   -- e.g. "8:00 A.M"
    is_recurring  TINYINT(1)  DEFAULT 1,
    UNIQUE KEY uq_avail (instructor_id, day_of_week, time_slot),
    FOREIGN KEY (instructor_id) REFERENCES instructors(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- 5. APPOINTMENTS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    instructor_id INT NOT NULL,
    day_of_week   VARCHAR(15) NOT NULL,
    time_slot     VARCHAR(30) NOT NULL,
    reason        TEXT,
    status        ENUM('PENDING','CONFIRMED','DECLINED','CANCELLED','COMPLETED')
                  DEFAULT 'PENDING',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id)    REFERENCES students(id)    ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES instructors(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- 6. NOTIFICATIONS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    message    TEXT NOT NULL,
    is_read    TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------
-- 7. CONSULTATION LOGS
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_logs (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  INT,
    student_name    VARCHAR(120),
    instructor_name VARCHAR(120),
    subject         VARCHAR(100),
    day_of_week     VARCHAR(15),
    time_slot       VARCHAR(30),
    status          VARCHAR(20),
    logged_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------
-- DEFAULT ADMIN ACCOUNT  (login: admin / admin123)
-- ---------------------------------------------------------------
INSERT IGNORE INTO users (username, password, role)
VALUES ('admin', SHA2('admin123', 256), 'ADMIN'); -- SHA-256 hashed (SECURITY: no plain-text passwords)

-- ---------------------------------------------------------------
-- HELPFUL VIEW
-- ---------------------------------------------------------------
CREATE OR REPLACE VIEW v_appointments AS
SELECT
    a.id,
    a.status,
    a.day_of_week,
    a.time_slot,
    a.reason,
    a.created_at,
    s.student_no,
    CONCAT(s.firstname,' ',s.lastname) AS student_name,
    s.course AS student_course,
    i.instructor_no,
    CONCAT(i.firstname,' ',i.lastname) AS instructor_name,
    i.course  AS instructor_dept
FROM appointments a
JOIN students    s ON a.student_id    = s.id
JOIN instructors i ON a.instructor_id = i.id;
