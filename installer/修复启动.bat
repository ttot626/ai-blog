@echo off
chcp 65001 >nul
title 修复 AI Blog 启动
cd /d "%~dp0"

set "JDK=C:\Program Files\Java\jdk-21.0.10\bin"
set "DEST=%~dp0runtime\bin"

if not exist "%JDK%\javaw.exe" (
    echo [错误] 未找到 JDK 21，请修改本脚本中的 JDK 路径
    pause
    exit /b 1
)

echo 正在复制 java.exe 和 javaw.exe ...
copy /y "%JDK%\java.exe" "%DEST%\" >nul
copy /y "%JDK%\javaw.exe" "%DEST%\" >nul

if exist "%DEST%\javaw.exe" (
    echo [成功] 修复完成！请双击「AI Blog.bat」启动
) else (
    echo [失败] 请右键「以管理员身份运行」本脚本
)

pause
