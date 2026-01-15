#!/usr/bin/env python3
"""
Automated Mass Assignment Testing Script

This script demonstrates:
- Automated mass assignment exploit attempts
- Testing for privilege escalation via role parameter manipulation
- Verification of security fixes

Usage:
    python3 scripts/test_mass_assignment.py [--url URL]
    
Example:
    python3 scripts/test_mass_assignment.py --url http://localhost:8080
"""

import requests
import sys
import argparse
import time
import random
import string

def generate_random_username():
    """Generate a random username for testing"""
    return f"test_user_{''.join(random.choices(string.ascii_lowercase + string.digits, k=6))}"

def test_mass_assignment(url, verbose=False):
    """
    Test mass assignment vulnerability by trying to set role=ADMIN during signup
    
    Args:
        url: Base URL of the application
        verbose: Print detailed output
    
    Returns:
        True if mass assignment successful (role=ADMIN accepted), False otherwise
    """
    signup_url = f"{url}/signup"
    login_url = f"{url}/login"
    admin_url = f"{url}/admin"
    
    # Generate unique test credentials
    test_username = generate_random_username()
    test_email = f"{test_username}@test.com"
    test_password = "testpass123"
    
    try:
        # Step 1: Try to signup with role=ADMIN parameter
        if verbose:
            print(f"  Attempting signup with role=ADMIN...")
        
        signup_response = requests.post(
            signup_url,
            data={
                "username": test_username,
                "email": test_email,
                "password": test_password,
                "role": "ADMIN"  # Mass assignment attempt
            },
            allow_redirects=True,
            timeout=5
        )
        
        # Check if signup was successful
        if "error=exists" in signup_response.url or "error" in signup_response.url:
            if verbose:
                print(f"  Signup failed (user may already exist)")
            return False
        
        # Step 2: Login with the created account
        if verbose:
            print(f"  Logging in with created account...")
        
        session = requests.Session()
        login_response = session.post(
            login_url,
            data={
                "username": test_username,
                "password": test_password
            },
            allow_redirects=True,
            timeout=5
        )
        
        # Check if login was successful
        if "/dashboard" not in login_response.url:
            if verbose:
                print(f"  Login failed")
            return False
        
        # Step 3: Try to access admin panel
        if verbose:
            print(f"  Attempting to access admin panel...")
        
        admin_response = session.get(
            admin_url,
            allow_redirects=True,
            timeout=5
        )
        
        # Check if we can access admin panel (mass assignment successful)
        # Admin panel should show "Admin Panel" in the title or content
        if "/admin" in admin_response.url and "admin" in admin_response.text.lower():
            # Check for admin panel indicators
            admin_indicators = ["admin panel", "all users", "sensitive user data"]
            for indicator in admin_indicators:
                if indicator in admin_response.text.lower():
                    return True
        
        # Alternative check: Check dashboard for admin role badge
        dashboard_response = session.get(
            f"{url}/dashboard",
            allow_redirects=True,
            timeout=5
        )
        
        # Check if role is ADMIN in dashboard
        if "ADMIN" in dashboard_response.text and "badge-admin" in dashboard_response.text:
            return True
        
        return False
        
    except requests.exceptions.RequestException as e:
        if verbose:
            print(f"  [ERROR] Request failed: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description='Automated mass assignment testing')
    parser.add_argument('--url', default='http://localhost:8080', 
                       help='Base URL of the application (default: http://localhost:8080)')
    parser.add_argument('--verbose', '-v', action='store_true',
                       help='Print detailed output')
    
    args = parser.parse_args()
    
    print("=" * 60)
    print("Automated Mass Assignment Testing")
    print("=" * 60)
    print(f"Target URL: {args.url}")
    print("-" * 60)
    
    print("\nTesting mass assignment vulnerability...")
    print("Attempt: Signup with role=ADMIN parameter\n")
    
    if test_mass_assignment(args.url, args.verbose):
        print("[✓ SUCCESS] Mass Assignment successful!")
        print("  - Role parameter was accepted during signup")
        print("  - User was created with ADMIN privileges")
        print("  - Admin panel access granted")
        print("\n[!] VULNERABILITY DETECTED: Mass Assignment / Broken Access Control")
        print("    This demonstrates:")
        print("    - Users can escalate privileges during registration")
        print("    - Role parameter is not properly validated")
        print("    - Privilege escalation attack successful")
        return 1
    else:
        print("[✗ FAIL] Mass assignment blocked or failed")
        print("  - Role parameter was ignored or rejected")
        print("  - User was created with USER role (default)")
        print("  - Admin panel access denied")
        print("\n[✓] No mass assignment vulnerabilities detected")
        print("    This indicates:")
        print("    - Role assignment is controlled by server")
        print("    - User input for role is properly ignored")
        print("    - Security fixes are working correctly")
        return 0

if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("\n\n[!] Test interrupted by user")
        sys.exit(130)

