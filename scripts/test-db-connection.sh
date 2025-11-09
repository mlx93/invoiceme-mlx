#!/bin/bash

# Test Database Connection Script
# This script tests the Supabase PostgreSQL connection

echo "=========================================="
echo "Testing Supabase Database Connection"
echo "=========================================="
echo ""

# Database connection details
DB_HOST="db.rhyariaxwllotjiuchhz.supabase.co"
DB_PORT="5432"
DB_NAME="postgres"
DB_USER="postgres"
DB_PASSWORD="invoicemesupa"

# Test 1: Using psql (if available)
echo "Test 1: Testing with psql..."
if command -v psql &> /dev/null; then
    echo "Attempting connection..."
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" 2>&1
    if [ $? -eq 0 ]; then
        echo "✅ psql connection successful!"
    else
        echo "❌ psql connection failed"
    fi
else
    echo "⚠️  psql not installed. Skipping psql test."
    echo "   Install with: brew install postgresql (macOS) or apt-get install postgresql-client (Linux)"
fi

echo ""
echo "=========================================="
echo "Test 2: Testing JDBC URL format"
echo "=========================================="
echo ""

# Correct JDBC URL format (NO username/password)
CORRECT_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "✅ CORRECT format (use this):"
echo "   DATABASE_URL=${CORRECT_URL}"
echo "   DB_USERNAME=${DB_USER}"
echo "   DB_PASSWORD=${DB_PASSWORD}"
echo ""

# Wrong JDBC URL format (with username/password)
WRONG_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?user=${DB_USER}&password=${DB_PASSWORD}"
echo "❌ WRONG format (DO NOT use this):"
echo "   DATABASE_URL=${WRONG_URL}"
echo ""

echo "=========================================="
echo "Test 3: Testing with Java/Spring Boot"
echo "=========================================="
echo ""

# Check if we can test with the Spring Boot app
if [ -f "backend/target/invoiceme-backend-2.0.0.jar" ]; then
    echo "Found JAR file. Testing connection..."
    echo ""
    echo "Setting environment variables..."
    export DATABASE_URL="$CORRECT_URL"
    export DB_USERNAME="$DB_USER"
    export DB_PASSWORD="$DB_PASSWORD"
    export SERVER_PORT=8080
    export SPRING_PROFILES_ACTIVE=dev
    
    echo "Starting Spring Boot app (will test DB connection)..."
    echo "Press Ctrl+C after you see 'Started InvoiceMeApplication' or any errors"
    echo ""
    
    cd backend
    java -jar target/invoiceme-backend-2.0.0.jar 2>&1 | head -50
    cd ..
else
    echo "⚠️  JAR file not found. Build it first:"
    echo "   cd backend && mvn clean package -DskipTests"
fi

echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="
echo ""
echo "For Elastic Beanstalk, use these EXACT values:"
echo ""
echo "DATABASE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "DB_USERNAME=${DB_USER}"
echo "DB_PASSWORD=${DB_PASSWORD}"
echo ""
echo "⚠️  IMPORTANT: DATABASE_URL must NOT include ?user= or &password="

