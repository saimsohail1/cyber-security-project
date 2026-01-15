-- Create admin user with BCrypt-hashed password
-- Password: admin
-- This script should be run in the H2 console

-- Delete existing admin user if it exists
DELETE FROM users WHERE username = 'admin';

-- Insert admin user with BCrypt-hashed password
INSERT INTO users (username, email, password, role, created_at)
VALUES ('admin', 'admin@example.com', '$2a$10$2bfkz6bNqZdMPwCz4NdYJuQLrnj/XSv6gEJgnmNp05a4kH/6XPfoK', 'ADMIN', CURRENT_TIMESTAMP);

-- Verify the user was created
SELECT username, email, role FROM users WHERE username = 'admin';

