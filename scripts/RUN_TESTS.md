# How to Run Automation Scripts

## Prerequisites

1. **Install Python requests library:**
   ```bash
   pip3 install requests
   ```

2. **Start the application:**
   ```bash
   # Make sure you're in the project directory
   cd /Users/saimsohail/Documents/cyber-security-project
   
   # Start the Spring Boot application
   mvn spring-boot:run
   ```
   
   Wait for the application to start (you'll see "Started VulnerableAppApplication" in the console).

## Running the Scripts

### Option 1: Run Individual Scripts

#### Test SQL Injection
```bash
python3 scripts/test_sql_injection.py
```

#### Test Brute Force Login
```bash
python3 scripts/bruteforce_login.py
```

#### Test Mass Assignment
```bash
python3 scripts/test_mass_assignment.py
```

### Option 2: Run All Scripts (One by One)

```bash
# Test all vulnerabilities
python3 scripts/test_sql_injection.py
python3 scripts/bruteforce_login.py
python3 scripts/test_mass_assignment.py
```

### Option 3: Run with Custom URL

If your app is running on a different port or URL:

```bash
python3 scripts/test_sql_injection.py --url http://localhost:8080
python3 scripts/bruteforce_login.py --url http://localhost:8080
python3 scripts/test_mass_assignment.py --url http://localhost:8080
```

### Option 4: Run with Verbose Output

To see detailed information:

```bash
python3 scripts/test_sql_injection.py --verbose
python3 scripts/bruteforce_login.py --verbose
python3 scripts/test_mass_assignment.py --verbose
```

## Testing Both Versions

### Step 1: Test Vulnerable Version (main branch)

```bash
# Switch to main branch (vulnerable version)
git checkout main

# Make sure app is running
mvn spring-boot:run

# In another terminal, run the scripts
python3 scripts/test_sql_injection.py
python3 scripts/bruteforce_login.py
python3 scripts/test_mass_assignment.py
```

**Expected Results:**
- ✅ SQL injection successful
- ✅ Multiple successful logins found
- ✅ Privilege escalation successful

### Step 2: Test Fixed Version (fixes branch)

```bash
# Switch to fixes branch (fixed version)
git checkout fixes

# Restart the application
mvn spring-boot:run

# In another terminal, run the scripts again
python3 scripts/test_sql_injection.py
python3 scripts/bruteforce_login.py
python3 scripts/test_mass_assignment.py
```

**Expected Results:**
- ❌ All SQL injection attempts fail
- ❌ All brute force attempts fail (passwords are hashed)
- ❌ Privilege escalation prevented

## Quick Start Guide

1. **Open Terminal 1** - Start the application:
   ```bash
   cd /Users/saimsohail/Documents/cyber-security-project
   mvn spring-boot:run
   ```

2. **Wait for app to start** (look for "Started VulnerableAppApplication")

3. **Open Terminal 2** - Run the scripts:
   ```bash
   cd /Users/saimsohail/Documents/cyber-security-project
   
   # Test SQL injection
   python3 scripts/test_sql_injection.py
   
   # Test brute force
   python3 scripts/bruteforce_login.py
   
   # Test mass assignment
   python3 scripts/test_mass_assignment.py
   ```

## Troubleshooting

### "Connection refused" or "Connection error"
- Make sure the application is running on port 8080
- Check: `curl http://localhost:8080`

### "ModuleNotFoundError: No module named 'requests'"
- Install requests: `pip3 install requests`

### Scripts run but find no vulnerabilities
- Make sure you're on the `main` branch (vulnerable version)
- Check that the application is actually running
- Try with `--verbose` flag to see detailed output

### Scripts show vulnerabilities on fixes branch
- This shouldn't happen if fixes are correct
- Verify you're on the `fixes` branch: `git branch`
- Make sure you restarted the app after switching branches

## Example Output

### Successful SQL Injection Test (Vulnerable Version)
```
============================================================
Automated SQL Injection Testing
============================================================
[✓ SUCCESS] SQL Injection successful!
  Payload: admin' OR '1'='1' --
[!] VULNERABILITY DETECTED: SQL Injection
```

### Failed SQL Injection Test (Fixed Version)
```
============================================================
Automated SQL Injection Testing
============================================================
[✗ FAIL] Payload: admin' OR '1'='1' --
[✓] No SQL injection vulnerabilities detected
```

