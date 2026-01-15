#!/usr/bin/env python3
"""
Automated SQL Injection Testing Script

This script demonstrates:
- Automated SQL injection exploit attempts
- Testing for SQL injection vulnerability in login
- Verification of security fixes

Usage:
    python3 scripts/test_sql_injection.py [--url URL]
    
Example:
    python3 scripts/test_sql_injection.py --url http://localhost:8080
"""

import requests
import sys
import argparse

def test_sql_injection(url, payload, verbose=False):
    """
    Test SQL injection payload against login endpoint
    
    Args:
        url: Base URL of the application
        payload: SQL injection payload to test
        verbose: Print detailed output
    
    Returns:
        True if SQL injection successful (bypasses authentication), False otherwise
    """
    login_url = f"{url}/login"
    
    try:
        response = requests.post(
            login_url,
            data={
                "username": payload,
                "password": "anything"  # Password doesn't matter for SQL injection
            },
            allow_redirects=True,
            timeout=5
        )
        
        # Check if redirected to dashboard URL (most reliable indicator of successful auth)
        # The URL should contain "/dashboard" for successful authentication
        # NOTE: We don't check for "welcome" in text because the login page 
        # contains "Welcome Back", which causes false positives
        if "/dashboard" in response.url:
            return True
        
        return False
    except requests.exceptions.RequestException as e:
        if verbose:
            print(f"  [ERROR] Request failed: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description='Automated SQL injection testing')
    parser.add_argument('--url', default='http://localhost:8080', 
                       help='Base URL of the application (default: http://localhost:8080)')
    parser.add_argument('--verbose', '-v', action='store_true',
                       help='Print detailed output')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("Automated SQL Injection Testing")
    print("=" * 60)
    print(f"Target URL: {args.url}")
    print("-" * 60)
    
    # Common SQL injection payloads
    sql_payloads = [
        ("admin' OR '1'='1' --", "Classic SQL injection - always true condition"),
        ("' OR '1'='1' --", "SQL injection without username"),
        ("admin'--", "SQL injection with comment"),
        ("' OR '1'='1", "SQL injection without comment"),
        ("admin' OR '1'='1' OR '1'='1", "Multiple OR conditions"),
    ]
    
    successful_injections = []
    
    print("\nTesting SQL injection payloads...\n")
    
    for payload, description in sql_payloads:
        if args.verbose:
            print(f"Testing: {payload}")
            print(f"  Description: {description}")
        
        if test_sql_injection(args.url, payload, args.verbose):
            print(f"[✓ SUCCESS] SQL Injection successful!")
            print(f"  Payload: {payload}")
            print(f"  Description: {description}")
            successful_injections.append((payload, description))
        else:
            if args.verbose:
                print(f"[✗ FAIL] SQL injection blocked or failed")
            print(f"[✗ FAIL] Payload: {payload}")
        print()
    
    print("=" * 60)
    print("Test Summary")
    print("=" * 60)
    print(f"Total payloads tested: {len(sql_payloads)}")
    print(f"Successful SQL injections: {len(successful_injections)}")
    
    if successful_injections:
        print("\n[!] VULNERABILITY DETECTED: SQL Injection")
        print("    Successful payloads:")
        for payload, description in successful_injections:
            print(f"    - {payload}")
            print(f"      {description}")
        print("\n[!] This demonstrates:")
        print("    - Authentication can be bypassed")
        print("    - User input is not properly sanitized")
        print("    - SQL queries are vulnerable to injection")
        return 1
    else:
        print("\n[✓] No SQL injection vulnerabilities detected")
        print("    This indicates:")
        print("    - Input is properly parameterized")
        print("    - SQL injection attacks are prevented")
        print("    - Security fixes are working correctly")
        return 0

if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\n\n[!] Test interrupted by user")
        sys.exit(130)

