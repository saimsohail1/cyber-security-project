package com.cybersecurity.vulnerableapp.service;

import com.cybersecurity.vulnerableapp.model.User;
import com.cybersecurity.vulnerableapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VulnerableUserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * FIXED: SQL INJECTION VULNERABILITY
     * 
     * SECURITY FIX:
     * - Replaced string concatenation with JPA repository method
     * - Uses parameterized query internally (safe from SQL injection)
     * - Finds user by username first, then compares password in Java
     * 
     * This prevents SQL injection attacks because:
     * 1. JPA repository methods use parameterized queries automatically
     * 2. User input is never directly concatenated into SQL
     * 3. Password comparison happens in Java code, not SQL
     * 
     * Example attack that is now prevented:
     * Username: admin' OR '1'='1' --
     * This will now be treated as a literal username string, not SQL code
     */
    public User authenticateUser(String username, String password) {
        // SECURE: Using JPA repository method with parameterized query
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Compare password in Java (will be hashed in next fix)
            if (user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    /**
     * VULNERABILITY 2: BROKEN AUTHENTICATION
     * - Passwords stored in plain text (no hashing)
     * - No password complexity requirements
     * - No rate limiting (brute force attacks possible)
     * - No account lockout mechanism
     * 
     * ATTACK SCENARIOS:
     * 1. Database breach exposes all passwords in plain text
     * 2. Brute force attacks are easy without rate limiting
     * 3. Weak passwords are accepted
     * 
     * FIX: 
     * - Use BCrypt/Argon2 for password hashing
     * - Implement rate limiting (Spring Security Rate Limiter)
     * - Add account lockout after failed attempts
     * - Enforce password complexity rules
     */
    @Transactional
    public User createUser(String username, String email, String password, String role) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent() ||
            userRepository.findByEmail(email).isPresent()) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // VULNERABLE: Storing password in plain text
        // FIX: Use BCryptPasswordEncoder.encode(password)
        user.setPassword(password);
        
        user.setRole(role != null ? role : "USER");
        
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

