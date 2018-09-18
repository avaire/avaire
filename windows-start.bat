@echo off
@title AvaIre Windows Launcher

setlocal enabledelayedexpansion
set Looping=False

cls

:START
echo.
echo Welcome to AvaIre's window launcher
echo Please select an option to begin:
echo.
echo    1. Start the bot normally
echo    2. Start the bot with automatic restarts
echo    3. Update the bot using the nightly build
echo    9. Exit program
echo.

set /p in="Enter your option: "

if !in! == 1 (
    goto START_BOT
)
if !in! == 2 (
    goto RESTART_LOOP
)
if !in! == 3 (
    goto UPDATE
)
if !in! == 9 (
    goto EOF
) else (
    echo.
    echo     Invalid option given
    goto START
)

:RESTART_LOOP
set Looping=True
goto START_BOT

:START_BOT
java -Dfile.encoding=UTF-8 -jar AvaIre.jar --no-colors
if !Looping! == False (
    goto START
)

echo.
echo Restarting the bot in 5 second, press CTRL + C to cancel the process.
echo.

choice /d y /t 5 > nul
goto START_BOT

:UPDATE
echo.
echo Updating to the latest version using the nightly build
echo Note: The script will download and unzip some files, this
echo might cause your antivirus to see the script as a threat,
echo just make an exception for the script so it can download
echo the updates.
echo.
echo Tasks:

echo|set /p=" - Downloading the nightly-build.jar file... "
powershell -Command "(New-Object Net.WebClient).DownloadFile('https://avairebot.com/nightly-build.jar', 'nightly-build.jar')"
echo Done

echo|set /p=" - Deleting existing AvaIre.jar file... "
del /f AvaIre.jar > nul 2> nul
echo Done

echo|set /p=" - Renaming nightly-build.jar to AvaIre.jar... "
ren nightly-build.jar AvaIre.jar > nul 2> nul
echo Done
echo.
echo Ava has been successfully updated, going back to the menu.
GOTO START

:EOF
exit
