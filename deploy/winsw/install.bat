@echo off

@echo Checking for Java...
where java >nul 2>nul
if %errorlevel%==1 (
    @echo Java not found in path.
    pause
    exit
)

@echo Creating output directory if it does not exist...
if not exist "maps" mkdir maps

@echo Installing Service...
FE2_Kartengenerierung.exe install
FE2_Kartengenerierung.exe status

@echo Starting Service...
FE2_Kartengenerierung.exe start
FE2_Kartengenerierung.exe status

@echo Give Service some seconds to come up. Will then open health endpoint...
timeout /t 10 /nobreak

start "" http://localhost:8080/actuator/health
