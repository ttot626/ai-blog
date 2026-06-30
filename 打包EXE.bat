@echo off
chcp 65001 >nul
title 打包 AI Blog 桌面版
cd /d "%~dp0"

echo ========================================
echo   AI Blog 桌面版（独立窗口软件）
echo ========================================
echo.

where java >nul 2>&1 || (echo [错误] 需要 JDK 21 & pause & exit /b 1)
where jpackage >nul 2>&1 || (echo [错误] 需要完整版 JDK 21 & pause & exit /b 1)

echo [准备] 关闭 IDEA 运行中的项目，暂停 OneDrive 同步
echo.

for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /PID %%p /F >nul 2>&1
timeout /t 2 /nobreak >nul

set "BUILD_DIR=C:\AIBlog-build"

echo [1/5] Maven 打包...
call mvn package -Pdesktop -DskipTests -q
if errorlevel 1 call mvn clean package -Pdesktop -DskipTests -q
if errorlevel 1 (echo [错误] Maven 失败 & pause & exit /b 1)

echo [2/5] 清理旧文件...
taskkill /F /IM AIBlog.exe >nul 2>&1
taskkill /F /IM javaw.exe >nul 2>&1
timeout /t 1 /nobreak >nul

if exist "%BUILD_DIR%\AIBlog" (
    echo       旧文件夹存在，先挪到备份目录（比直接删更快）...
    set "OLD=%BUILD_DIR%\AIBlog_old_%RANDOM%"
    move "%BUILD_DIR%\AIBlog" "%OLD%" >nul 2>&1
    if exist "%BUILD_DIR%\AIBlog" (
        echo       挪动失败，尝试 robocopy 删除...
        set "E=%TEMP%\aiblog_empty_%RANDOM%"
        mkdir "%E%" 2>nul
        robocopy "%E%" "%BUILD_DIR%\AIBlog" /MIR /R:0 /W:0 /NFL /NDL /NJH /NJS >nul
        rmdir "%E%" 2>nul
        rmdir /s /q "%BUILD_DIR%\AIBlog" 2>nul
    )
)

if exist "%BUILD_DIR%\AIBlog" (
    echo.
    echo [错误] 无法清理旧文件夹，请关闭所有 AI Blog 窗口后重试
    pause
    exit /b 1
)

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"
echo       清理完成

echo [3/5] jpackage 生成程序（3-10 分钟）...
jpackage ^
  --input target ^
  --dest "%BUILD_DIR%" ^
  --name "AIBlog" ^
  --main-jar ai-blog.jar ^
  --main-class org.springframework.boot.loader.launch.JarLauncher ^
  --type app-image ^
  --app-version 1.0.0 ^
  --vendor "AI Blog" ^
  --description "AI Blog 桌面版" ^
  --java-options "-Dfile.encoding=UTF-8"

if errorlevel 1 (echo [错误] jpackage 失败 & pause & exit /b 1)

set "APP_DIR=%BUILD_DIR%\AIBlog"

echo [3.5/5] 补全 java.exe（杀毒软件有时会删掉）...
set "JDK_BIN=C:\Program Files\Java\jdk-21.0.10\bin"
if exist "%JDK_BIN%\javaw.exe" (
    copy /y "%JDK_BIN%\java.exe" "%APP_DIR%\runtime\bin\" >nul
    copy /y "%JDK_BIN%\javaw.exe" "%APP_DIR%\runtime\bin\" >nul
    echo       已复制 java.exe / javaw.exe
) else (
    echo [警告] 未找到 JDK，请手动运行 修复启动.bat
)

echo [4/5] 创建桌面启动器...

:: 主启动器：双击这个，弹出独立软件窗口
> "%APP_DIR%\AI Blog.bat" echo @echo off
>> "%APP_DIR%\AI Blog.bat" echo cd /d "%%~dp0"
>> "%APP_DIR%\AI Blog.bat" echo start "" "%%~dp0runtime\bin\javaw.exe" -jar "%%~dp0app\ai-blog.jar"

:: 无黑窗口 VBS 启动器
> "%APP_DIR%\AI Blog.vbs" echo Set WshShell = CreateObject("WScript.Shell")
>> "%APP_DIR%\AI Blog.vbs" echo WshShell.CurrentDirectory = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
>> "%APP_DIR%\AI Blog.vbs" echo WshShell.Run """" ^& WshShell.CurrentDirectory ^& "\runtime\bin\javaw.exe"" -jar """ ^& WshShell.CurrentDirectory ^& "\app\ai-blog.jar""", 0, False

:: 测试内置 Java 是否正常
"%APP_DIR%\runtime\bin\java.exe" -version >nul 2>&1
if errorlevel 1 (
    echo [警告] 内置 Java 异常，将创建备用启动器（使用系统 Java）
    > "%APP_DIR%\AI Blog-备用.bat" echo @echo off
    >> "%APP_DIR%\AI Blog-备用.bat" echo cd /d "%%~dp0"
    >> "%APP_DIR%\AI Blog-备用.bat" echo javaw -jar "%%~dp0app\ai-blog.jar"
)

echo [5/5] 复制配置和 SQL...
if not exist "%APP_DIR%\sql" mkdir "%APP_DIR%\sql"
if not exist "%APP_DIR%\config" mkdir "%APP_DIR%\config"
copy /y "sql\init.sql" "%APP_DIR%\sql\" >nul
copy /y "installer\config\application.yml" "%APP_DIR%\config\" >nul
copy /y "installer\修复启动.bat" "%APP_DIR\" >nul
> "%APP_DIR%\使用说明.txt" echo 双击 AI Blog.bat 启动（独立窗口）
>> "%APP_DIR%\使用说明.txt" echo 首次使用：MySQL 执行 sql\init.sql
>> "%APP_DIR%\使用说明.txt" echo 若启动失败：先运行 修复启动.bat

powershell -Command "Compress-Archive -Path '%APP_DIR%' -DestinationPath '%BUILD_DIR%\AIBlog.zip' -Force"

echo       清理旧备份...
for /d %%D in ("%BUILD_DIR%\AIBlog_old_*") do rmdir /s /q "%%D" 2>nul
del /f /q "%BUILD_DIR%\AIBlog-1.0-Windows.zip" 2>nul
del /f /q "%BUILD_DIR%\AIBlog-桌面版.zip" 2>nul

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo 请双击这个启动（独立软件窗口）：
echo   C:\AIBlog-build\AIBlog\AI Blog.bat
echo.
echo 分发 zip：C:\AIBlog-build\AIBlog.zip
echo.
explorer "%APP_DIR%"
pause
