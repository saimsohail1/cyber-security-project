# Automation Scripts

This directory contains automated testing scripts for demonstrating and verifying security vulnerabilities.

## Scripts

### 1. `test_sql_injection.py`
**Purpose:** Automated SQL injection testing

**Demonstrates:**
- SQL Injection vulnerability in login
- Authentication bypass attempts
- Input validation issues

**Usage:**
```bash
# Test vulnerable version
python3 scripts/test_sql_injection.py

# Test with custom URL
python3 scripts/test_sql_injection.py --url http://localhost:8080

# Verbose output
python3 scripts/test_sql_injection.py --verbose
```

**Expected Results:**
- **Vulnerable version:** SQL injection successful, authentication bypassed
- **Fixed version:** All SQL injection attempts fail

---

### 2. `test_mass_assignment.py`
**Purpose:** Automated mass assignment / privilege escalation testing

**Demonstrates:**
- Mass assignment vulnerability in signup
- Privilege escalation via role parameter manipulation
- Broken access control

**Usage:**
```bash
# Test vulnerable version
python3 scripts/test_mass_assignment.py

# Test with custom URL
python3 scripts/test_mass_assignment.py --url http://localhost:8080

# Verbose output
python3 scripts/test_mass_assignment.py --verbose
```

**Expected Results:**
- **Vulnerable version:** Role=ADMIN parameter accepted, admin access granted
- **Fixed version:** Role parameter ignored, user created with USER role only

---

### 3. `test_broken_authentication.py`
**Purpose:** Automated broken authentication testing

**Demonstrates:**
- Plain-text password storage vulnerability
- Password hashing verification

**Usage:**
```bash
# Test vulnerable version
python3 scripts/test_broken_authentication.py

# Test with custom URL
python3 scripts/test_broken_authentication.py --url http://localhost:8080

# Verbose output
python3 scripts/test_broken_authentication.py --verbose
```

**Expected Results:**
- **Vulnerable version:** Passwords in plain text
- **Fixed version:** Passwords hashed (BCrypt)

---

## Requirements

Install required Python package:
```bash
pip3 install requests
```

## Running the Tests

### Test All Vulnerabilities

```bash
# On vulnerable branch (main)
git checkout main
mvn spring-boot:run  # Start app in one terminal

# Run all tests in another terminal
python3 scripts/test_sql_injection.py
python3 scripts/test_mass_assignment.py
python3 scripts/test_broken_authentication.py

# On fixed branch (fixes)
git checkout fixes
mvn spring-boot:run  # Start app in one terminal

# Run all tests in another terminal
python3 scripts/test_sql_injection.py
python3 scripts/test_mass_assignment.py
python3 scripts/test_broken_authentication.py
```

### Test Individual Vulnerabilities

```bash
# SQL Injection
python3 scripts/test_sql_injection.py

# Mass Assignment
python3 scripts/test_mass_assignment.py

# Broken Authentication
python3 scripts/test_broken_authentication.py
```

## How Tools Aided Analysis

1. **Automated Testing:** Scripts allow rapid testing of multiple attack vectors
2. **Reproducibility:** Same tests can be run before and after fixes
3. **Verification:** Confirms that security fixes are working correctly
4. **Documentation:** Provides evidence of vulnerabilities and their remediation

