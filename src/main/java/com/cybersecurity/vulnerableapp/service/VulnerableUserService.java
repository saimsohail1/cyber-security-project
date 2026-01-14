package com.cybersecurity.vulnerableapp.service;

import com.cybersecurity.vulnerableapp.model.User;
import com.cybersecurity.vulnerableapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VulnerableUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * FIXED: SQL INJECTION VULNERABILITY + BROKEN AUTHENTICATION (Password Hashing)
     * 
     * SECURITY FIXES:
     * 1. SQL Injection: Uses JPA repository method with parameterized queries
     * 2. Broken Authentication: Uses BCrypt password hashing for secure password verification
     * 
     * Password Security:
     * - Passwords are hashed using BCrypt before storage
     * - BCrypt automatically salts passwords (unique salt per password)
     * - BCrypt is resistant to rainbow table attacks
     * - Uses passwordEncoder.matches() for secure comparison
     * 
     * Example attack that is now prevented:
     * - SQL Injection: admin' OR '1'='1' -- (treated as literal string)
     * - Plain-text password exposure: Database breach won't expose readable passwords
     */
    public User authenticateUser(String username, String password) {
        // SECURE: Using JPA repository method with parameterized query
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // SECURE: Compare password using BCrypt (handles hashed passwords)
            // BCrypt.matches() compares plain password with hashed password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * FIXED: BROKEN AUTHENTICATION + MASS ASSIGNMENT
     * 
     * SECURITY FIXES:
     * 1. Password Hashing: Passwords are hashed using BCrypt before storage
     * 2. Mass Assignment: Role is always set to "USER" on server side
     * 
     * Mass Assignment Fix:
     * - Removed role parameter from method signature
     * - Role is always set to "USER" (never from user input)
     * - Prevents privilege escalation during registration
     * - Only administrators can change user roles (via separate admin function)
     * 
     * BCrypt Benefits:
     * 1. One-way hashing (passwords cannot be reversed)
     * 2. Unique salt per password (stored in the hash itself)
     * 3. Configurable cost factor (default 10 rounds)
     * 4. Resistant to rainbow table attacks
     */
    @Transactional
    public User createUser(String username, String email, String password) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent() ||
            userRepository.findByEmail(email).isPresent()) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        // SECURE: Hash password using BCrypt before storage
        // BCrypt automatically generates and stores a unique salt
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        
        // SECURE: Role is always set to "USER" on the server side
        // Users cannot set their own role (prevents Mass Assignment vulnerability)
        // Only administrators can change roles through proper admin functions
        user.setRole("USER");
        
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

