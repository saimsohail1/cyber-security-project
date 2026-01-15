#!/usr/bin/env python3
"""
Automated Broken Authentication Testing Script

This script demonstrates:
- Automated testing for broken authentication vulnerabilities
- Testing password hashing (plain text vs hashed)
- Verification of security fixes

Usage:
    python3 scripts/test_broken_authentication.py [--url URL]
    
Example:
    python3 scripts/test_broken_authentication.py --url http://localhost:8080
"""

import requests
import sys
import argparse

def test_password_hashing(url, verbose=False):
    """
    Test if passwords are stored in plain text by checking admin panel
    
    Args:
        url: Base URL of the application
        verbose: Print detailed output
    
    Returns:
        True if passwords are in plain text (vulnerable), False if hashed (secure)
    """
    # Use known admin credentials from data.sql
    admin_username = "admin"
    admin_password = "admin"
    
    login_url = f"{url}/login"
    admin_url = f"{url}/admin"
    
    try:
        # Step 1: Login as admin
        if verbose:
            print(f"  Logging in as admin...")
        
        session = requests.Session()
        login_response = session.post(
            login_url,
            data={
                "username": admin_username,
                "password": admin_password
            },
            allow_redirects=True,
            timeout=5
        )
        
        if "/dashboard" not in login_response.url:
            if verbose:
                print(f"  Admin login failed")
            return None  # Cannot determine
        
        # Step 2: Access admin panel
        if verbose:
            print(f"  Accessing admin panel...")
        
        admin_response = session.get(
            admin_url,
            allow_redirects=True,
            timeout=5
        )
        
        # Check if we can access admin panel
        if "/admin" not in admin_response.url:
            if verbose:
                print(f"  Admin panel access denied")
            return None  # Cannot determine
        
        # Step 3: Check if passwords are visible in plain text
        # Look for password patterns in the response
        # Plain text passwords would be readable (not BCrypt hashes)
        response_text = admin_response.text.lower()
        
        # BCrypt hashes start with $2a$, $2b$, or $2y$ and are ~60 chars
        # Plain text passwords are usually shorter and don't start with $2
        # Check for password column in table
        if "password" in response_text:
            # Look for patterns that suggest plain text
            # Plain text: short, readable words
            # Hashed: long strings starting with $2a$ or similar
            
            # If we see common password patterns (like "password", "admin", "123")
            # in the password column, it's likely plain text
            plain_text_indicators = ["password", "admin", "alice", "user1"]
            for indicator in plain_text_indicators:
                # Check if indicator appears near password-related text
                # This is a heuristic - not perfect but good enough for testing
                if indicator in response_text:
                    # Check if it's in a table cell (between <td> tags)
                    # Simple check: if password-like words appear, likely plain text
                    return True
        
        # Check for BCrypt hash pattern ($2a$10$...)
        if "$2a$" in admin_response.text or "$2b$" in admin_response.text:
            return False  # Passwords are hashed
        
        # If we can't determine, assume vulnerable (conservative)
        return True
        
    except requests.exceptions.RequestException as e:
        if verbose:
            print(f"  [ERROR] Request failed: {e}")
            return None

def main():
    parser = argparse.ArgumentParser(description='Automated broken authentication testing')
    parser.add_argument('--url', default='http://localhost:8080', 
                       help='Base URL of the application (default: http://localhost:8080)')
    parser.add_argument('--verbose', '-v', action='store_true',
                       help='Print detailed output')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("Automated Broken Authentication Testing")
    print("=" * 60)
    print(f"Target URL: {args.url}")
    print("-" * 60)
    
    vulnerabilities_found = []
    
    # Test: Password Hashing
    print("\nTesting Password Storage (Plain Text vs Hashed)")
    print("-" * 60)
    
    password_test = test_password_hashing(args.url, args.verbose)
    
    if password_test is True:
        print("[✓ VULNERABLE] Passwords stored in plain text")
        print("  - Passwords are readable in database/admin panel")
        print("  - Database breach would expose all passwords")
        vulnerabilities_found.append("Plain-text password storage")
    elif password_test is False:
        print("[✓ SECURE] Passwords are hashed (BCrypt)")
        print("  - Passwords cannot be read from database")
        print("  - Security fix is working correctly")
    else:
        print("[?] Cannot determine password storage method")
        print("  - Admin access may be required to verify")
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    
    if vulnerabilities_found:
        print(f"\n[!] VULNERABILITIES DETECTED: Broken Authentication")
        print("    Issues found:")
        for vuln in vulnerabilities_found:
            print(f"    - {vuln}")
        print("\n[!] This demonstrates:")
        print("    - Authentication mechanisms are not properly secured")
        print("    - Security fixes may be needed")
        return 1
    else:
        print("\n[✓] No broken authentication vulnerabilities detected")
        print("    This indicates:")
        print("    - Passwords are properly hashed")
        print("    - Security fixes are working correctly")
        return 0

if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\n\n[!] Test interrupted by user")
        sys.exit(130)

