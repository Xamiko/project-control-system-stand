REM 
timeout /t 3 >nul

echo ==== Subscribing to topic sensor/temperature ====
start "MQTT Sub" cmd /k "D:\Git\project-control-system-stand\mosquittoserver\mosquitto\mosquitto_sub.exe -h 192.168.0.112 -t sensor/temperature"

pause