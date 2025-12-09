@echo off]
cd /d "%~dp0"

echo Starting up gadget-room...
docker compose --project-name gadget-room-backend --env-file .\test.env up --build