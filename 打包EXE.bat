@echo off
chcp 65001 >nul
title 打包 AI Blog 桌面版
cd /d "%~dp0"

set "VERSION=1.0.0"
set "BUILD_DIR=C:\AIBlog-build"
set "ZIP_NAME=AIBlog-%VERSION%-Windows.zip"

echo ========================================
echo   AI Blog 桌面版（GitHub 可分发 zip）
echo ========================================
echo.

where java >nul 2>&1 || (echo [错误] 需要 JDK 21 & pause & exit /b 1)
where jpackage >nul 2>&1 || (echo [错误] 需要完整版 JDK 21 & pause & exit /b 1)

echo [准备] 关闭运行中的 AI Blog，暂停 OneDrive 同步
echo.

for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /PID %%p /F >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
timeout /t 2 /nobreak >nul

echo [1/5] Maven 打包（桌面版）...
call mvn package -Pdesktop -DskipTests -q
if errorlevel 1 call mvn clean package -Pdesktop -DskipTests -q
if errorlevel 1 (echo [错误] Maven 失败 & pause & exit /b 1)

if not exist "target\ai-blog.jar" (
    echo [错误] 未找到 target\ai-blog.jar
    pause
    exit /b 1
)

echo [2/5] 准备 jpackage 输入...
if exist "target\jpackage-input" rmdir /s /q "target\jpackage-input"
mkdir "target\jpackage-input"
copy /y "target\ai-blog.jar" "target\jpackage-input\" >nul

echo [3/5] 清理旧输出...
if exist "%BUILD_DIR%\AIBlog" (
    set "OLD=%BUILD_DIR%\AIBlog_old_%RANDOM%"
    move "%BUILD_DIR%\AIBlog" "%OLD%" >nul 2>&1
    if exist "%BUILD_DIR%\AIBlog" rmdir /s /q "%BUILD_DIR%\AIBlog" 2>nul
)
for /d %%D in ("%BUILD_DIR%\AIBlog_old_*") do rmdir /s /q "%%D" 2>nul
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo [4/5] jpackage 生成程序（约 3-10 分钟）...
jpackage ^
  --input target\jpackage-input ^
  --dest "%BUILD_DIR%" ^
  --name "AIBlog" ^
  --main-jar ai-blog.jar ^
  --main-class org.springframework.boot.loader.launch.JarLauncher ^
  --type app-image ^
  --app-version %VERSION% ^
  --vendor "AI Blog" ^
  --description "AI Blog 桌面版" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Dapp.auto-open-browser=false"

if errorlevel 1 (echo [错误] jpackage 失败 & pause & exit /b 1)

set "APP_DIR=%BUILD_DIR%\AIBlog"

echo       补全 java.exe...
set "JDK_BIN=C:\Program Files\Java\jdk-21.0.10\bin"
if exist "%JDK_BIN%\javaw.exe" (
    copy /y "%JDK_BIN%\java.exe" "%APP_DIR%\runtime\bin\" >nul
    copy /y "%JDK_BIN%\javaw.exe" "%APP_DIR%\runtime\bin\" >nul
)

echo       创建启动器...
> "%APP_DIR%\AI Blog.bat" echo @echo off
>> "%APP_DIR%\AI Blog.bat" echo cd /d "%%~dp0"
>> "%APP_DIR%\AI Blog.bat" echo start "" "%%~dp0runtime\bin\javaw.exe" -jar "%%~dp0app\ai-blog.jar"

> "%APP_DIR%\AI Blog.vbs" echo Set WshShell = CreateObject("WScript.Shell")
>> "%APP_DIR%\AI Blog.vbs" echo WshShell.CurrentDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
>> "%APP_DIR%\AI Blog.vbs" echo WshShell.Run """" ^& WshShell.CurrentDirectory ^& "\runtime\bin\javaw.exe"" -jar """ ^& WshShell.CurrentDirectory ^& "\app\ai-blog.jar""", 0, False

"%APP_DIR%\runtime\bin\java.exe" -version >nul 2>&1
if errorlevel 1 (
    > "%APP_DIR%\AI Blog-备用.bat" echo @echo off
    >> "%APP_DIR%\AI Blog-备用.bat" echo cd /d "%%~dp0"
    >> "%APP_DIR%\AI Blog-备用.bat" echo javaw -jar "%%~dp0app\ai-blog.jar"
)

echo [5/5] 复制配置并压缩 zip...
if not exist "%APP_DIR%\sql" mkdir "%APP_DIR%\sql"
if not exist "%APP_DIR%\config" mkdir "%APP_DIR%\config"
copy /y "sql\init.sql" "%APP_DIR%\sql\" >nul
copy /y "installer\config\application.yml" "%APP_DIR%\config\" >nul
copy /y "installer\修复启动.bat" "%APP_DIR\" >nul
copy /y "release\使用说明.txt" "%APP_DIR\" >nul

if not exist "target\dist" mkdir "target\dist"
powershell -Command "Compress-Archive -Path '%APP_DIR%' -DestinationPath 'target\dist\%ZIP_NAME%' -Force"
copy /y "target\dist\%ZIP_NAME%" "%BUILD_DIR%\%ZIP_NAME%" >nul

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo 本地启动：%BUILD_DIR%\AIBlog\AI Blog.bat
echo.
echo 上传到 GitHub Releases 的文件：
echo   target\dist\%ZIP_NAME%
echo   （已复制到 %BUILD_DIR%\%ZIP_NAME%）
echo.
echo 发布命令：git tag v%VERSION% ^&^& git push origin v%VERSION%
echo.
explorer "%BUILD_DIR%"
pause
