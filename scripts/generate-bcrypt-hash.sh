#!/bin/bash
# Generate BCrypt hash for password
# Usage: ./generate-bcrypt-hash.sh <password>

if [ -z "$1" ]; then
    echo "Usage: $0 <password>"
    exit 1
fi

PASSWORD="$1"

# Try Python bcrypt library first
if command -v python3 &> /dev/null; then
    python3 -c "import bcrypt; print(bcrypt.hashpw(b'$PASSWORD', bcrypt.gensalt()).decode('utf-8'))" 2>/dev/null && exit 0
fi

# Try Node.js bcrypt
if command -v node &> /dev/null; then
    node -e "const bcrypt = require('bcrypt'); bcrypt.hash('$PASSWORD', 10).then(hash => console.log(hash));" 2>/dev/null && exit 0
fi

# Fallback: Use online tool or manual generation
echo "Error: Cannot generate bcrypt hash automatically."
echo "Please use one of these methods:"
echo "1. Python: pip install bcrypt && python3 -c \"import bcrypt; print(bcrypt.hashpw(b'$PASSWORD', bcrypt.gensalt()).decode('utf-8'))\""
echo "2. Online tool: https://bcrypt-generator.com/"
echo "3. Java: Use Spring Security BCryptPasswordEncoder"
exit 1

