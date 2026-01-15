# Automation & Tools

This document describes the automated testing scripts and static analysis tools used to identify, exploit, and verify security vulnerabilities in the application.

---

## Tools Used

### 1. Static Analysis: Semgrep

**Tool:** Semgrep v1.136.0  
**Configuration:** Auto (Community Rules)  
**Purpose:** Automated static code analysis to identify security vulnerabilities

**Results:**
- Scanned 6 Java files
- Executed 166 security rules
- **Identified 1 critical finding:** SQL Injection vulnerability

**Finding:**
- **Rule:** `java.lang.security.audit.formatted-sql-string.formatted-sql-string`
- **Location:** `VulnerableUserService.java:41`
- **Issue:** Formatted string in SQL statement (string concatenation)
- **CWE:** CWE-89 (SQL Injection)
- **OWASP:** A03:2021 - Injection

**How It Aided Analysis:**
- Automatically detected SQL injection pattern in code
- Provided exact file location and line number
- Confirmed vulnerability with CWE and OWASP mappings
- Validated that the fix removed the vulnerable pattern

---

### 2. Automated Exploit Scripts

Three Python scripts were developed to automate vulnerability testing:

#### 2.1 SQL Injection Testing Script (`scripts/test_sql_injection.py`)

**Purpose:** Automate SQL injection exploit attempts

**What It Tests:**
- SQL injection vulnerability in login endpoint
- Authentication bypass attempts
- Input validation issues

**Payloads Tested:**
- `admin' OR '1'='1' --`
- `' OR '1'='1' --`
- `admin'--`
- And other common SQL injection patterns

**Results:**
- **Vulnerable Version (main branch):** SQL injection successful, authentication bypassed
- **Fixed Version (fixes branch):** All SQL injection attempts fail (parameterized queries prevent injection)

**Note:** The test script was improved to fix a false positive detection bug. It now correctly checks only the URL for `/dashboard` instead of searching response text (which caused false positives due to "Welcome Back" text on the login page).

**Demonstrates:**
- ✅ Automated SQL injection testing
- ✅ Authentication bypass vulnerability
- ✅ Verification that fixes prevent SQL injection

---

#### 2.2 Mass Assignment Testing Script (`scripts/test_mass_assignment.py`)

**Purpose:** Automate mass assignment / privilege escalation testing

**What It Tests:**
- Mass assignment vulnerability in signup endpoint
- Privilege escalation via role parameter manipulation
- Broken access control

**Attack Method:**
- Attempts to signup with `role=ADMIN` parameter
- Verifies if admin panel access is granted
- Checks if user was created with ADMIN privileges

**Results:**
- **Vulnerable Version (main branch):** Role parameter accepted, admin access granted
- **Fixed Version (fixes branch):** Role parameter ignored, user created with USER role only

**Demonstrates:**
- ✅ Automated privilege escalation testing
- ✅ Mass assignment vulnerability
- ✅ Verification that fixes prevent privilege escalation

---

#### 2.3 Broken Authentication Testing Script (`scripts/test_broken_authentication.py`)

**Purpose:** Automate broken authentication vulnerability testing

**What It Tests:**
- Plain-text password storage vulnerability
- Password hashing verification (BCrypt)

**Attack Method:**
- Accesses admin panel to check password storage format
- Verifies if passwords are stored in plain text or hashed
- Checks for BCrypt hash patterns

**Results:**
- **Vulnerable Version (main branch):** Passwords stored in plain text
- **Fixed Version (fixes branch):** Passwords hashed using BCrypt

**Demonstrates:**
- ✅ Automated password storage security testing
- ✅ Broken authentication vulnerability
- ✅ Verification that fixes implement proper password hashing

---

## How Tools Aided Analysis

### 1. **Detection Phase**
- **Semgrep:** Automatically identified SQL injection pattern in code
- **Static Analysis:** Found vulnerability without running the application
- **Code Review:** Provided exact locations for manual inspection

### 2. **Exploitation Phase**
- **Automated Scripts:** Rapidly tested multiple attack vectors across all three vulnerabilities
- **Reproducibility:** Same attacks could be executed consistently
- **Documentation:** Scripts provide clear evidence of vulnerabilities
- **Comprehensive Coverage:** Tests SQL injection, mass assignment, and broken authentication

### 3. **Verification Phase**
- **Before/After Comparison:** Scripts run on both vulnerable and fixed versions
- **Proof of Fix:** Automated tests confirm vulnerabilities are resolved
- **Regression Testing:** Ensures fixes don't break functionality

### 4. **Efficiency**
- **Time Savings:** Automated testing is faster than manual testing
- **Comprehensive Coverage:** Tests multiple attack vectors systematically
- **Repeatability:** Same tests can be run multiple times with consistent results

---

## Usage Examples

### Testing Vulnerable Version (main branch)
```bash
git checkout main
mvn spring-boot:run  # Start app

# Run all tests in another terminal
python3 scripts/test_sql_injection.py
python3 scripts/test_mass_assignment.py
python3 scripts/test_broken_authentication.py
```

**Expected Output:**
- SQL injection successful, authentication bypassed
- Mass assignment successful, admin access granted
- Passwords stored in plain text

### Testing Fixed Version (fixes branch)
```bash
git checkout fixes
mvn spring-boot:run  # Start app

# Run all tests in another terminal
python3 scripts/test_sql_injection.py
python3 scripts/test_mass_assignment.py
python3 scripts/test_broken_authentication.py
```

**Expected Output:**
- All SQL injection attempts fail, authentication properly protected
- Mass assignment blocked, role parameter ignored
- Passwords hashed using BCrypt

---

## Tool Output Examples

### Semgrep Output
```
┌────────────────┐
│ 1 Code Finding │
└────────────────┘
src/main/java/com/cybersecurity/vulnerableapp/service/VulnerableUserService.java
❯❯❱ java.lang.security.audit.formatted-sql-string.formatted-sql-string
   Detected a formatted string in a SQL statement...
   41┆ Query query = entityManager.createNativeQuery(sql, User.class);
```

### SQL Injection Script Output

**Vulnerable Version (main branch):**
```
============================================================
Automated SQL Injection Testing
============================================================
[✓ SUCCESS] SQL Injection successful!
  Payload: admin' OR '1'='1' --
[!] VULNERABILITY DETECTED: SQL Injection
```

**Fixed Version (fixes branch):**
```
============================================================
Automated SQL Injection Testing
============================================================
[✗ FAIL] Payload: admin' OR '1'='1' --
[✗ FAIL] Payload: ' OR '1'='1' --
...
[✓] No SQL injection vulnerabilities detected
    This indicates:
    - Input is properly parameterized
    - SQL injection attacks are prevented
    - Security fixes are working correctly
```

---

## Summary

The combination of **Semgrep static analysis** and **three automated testing scripts** provided:

1. **Automated Detection:** Semgrep identified SQL injection vulnerability in code
2. **Automated Exploitation:** Scripts demonstrated vulnerabilities in action:
   - SQL injection (authentication bypass)
   - Mass assignment (privilege escalation)
   - Broken authentication (plain-text passwords)
3. **Automated Verification:** Scripts confirmed fixes prevent all three vulnerabilities
4. **Clear Documentation:** Tool outputs provide evidence for reports

This approach demonstrates both **detection** (finding vulnerabilities) and **verification** (confirming fixes) through automation, satisfying the project requirements for tool usage and automation.

---

**Script Locations:**
- `scripts/test_sql_injection.py` - SQL injection testing
- `scripts/test_mass_assignment.py` - Mass assignment / privilege escalation testing
- `scripts/test_broken_authentication.py` - Broken authentication / password hashing testing

**Static Analysis Tool:** Semgrep (community rules)  
**Language:** Python 3 with `requests` library

