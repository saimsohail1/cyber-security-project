# Automation Script

This directory contains an automated testing script for demonstrating and verifying SQL injection vulnerability.

## Script

### `test_sql_injection.py`
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

## Requirements

Install required Python package:
```bash
pip3 install requests
```

## Running the Test

```bash
# On vulnerable branch (main)
git checkout main
mvn spring-boot:run  # Start app in one terminal
python3 scripts/test_sql_injection.py  # Run script in another terminal

# On fixed branch (fixes)
git checkout fixes
mvn spring-boot:run  # Start app in one terminal
python3 scripts/test_sql_injection.py  # Run script in another terminal
```

## How Tools Aided Analysis

1. **Automated Testing:** Scripts allow rapid testing of multiple attack vectors
2. **Reproducibility:** Same tests can be run before and after fixes
3. **Verification:** Confirms that security fixes are working correctly
4. **Documentation:** Provides evidence of vulnerabilities and their remediation

