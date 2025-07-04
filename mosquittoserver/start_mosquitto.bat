@echo off
echo ==== Start Mosquitto Service ====
net start mosquitto

REM 
if errorlevel 1 (
    echo [Error] Failed to start Mosquitto service. It may already be running or not installed.
) else (
    echo [OK] Mosquitto started successfully.
)


