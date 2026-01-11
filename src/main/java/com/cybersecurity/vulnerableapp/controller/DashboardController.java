package com.cybersecurity.vulnerableapp.controller;

import com.cybersecurity.vulnerableapp.model.User;
import com.cybersecurity.vulnerableapp.service.VulnerableUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private VulnerableUserService userService;

    /**
     * PROTECTED ENDPOINT
     * This endpoint requires authentication (checked via session)
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("role", user.getRole());
        
        return "dashboard";
    }

    /**
     * ADMIN ENDPOINT
     * Vulnerable to privilege escalation if user can set role=ADMIN during signup
     */
    @GetMapping("/admin")
    public String admin(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "Access denied. Admin role required.");
            return "dashboard";
        }
        
        // Show all users (sensitive data)
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("user", user);
        
        return "admin";
    }
}

