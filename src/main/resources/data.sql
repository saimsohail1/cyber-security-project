-- Initialize database with demo users
-- WARNING: These are intentionally vulnerable examples for demonstration

-- Demo user 1: Regular user
INSERT INTO users (username, email, password, role, created_at) 
VALUES ('user1', 'user1@example.com', 'password123', 'USER', CURRENT_TIMESTAMP);

-- Demo user 2: Another regular user
INSERT INTO users (username, email, password, role, created_at) 
VALUES ('alice', 'alice@example.com', 'alice123', 'USER', CURRENT_TIMESTAMP);

-- Demo user 3: Admin user (with weak password)
INSERT INTO users (username, email, password, role, created_at) 
VALUES ('admin', 'admin@example.com', 'admin', 'ADMIN', CURRENT_TIMESTAMP);

-- NOTE: All passwords are stored in plain text (Broken Authentication vulnerability)
-- These are intentionally weak passwords for demonstration purposes

