# Semgrep Static Analysis Report

**Analysis Date:** January 2026  
**Tool:** Semgrep v1.136.0  
**Configuration:** `auto` (Community Rules)  
**Target:** Java source code in `src/main/java/`

---

## Executive Summary

Semgrep successfully scanned **6 Java files** using **166 security rules** and identified **1 critical security finding**.

| Metric | Value |
|--------|-------|
| Files Scanned | 6 |
| Rules Executed | 166 |
| Findings | 1 (1 blocking) |
| Parse Success Rate | ~100% |
| Language | Java |

---

## Scan Configuration

**Command Used:**
```bash
semgrep --config=auto src/main/java/
```

**Rules Applied:**
- Java-specific security rules: 118 rules
- Multi-language security rules: 48 rules
- Total rules analyzed: 1,063 rules
- Rules matched against codebase: 166 rules

**Files Analyzed:**
1. `src/main/java/com/cybersecurity/vulnerableapp/VulnerableAppApplication.java`
2. `src/main/java/com/cybersecurity/vulnerableapp/controller/AuthController.java`
3. `src/main/java/com/cybersecurity/vulnerableapp/controller/DashboardController.java`
4. `src/main/java/com/cybersecurity/vulnerableapp/model/User.java`
5. `src/main/java/com/cybersecurity/vulnerableapp/repository/UserRepository.java`
6. `src/main/java/com/cybersecurity/vulnerableapp/service/VulnerableUserService.java`

---

## Findings

### Finding 1: SQL Injection (Formatted SQL String)

**Severity:** ❌ **ERROR** (Blocking)  
**Rule ID:** `java.lang.security.audit.formatted-sql-string.formatted-sql-string`  
**CWE:** CWE-89: Improper Neutralization of Special Elements used in an SQL Command ('SQL Injection')  
**OWASP:** 
- A01:2017 - Injection
- A03:2021 - Injection

**Location:**
- **File:** `src/main/java/com/cybersecurity/vulnerableapp/service/VulnerableUserService.java`
- **Line:** 41
- **Column:** 23-71
- **Code:**
```java
Query query = entityManager.createNativeQuery(sql, User.class);
```

**Issue Description:**
Semgrep detected a formatted string being used in a SQL statement. The variable `sql` is constructed using string concatenation with user input, which could lead to SQL injection if variables are not properly sanitized.

**Vulnerable Code Context:**
```java
36:    public User authenticateUser(String username, String password) {
37:        // VULNERABLE: Building SQL query with string concatenation
38:        String sql = "SELECT * FROM users WHERE username = '" + username 
39:                    + "' AND password = '" + password + "'";
40:        
41:        Query query = entityManager.createNativeQuery(sql, User.class);
42:        
43:        @SuppressWarnings("unchecked")
44:        List<User> users = query.getResultList();
45:        ...
46:    }
```

**Root Cause:**
- Line 38-39: String concatenation directly inserts user input (`username` and `password`) into SQL query
- Line 41: The concatenated string is passed to `createNativeQuery()` without parameterization

**Risk Assessment:**
- **Likelihood:** HIGH
- **Impact:** MEDIUM
- **Confidence:** MEDIUM
- **CWE Top 25:** ✅ Yes (2021 & 2022)

**Recommendation:**
Use prepared statements (`java.sql.PreparedStatement`) instead of string concatenation. Obtain a PreparedStatement using `connection.prepareStatement()` or use JPA repository methods with parameterized queries.

**References:**
- [OWASP SQL Injection Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)
- [Oracle Java PreparedStatement Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html#create_ps)
- [SANS: Fixing SQL Injection in Java](https://software-security.sans.org/developer-how-to/fix-sql-injection-in-java-using-prepared-callable-statement)
- [Semgrep Rule Details](https://sg.run/OPXp)

**ASVS Mapping:**
- **Control:** 5.3.5 Injection
- **Section:** V5: Validation, Sanitization and Encoding Verification Requirements
- **Version:** ASVS 4.0

---

## Additional Notes

### Why Other Vulnerabilities Were Not Detected

Semgrep successfully identified the SQL injection vulnerability (Vulnerability 1) because it matches a well-defined pattern: formatted strings in SQL queries.

However, the following vulnerabilities were not detected by automated static analysis:

1. **Plain-Text Password Storage (Vulnerability 2):**
   - **Reason:** This is a logic vulnerability rather than a code pattern
   - Semgrep would need custom rules to detect that passwords aren't being hashed
   - Static analysis tools typically focus on code patterns, not business logic

2. **Mass Assignment (Vulnerability 3):**
   - **Reason:** Accepting user input parameters is a common pattern in Spring controllers
   - The vulnerability is in the business logic (allowing role to be set)
   - Would require semantic analysis to understand intent

**Conclusion:** Manual code review is still necessary to identify logic-based vulnerabilities that automated tools may miss.

---

## Recommendations

### Immediate Actions
1. ✅ **Fix SQL Injection** - Replace string concatenation with parameterized queries (Detected by Semgrep)
2. ⚠️ **Review Password Handling** - Implement password hashing (Requires manual review)
3. ⚠️ **Review Access Control** - Fix Mass Assignment vulnerability (Requires manual review)

### Best Practices
1. Run Semgrep in CI/CD pipeline to catch issues early
2. Use multiple static analysis tools for comprehensive coverage
3. Combine automated tools with manual code review
4. Regular security audits and penetration testing

### Tool Configuration
Consider using additional Semgrep configurations:
- `p/java` - Java-specific security rules
- `p/security-audit` - Security-focused rules
- Custom rules for application-specific patterns

---

## Scan Metadata

**Semgrep Version:** 1.136.0  
**Engine:** OSS (Open Source)  
**Config Time:** 6.45 seconds  
**Core Time:** 5.26 seconds  
**Scanning Time:** 0.71 seconds  
**Total Time:** 11.73 seconds  
**Max Memory:** ~1.1 GB  

---

## Conclusion

Semgrep successfully identified the critical SQL injection vulnerability in the codebase. This demonstrates the value of automated static analysis tools in identifying common security issues. However, the tool did not detect all vulnerabilities, highlighting the importance of combining automated tools with manual code review and security testing.

**Finding Summary:**
- ✅ SQL Injection: **Detected** by Semgrep
- ⚠️ Plain-Text Passwords: **Not detected** (requires manual review)
- ⚠️ Mass Assignment: **Not detected** (requires manual review)

**Overall Assessment:** Static analysis tools like Semgrep are excellent for detecting pattern-based vulnerabilities but should be used in combination with other security practices for comprehensive coverage.

---

**Report Generated:** January 2026  
**Tool:** Semgrep v1.136.0  
**Configuration:** Auto (Community Rules)

