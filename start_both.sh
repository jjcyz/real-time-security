#!/bin/bash

echo "🚀 Starting Real-Time Security Dashboard (Frontend + Backend)"

# Function to check if a port is in use
check_port() {
    lsof -i :$1 > /dev/null 2>&1
    return $?
}

# Start backend if not running
if check_port 8080; then
    echo "✅ Backend already running on port 8080"
else
    echo "🔧 Starting backend..."
    cd backend
    mvn spring-boot:run > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo "Backend started with PID: $BACKEND_PID"
    cd ..
fi

# Start frontend if not running
if check_port 3000; then
    echo "✅ Frontend already running on port 3000"
else
    echo "🔧 Starting frontend..."
    cd frontend
    npm run dev > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo "Frontend started with PID: $FRONTEND_PID"
    cd ..
fi

# Create logs directory
mkdir -p logs

echo ""
echo "🎉 Both services are starting up!"
echo ""
echo "📊 Backend API: http://localhost:8080/api"
echo "🌐 Frontend: http://localhost:3000"
echo ""
echo "📝 Logs:"
echo "   - Backend: logs/backend.log"
echo "   - Frontend: logs/frontend.log"
echo ""
echo "🛑 To stop: pkill -f 'spring-boot:run' && pkill -f 'npm run dev'"
echo ""
