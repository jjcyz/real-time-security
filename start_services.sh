#!/bin/bash

# Real-Time Security Dashboard Startup Script
echo "🚀 Starting Real-Time Security Dashboard Services..."

# Function to check if a port is in use
check_port() {
    lsof -i :$1 > /dev/null 2>&1
    return $?
}

# Create logs directory if it doesn't exist
mkdir -p logs

# Check if Java backend is already running
if check_port 8080; then
    echo "✅ Java backend already running on port 8080"
else
    echo "🔧 Starting Java backend..."
    cd backend
    mvn spring-boot:run > ../logs/java-backend.log 2>&1 &
    JAVA_PID=$!
    echo "Java backend started with PID: $JAVA_PID"
    cd ..
fi

# Check if React frontend is already running
if check_port 3000; then
    echo "✅ React frontend already running on port 3000"
else
    echo "🔧 Starting React frontend..."
    cd frontend
    npm run dev > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo "React frontend started with PID: $FRONTEND_PID"
    cd ..
fi

echo ""
echo "🎉 Services are starting up!"
echo ""
echo "📊 Java Backend: http://localhost:8080/api"
echo "   - API Documentation: http://localhost:8080/api"
echo "   - H2 Database Console: http://localhost:8080/h2-console"
echo "   - Default credentials: admin/admin"
echo ""
echo "🌐 React Frontend: http://localhost:3000"
echo ""
echo "📝 Logs are being written to:"
echo "   - Java Backend: logs/java-backend.log"
echo "   - React Frontend: logs/frontend.log"
echo ""
echo "🛑 To stop services, run: ./stop_services.sh"
echo ""
