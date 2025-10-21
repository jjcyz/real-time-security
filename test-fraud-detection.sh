#!/bin/bash

# Fraud Detection Testing Script
# Tests various scenarios to demonstrate the fraud detection system

BASE_URL="http://localhost:8080/api"

echo "======================================"
echo "🛡️  Fraud Detection System Tests"
echo "======================================"
echo ""

# Function to print test results
print_result() {
    echo "----------------------------------------"
    echo "$1"
    echo "----------------------------------------"
}

# Test 1: Legitimate Small Transaction
print_result "Test 1: Legitimate Small Transaction ($50)"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50.00,
    "userId": 1,
    "merchantName": "Starbucks",
    "merchantCategory": "Food & Dining",
    "ipAddress": "192.168.1.100",
    "deviceId": "device_mobile_123"
  }' | python3 -m json.tool
echo ""
sleep 2

# Test 2: High Amount Transaction (Medium Risk)
print_result "Test 2: High Amount Transaction ($6,500 - Electronics)"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 6500.00,
    "userId": 2,
    "merchantName": "Apple Store",
    "merchantCategory": "Electronics",
    "ipAddress": "192.168.1.200",
    "merchantLocation": "New York, NY"
  }' | python3 -m json.tool
echo ""
sleep 2

# Test 3: Suspicious Transaction (Should be Fraudulent)
print_result "Test 3: High-Risk Transaction ($9,800 - Crypto + Suspicious IP)"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 9800.00,
    "userId": 1,
    "merchantName": "Crypto Exchange",
    "merchantCategory": "Cryptocurrency",
    "ipAddress": "45.33.12.45",
    "transactionLocation": "Unknown"
  }' | python3 -m json.tool
echo ""
sleep 2

# Test 4: Wire Transfer (High Risk Merchant)
print_result "Test 4: Wire Transfer ($7,000 - High Risk Category)"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 7000.00,
    "userId": 3,
    "merchantName": "International Wire Service",
    "merchantCategory": "Wire Transfer",
    "ipAddress": "192.168.1.50"
  }' | python3 -m json.tool
echo ""
sleep 2

# Test 5: Velocity Test - Multiple Rapid Transactions
print_result "Test 5: Velocity Test - Creating 6 rapid transactions..."
for i in {1..6}; do
    echo "Transaction $i/6..."
    curl -s -X POST "$BASE_URL/transactions" \
      -H "Content-Type: application/json" \
      -d "{
        \"amount\": 500.00,
        \"userId\": 1,
        \"merchantName\": \"Online Store\",
        \"merchantCategory\": \"Retail\",
        \"ipAddress\": \"192.168.1.100\"
      }" > /dev/null
    sleep 1
done
echo "Creating final velocity-triggered transaction..."
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00,
    "userId": 1,
    "merchantName": "Online Store",
    "merchantCategory": "Retail",
    "ipAddress": "192.168.1.100"
  }' | python3 -m json.tool
echo ""
sleep 2

# Get Statistics
print_result "📊 Updated Statistics"
curl -s "$BASE_URL/stats" | python3 -m json.tool
echo ""

# Get Suspicious Transactions
print_result "🚨 Suspicious Transactions"
curl -s "$BASE_URL/transactions/suspicious" | python3 -m json.tool | head -50
echo ""

# Get Fraud Insights
print_result "📈 Fraud Detection Insights"
curl -s "$BASE_URL/fraud/insights" | python3 -m json.tool
echo ""

# Get High-Risk Transactions
print_result "⚠️  High-Risk Transactions (Score ≥ 70)"
curl -s "$BASE_URL/transactions/high-risk" | python3 -m json.tool | head -50
echo ""

echo "======================================"
echo "✅ Fraud Detection Tests Complete!"
echo "======================================"
echo ""
echo "Summary:"
echo "- Tested legitimate transactions"
echo "- Tested high-amount transactions"
echo "- Tested high-risk merchant categories"
echo "- Tested geographic anomalies"
echo "- Tested velocity checks"
echo ""
echo "View full documentation in FRAUD_DETECTION.md"

