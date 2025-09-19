#!/bin/bash
cd /Users/jessicazhou/real-time-security/backend
source venv/bin/activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000
