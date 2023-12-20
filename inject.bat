@echo off
setlocal enabledelayedexpansion

@REM Check if 2 or 3 arguments are provided
if "%~2"=="" (
    echo Usage: %0 ^<folder_path^> ^<zip_file_path^> [new_folder_name]
    exit /b 1
)

set "folder_path=%~1"
set "zip_file_path=%~2"
set "new_folder_name=%~3"

@REM Set default new folder name if not provided
if "%new_folder_name%"=="" (
    set "new_folder_name=%~n1-injected"
)

@REM Step 1: Copy the folder recursively to a new folder
xcopy /s /e /i "%folder_path%" "%new_folder_name%"

@REM Step 2: Decompress the zip file into the new folder, overwriting existing files
powershell Expand-Archive -Path "%zip_file_path%" -DestinationPath "%new_folder_name%" -Force

@REM Step 3: Compress the files under the new folder to a new zip file
@REM powershell Compress-Archive -Path "%new_folder_name%\*" -DestinationPath "%new_folder_name%.zip" -Force

@REM Step 4: Remove the temporary folder
@REM rmdir /s /q "%new_folder_name%"

echo Task completed successfully.

exit /b 0