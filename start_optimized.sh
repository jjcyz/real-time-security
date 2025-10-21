#!/bin/bash

echo "🚀 Starting Optimized Real-Time Security Dashboard"

# Function to check if a port is in use
check_port() {
    lsof -i :$1 > /dev/null 2>&1
    return $?
}

# Create logs directory
mkdir -p logs

# JVM optimization settings
export MAVEN_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.awt.headless=true"

# Start backend if not running
if check_port 8080; then
    echo "✅ Backend already running on port 8080"
else
    echo "🔧 Starting optimized backend..."
    cd backend
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms512m -Xmx1g -XX:+UseG1GC -XX:+UseStringDeduplication" > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    echo "Backend started with PID: $BACKEND_PID (with JVM optimizations)"
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

echo ""
echo "🎉 Optimized services are starting up!"
echo ""
echo "📊 Backend API: http://localhost:8080/api"
echo "🌐 Frontend: http://localhost:3000"
echo ""
echo "⚡ Performance optimizations applied:"
echo "   - Reduced polling frequency (5min instead of 30sec)"
echo "   - Optimized React Query caching"
echo "   - Reduced animation overhead"
echo "   - JVM memory optimization (G1GC, 1GB heap)"
echo "   - Reduced logging verbosity"
echo ""
echo "📝 Logs:"
echo "   - Backend: logs/backend.log"
echo "   - Frontend: logs/frontend.log"
echo ""
echo "🛑 To stop: pkill -f 'spring-boot:run' && pkill -f 'npm run dev'"
echo ""
