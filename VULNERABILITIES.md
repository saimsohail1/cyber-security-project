# Vulnerability Documentation

## Quick Reference Guide

### Vulnerability 1: SQL Injection 🔴

**Status:** Implemented ✅

**Location:** `src/main/java/com/cybersecurity/vulnerableapp/service/VulnerableUserService.java`

**Method:** `authenticateUser(String username, String password)`

**Attack Vector:**
- Login page (`/login`)
- Username field accepts SQL injection payloads

**Test Payloads:**
```
Username: admin' OR '1'='1' --
Password: anything

Username: ' OR '1'='1' --
Password: ' OR '1'='1' --

Username: admin'--
Password: (leave empty or anything)
```

**Expected Behavior:**
- Bypasses authentication
- Logs in as the first user in the database
- Can access user dashboard without valid credentials

**How to Fix:**
1. Replace native SQL queries with JPA repository methods
2. Use parameterized queries if native SQL is necessary
3. Example fix:
```java
public User authenticateUser(String username, String password) {
    Optional<User> userOpt = userRepository.findByUsername(username);
    if (userOpt.isPresent()) {
        User user = userOpt.get();
        // Use password encoder to verify
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
    }
    return null;
}
```

---

### Vulnerability 2: Broken Authentication 🔴

**Status:** Implemented ✅

**Multiple sub-vulnerabilities:**

#### 2.1 Plain-Text Password Storage

**Location:** 
- `src/main/java/com/cybersecurity/vulnerableapp/model/User.java`
- `src/main/java/com/cybersecurity/vulnerableapp/service/VulnerableUserService.java`

**How to Test:**
1. Create a new account via `/signup`
2. Login as admin (username: `admin`, password: `admin`)
3. Navigate to `/admin`
4. Observe all passwords displayed in plain text

**Expected Behavior:**
- All user passwords visible in plain text
- Database breach immediately exposes all credentials

**How to Fix:**
```java
@Autowired
private BCryptPasswordEncoder passwordEncoder;

user.setPassword(passwordEncoder.encode(password));
```

#### 2.2 No Rate Limiting

**Location:** `src/main/java/com/cybersecurity/vulnerableapp/controller/AuthController.java`

**How to Test:**
1. Try logging in with wrong credentials multiple times (100+ attempts)
2. Observe that login attempts are never blocked
3. Use a script to try thousands of password combinations

**Expected Behavior:**
- Unlimited login attempts allowed
- No account lockout
- Brute force attacks are easy

**How to Fix:**
- Implement Spring Security with rate limiting
- Use `@RateLimiter` annotation
- Lock accounts after N failed attempts

#### 2.3 No Password Complexity Requirements

**Location:** `src/main/java/com/cybersecurity/vulnerableapp/controller/AuthController.java`

**How to Test:**
1. Go to `/signup`
2. Create account with password: `123`
3. Account is created successfully

**Expected Behavior:**
- Weak passwords accepted
- No minimum length requirement
- No complexity requirements

**How to Fix:**
- Add validation for password complexity
- Use Spring Validation with custom validator
- Example:
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$")
private String password;
```

---

### Vulnerability 3: Mass Assignment Risk 🟡

**Status:** Implemented ✅

**Location:** 
- `src/main/java/com/cybersecurity/vulnerableapp/controller/AuthController.java`
- Signup endpoint

**How to Test:**
1. During signup, modify the form to include: `<input type="hidden" name="role" value="ADMIN">`
2. Or use a tool like Burp Suite to send:
   ```
   POST /signup
   Content-Type: application/x-www-form-urlencoded
   
   username=testuser&email=test@example.com&password=test123&role=ADMIN
   ```
3. Login and verify you have ADMIN role
4. Access `/admin` endpoint

**Expected Behavior:**
- Users can set their own role during signup
- Privilege escalation possible

**How to Fix:**
- Always set role on the server side
- Ignore role parameter in signup requests
- Use DTOs to control which fields can be set
```java
@PostMapping("/signup")
public String signup(@Valid SignupDTO signupDTO, ...) {
    // role is not in SignupDTO, so users can't set it
    User user = userService.createUser(
        signupDTO.getUsername(),
        signupDTO.getEmail(),
        signupDTO.getPassword(),
        "USER"  // Always set to USER on server side
    );
}
```

---

## OWASP Mapping

| Vulnerability | OWASP Top 10 2021 | OWASP API Top 10 |
|--------------|-------------------|------------------|
| SQL Injection | A03:2021 – Injection | API8:2023 – Security Misconfiguration |
| Broken Authentication | A07:2021 – Identification and Authentication Failures | API2:2023 – Broken Authentication |
| Mass Assignment | A01:2021 – Broken Access Control | API1:2023 – Broken Object Level Authorization |

---

## CWE Mapping

| Vulnerability | CWE Number | CWE Name |
|--------------|-----------|----------|
| SQL Injection | CWE-89 | Improper Neutralization of Special Elements used in an SQL Command |
| Plain-Text Password Storage | CWE-256 | Plaintext Storage of a Password |
| No Rate Limiting | CWE-307 | Improper Restriction of Excessive Authentication Attempts |
| No Password Complexity | CWE-521 | Weak Password Requirements |
| Mass Assignment | CWE-915 | Improperly Controlled Modification of Dynamically-Determined Object Attributes |

---

## Testing Checklist

### SQL Injection Tests
- [ ] Try `admin' OR '1'='1' --` in username field
- [ ] Try `' OR '1'='1' --` in username field
- [ ] Try `admin'--` in username field
- [ ] Try `'; DROP TABLE users; --` (wont work with H2, but demonstrates risk)
- [ ] Try UNION-based SQL injection
- [ ] Verify authentication is bypassed

### Broken Authentication Tests
- [ ] Create account and verify password is in plain text in database
- [ ] Login as admin and view `/admin` to see all plain-text passwords
- [ ] Try logging in with wrong credentials 100+ times
- [ ] Verify no account lockout occurs
- [ ] Create account with password `123`
- [ ] Create account with password `password`
- [ ] Verify weak passwords are accepted

### Mass Assignment Tests
- [ ] Try setting `role=ADMIN` during signup
- [ ] Verify you can create admin account
- [ ] Login and access `/admin` endpoint
- [ ] Verify privilege escalation works

---

## Exploitation Scenarios

### Scenario 1: SQL Injection Attack
1. Attacker navigates to login page
2. Enters SQL injection payload: `admin' OR '1'='1' --`
3. Enters any password (e.g., `test`)
4. Attacker is logged in as the first user (possibly admin)
5. Attacker gains unauthorized access to the application

**Impact:** Complete authentication bypass

### Scenario 2: Database Breach with Plain-Text Passwords
1. Attacker gains access to database (SQL injection, backup compromise, etc.)
2. All user passwords are immediately readable
3. Attacker can login as any user
4. If admin password is compromised, attacker has full access

**Impact:** Complete account compromise for all users

### Scenario 3: Brute Force Attack
1. Attacker identifies a valid username (user enumeration)
2. Attacker creates script to try thousands of common passwords
3. No rate limiting allows unlimited attempts
4. Weak passwords are easily cracked
5. Attacker gains access to user account

**Impact:** Account compromise through brute force

### Scenario 4: Privilege Escalation via Mass Assignment
1. Attacker creates new account
2. During signup, attacker sets `role=ADMIN` via form manipulation or proxy
3. Attacker now has admin privileges
4. Attacker can access `/admin` endpoint and view all user data

**Impact:** Unauthorized privilege escalation

---

## Remediation Priority

1. **CRITICAL:** Fix SQL Injection (authentication bypass)
2. **HIGH:** Implement password hashing (BCrypt)
3. **HIGH:** Implement rate limiting and account lockout
4. **MEDIUM:** Enforce password complexity
5. **MEDIUM:** Fix mass assignment vulnerability

---

## Additional Resources

- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [CWE-89: SQL Injection](https://cwe.mitre.org/data/definitions/89.html)
- [CWE-256: Plaintext Password Storage](https://cwe.mitre.org/data/definitions/256.html)


