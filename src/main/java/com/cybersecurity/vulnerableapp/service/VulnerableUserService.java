package com.cybersecurity.vulnerableapp.service;

import com.cybersecurity.vulnerableapp.model.User;
import com.cybersecurity.vulnerableapp.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VulnerableUserService {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * VULNERABILITY 1: SQL INJECTION
     * This method is intentionally vulnerable to SQL injection attacks.
     * 
     * ATTACK EXAMPLE:
     * Username: admin' OR '1'='1' --
     * Password: anything
     * 
     * This will bypass authentication because the SQL query becomes:
     * SELECT * FROM users WHERE username = 'admin' OR '1'='1' --' AND password = 'anything'
     * 
     * FIX: Use parameterized queries or JPA methods instead of string concatenation
     */
    public User authenticateUser(String username, String password) {
        // VULNERABLE: Building SQL query with string concatenation
        String sql = "SELECT * FROM users WHERE username = '" + username 
                    + "' AND password = '" + password + "'";
        
        Query query = entityManager.createNativeQuery(sql, User.class);
        
        @SuppressWarnings("unchecked")
        List<User> users = query.getResultList();
        
        if (!users.isEmpty()) {
            return users.get(0);
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

