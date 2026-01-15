-- Quick script to check all users in the database
-- Run this first to see what's in the database

SELECT 
    id, 
    username, 
    email, 
    role, 
    LEFT(password, 30) as password_preview,
    created_at
FROM users 
ORDER BY id;

