# Vulnerable Cybersecurity Demo Application

⚠️ **WARNING: This application is intentionally insecure and designed for educational purposes only. DO NOT use this in production or expose it to the internet.**

## Project Overview

This is a deliberately vulnerable Spring Boot application designed to demonstrate common cybersecurity vulnerabilities for educational purposes. The application includes authentication, user management, and protected endpoints, all with intentional security flaws.

## Features

- User registration (signup)
- User authentication (login)
- User dashboard
- Admin panel (protected endpoint)
- H2 in-memory database
- Thymeleaf web templates

## Vulnerabilities Demonstrated

### 🔴 Vulnerability 1: SQL Injection (REQUIRED)

**Location:** `VulnerableUserService.authenticateUser()`

**Description:** The login authentication uses string concatenation to build SQL queries, making it vulnerable to SQL injection attacks.

**Vulnerable Code:**
```java
String sql = "SELECT * FROM users WHERE username = '" + username 
            + "' AND password = '" + password + "'";
Query query = entityManager.createNativeQuery(sql, User.class);
```

**Attack Example:**
```
Username: admin' OR '1'='1' --
Password: anything
```

**How It Works:**
The malicious input causes the SQL query to become:
```sql
SELECT * FROM users WHERE username = 'admin' OR '1'='1' --' AND password = 'anything'
```

This bypasses authentication because:
- `'1'='1'` is always true
- `--` comments out the rest of the query
- The query returns the first user in the database

**Demonstrations:**
1. **Authentication Bypass:** Login without valid credentials
2. **Data Leakage:** Access user accounts by exploiting SQL injection
3. **User Enumeration:** Determine if usernames exist in the database

**Fix:**
- Use parameterized queries with `PreparedStatement`
- Use JPA repository methods instead of native queries
- Use `@Query` with parameterized bindings
- Example fix:
```java
@Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password")
User findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
```

### 🔴 Vulnerability 2: Broken Authentication

**Location:** Multiple locations in the application

**Description:** Multiple authentication-related vulnerabilities that compromise the security of user accounts.

#### 2.1 Plain-Text Password Storage

**Location:** `User` entity, `VulnerableUserService.createUser()`

**Description:** Passwords are stored in plain text in the database, making them immediately readable if the database is compromised.

**Vulnerable Code:**
```java
// VULNERABLE: Storing password in plain text
user.setPassword(password);
```

**Attack Scenarios:**
1. Database breach exposes all passwords immediately
2. Admins can see all user passwords in plain text
3. No protection if database backup is compromised

**Fix:**
- Use BCrypt, Argon2, or PBKDF2 for password hashing
- Example fix:
```java
@Autowired
private BCryptPasswordEncoder passwordEncoder;

user.setPassword(passwordEncoder.encode(password));
```

#### 2.2 No Rate Limiting

**Location:** `AuthController.login()`

**Description:** No rate limiting on login attempts, allowing brute force attacks.

**Attack Scenarios:**
1. Attackers can try unlimited password combinations
2. Automated scripts can brute force weak passwords
3. No protection against credential stuffing attacks

**Fix:**
- Implement rate limiting using Spring Security Rate Limiter
- Lock accounts after N failed attempts
- Use CAPTCHA after multiple failed attempts
- Example: Use `@RateLimiter` annotation or Spring Security's rate limiting features

#### 2.3 No Account Lockout

**Location:** `AuthController.login()`

**Description:** Failed login attempts are tracked but accounts are never locked, allowing unlimited brute force attempts.

**Fix:**
- Lock accounts after X failed attempts (e.g., 5 attempts)
- Implement temporary lockout (e.g., 15 minutes)
- Require email verification to unlock

#### 2.4 No Password Complexity Requirements

**Location:** `AuthController.signup()`

**Description:** Users can create accounts with weak passwords like "123" or "password".

**Fix:**
- Enforce minimum password length (e.g., 12 characters)
- Require uppercase, lowercase, numbers, and special characters
- Use password strength validation
- Consider using password breach detection (Have I Been Pwned API)

### 🟡 Additional Vulnerability: Mass Assignment Risk

**Location:** `AuthController.signup()`, `VulnerableUserService.createUser()`

**Description:** Users can set their own role during signup, potentially escalating privileges.

**Attack Example:**
```
During signup, set role parameter to "ADMIN"
```

**Fix:**
- Always set role on the server side
- Use DTOs to control which fields can be set
- Ignore role field in signup requests using `@JsonIgnore` or validation

## How to Run

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone or download this project
2. Navigate to the project directory
3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

5. Open your browser and navigate to:
```
http://localhost:8080
```

### Access H2 Database Console

1. Navigate to: `http://localhost:8080/h2-console`
2. Use these credentials:
   - JDBC URL: `jdbc:h2:mem:vulnerabledb`
   - Username: `sa`
   - Password: (leave empty)

## Testing the Vulnerabilities

### Testing SQL Injection

1. Start the application
2. Navigate to the login page
3. Try logging in with:
   - Username: `admin' OR '1'='1' --`
   - Password: `anything`
4. You should be logged in as the first user in the database

**Other SQL Injection Payloads to Try:**
- `' OR '1'='1`
- `admin'--`
- `' UNION SELECT * FROM users --`
- `'; DROP TABLE users; --` (this won't work with H2, but demonstrates the risk)

### Testing Broken Authentication

1. **Plain-Text Passwords:**
   - Sign up for a new account
   - Log in as admin (if you have access)
   - Go to `/admin` endpoint
   - Observe that all passwords are visible in plain text

2. **No Rate Limiting:**
   - Try logging in with wrong credentials multiple times
   - Notice there's no account lockout
   - Use a script to try many password combinations

3. **Weak Passwords:**
   - Sign up with a very weak password like "123"
   - Notice it's accepted without validation

4. **Mass Assignment:**
   - During signup, add a hidden form field or use a tool like Burp Suite to send:
     ```
     POST /signup
     username=testuser&email=test@example.com&password=test123&role=ADMIN
     ```
   - Verify you can create an admin account

## Demo Users

The application comes with pre-loaded demo users (see `data.sql`):

- **user1** / **password123** (USER role)
- **alice** / **alice123** (USER role)
- **admin** / **admin** (ADMIN role)

⚠️ **Note:** These are intentionally weak passwords for demonstration purposes.

## Project Structure

```
cyber-security-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cybersecurity/vulnerableapp/
│   │   │       ├── VulnerableAppApplication.java
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java      # Login, signup endpoints
│   │   │       │   └── DashboardController.java # Dashboard, admin endpoints
│   │   │       ├── model/
│   │   │       │   └── User.java                # User entity
│   │   │       ├── repository/
│   │   │       │   └── UserRepository.java      # JPA repository
│   │   │       └── service/
│   │   │           └── VulnerableUserService.java # Vulnerable service layer
│   │   └── resources/
│   │       ├── application.properties           # App configuration
│   │       ├── data.sql                         # Demo data
│   │       └── templates/
│   │           ├── login.html
│   │           ├── signup.html
│   │           ├── dashboard.html
│   │           └── admin.html
│   └── test/
├── pom.xml                                      # Maven dependencies
└── README.md                                    # This file
```

## Security Recommendations

To fix the vulnerabilities in this application:

1. **SQL Injection:**
   - Replace string concatenation with parameterized queries
   - Use JPA methods or `@Query` with proper parameter binding

2. **Broken Authentication:**
   - Implement BCrypt password hashing
   - Add rate limiting for login attempts
   - Implement account lockout after failed attempts
   - Enforce password complexity rules
   - Use HTTPS in production
   - Implement proper session management

3. **Mass Assignment:**
   - Use DTOs to control which fields users can set
   - Always set sensitive fields (like role) on the server side

4. **General:**
   - Use Spring Security for authentication and authorization
   - Implement proper logging and monitoring
   - Use HTTPS in production
   - Regular security audits and penetration testing

## Educational Purpose

This application is created solely for educational purposes to:
- Demonstrate common web application vulnerabilities
- Show how SQL injection attacks work
- Illustrate broken authentication patterns
- Provide hands-on experience with security testing
- Understand how to fix security vulnerabilities

## Disclaimer

⚠️ **This application is intentionally insecure. Do not:**
- Deploy this to a public server
- Use real credentials or personal information
- Use this as a reference for secure code
- Expose this application to the internet

## License

This project is provided for educational purposes only.

## Author

Created for cybersecurity education and demonstration purposes.

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP SQL Injection](https://owasp.org/www-community/attacks/SQL_Injection)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [CWE-89: SQL Injection](https://cwe.mitre.org/data/definitions/89.html)
- [CWE-256: Plaintext Storage of Password](https://cwe.mitre.org/data/definitions/256.html)


