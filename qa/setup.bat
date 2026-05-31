@echo off
setlocal

set "ENV_FILE=%~dp0test.env"
if not exist "%ENV_FILE%" set "ENV_FILE=%~dp0..\test.env"
if not exist "%ENV_FILE%" (
  echo Missing test.env. Put it into %~dp0 or %~dp0..
  exit /b 1
)

set "MODE=%~1"
if "%MODE%"=="" set "MODE=up"

if /I "%MODE%"=="reset" (
  echo Resetting gadget-room containers and volumes...
  docker compose --project-directory "%~dp0.." --project-name gadget-room-backend --env-file "%ENV_FILE%" down -v --remove-orphans
  if errorlevel 1 exit /b 1
  echo Starting up gadget-room...
  docker compose --project-directory "%~dp0.." --project-name gadget-room-backend --env-file "%ENV_FILE%" up --build
  exit /b
)

if /I "%MODE%"=="up" (
  echo Starting up gadget-room...
  docker compose --project-directory "%~dp0.." --project-name gadget-room-backend --env-file "%ENV_FILE%" up --build
  exit /b
)

echo Usage: setup.bat [up^|reset]
exit /b 1
