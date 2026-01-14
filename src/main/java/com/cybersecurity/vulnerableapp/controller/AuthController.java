package com.cybersecurity.vulnerableapp.controller;

import com.cybersecurity.vulnerableapp.model.User;
import com.cybersecurity.vulnerableapp.service.VulnerableUserService;
import jakarta.servlet.http.HttpSession;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private VulnerableUserService userService;

    // Simple in-memory rate limiting (not secure - just for demo)
    // VULNERABILITY: This is a basic counter, easily bypassed
    private Map<String, Integer> loginAttempts = new HashMap<>();

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        return "login";
    }

    /**
     * VULNERABLE LOGIN ENDPOINT
     * Vulnerabilities:
     * 1. SQL Injection - username/password directly concatenated into SQL
     * 2. Broken Authentication - no rate limiting, plain text passwords
     */
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        // VULNERABILITY: No proper rate limiting
        // A basic attempt counter that can be easily bypassed by changing IP
        loginAttempts.putIfAbsent(username, 0);
        
        try {
            // Use the vulnerable authentication method (SQL Injection vulnerability)
            User user = userService.authenticateUser(username, password);
            
            if (user != null) {
                // Reset login attempts on success
                loginAttempts.remove(username);
                
                // Set user in session
                session.setAttribute("user", user);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                
                return "redirect:/dashboard";
            } else {
                // Increment failed attempts (but no lockout implemented)
                loginAttempts.put(username, loginAttempts.get(username) + 1);
                
                // VULNERABILITY: No account lockout after X failed attempts
                // VULNERABILITY: Error message could help with user enumeration
                redirectAttributes.addAttribute("error", "invalid");
                return "redirect:/login";
            }
        } catch (Exception e) {
            // Handle SQL exceptions gracefully (e.g., from malformed SQL injection attempts)
            // In a real application, we'd log this for security monitoring
            // For demo purposes, we just redirect to login with error
            loginAttempts.putIfAbsent(username, 0);
            loginAttempts.put(username, loginAttempts.get(username) + 1);
            redirectAttributes.addAttribute("error", "invalid");
            return "redirect:/login";
        }
    }

    @GetMapping("/signup")
    public String signupPage(Model model, @RequestParam(required = false) String error) {
        if (error != null) {
            model.addAttribute("error", "Username or email already exists");
        }
        return "signup";
    }

    /**
     * FIXED: MASS ASSIGNMENT / BROKEN ACCESS CONTROL
     * 
     * SECURITY FIX:
     * - Removed role parameter from signup endpoint
     * - Role is always set to "USER" on the server side
     * - Users cannot escalate privileges during registration
     * - Role assignment is controlled by server, not user input
     * 
     * This prevents privilege escalation attacks where users could:
     * - Set role=ADMIN during signup via form manipulation
     * - Use HTTP request manipulation (Burp Suite, etc.) to set role
     * - Gain unauthorized admin access immediately after registration
     */
    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                        @RequestParam String email,
                        @RequestParam String password,
                        RedirectAttributes redirectAttributes) {
        
        // SECURE: Role is NOT accepted from user input
        // Role is always set to "USER" on the server side
        // This prevents Mass Assignment vulnerability
        
        User user = userService.createUser(username, email, password);
        
        if (user != null) {
            redirectAttributes.addFlashAttribute("success", 
                "Account created successfully! Please login.");
            return "redirect:/login";
        } else {
            redirectAttributes.addAttribute("error", "exists");
            return "redirect:/signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

