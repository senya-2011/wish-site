@echo off
echo Сборка всех артефактов для Docker...

if not exist "docker\artifacts" mkdir docker\artifacts

echo Сборка Core Service...
call gradlew.bat :core-service:bootJar --no-daemon
for %%f in (core-service\build\libs\*.jar) do (
    echo %%~nxf | findstr /V "plain" >nul
    if not errorlevel 1 (
        copy /Y "%%f" docker\artifacts\core-service.jar >nul
        goto :core_done
    )
)
:core_done

echo Сборка Notification Service...
call gradlew.bat :notification-service:bootJar --no-daemon
for %%f in (notification-service\build\libs\*.jar) do (
    echo %%~nxf | findstr /V "plain" >nul
    if not errorlevel 1 (
        copy /Y "%%f" docker\artifacts\notification-service.jar >nul
        goto :notif_done
    )
)
:notif_done

echo Сборка Frontend...
cd frontend
set VITE_API_BASE_URL=/api
call npm ci
call npm run build
cd ..

if exist "docker\artifacts\frontend" rmdir /s /q "docker\artifacts\frontend"
xcopy /E /I /Y frontend\dist docker\artifacts\frontend >nul

echo Все артефакты собраны в docker\artifacts\!
