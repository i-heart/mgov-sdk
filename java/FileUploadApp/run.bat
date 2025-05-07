@echo off
setlocal

REM setup.conf 파일에서 설정 읽기
for /f "tokens=1,2 delims==" %%G in (setup.conf) do (
   set %%G=%%H
)

REM 메인 메소드 컴파일
javac -cp "src/main/lib/nirs-file-upload-1.0.0.jar" src/main/java/FileUploadApplication.java

REM 메인 클래스 실행
java -cp "./src/main/java;src/main/lib/nirs-file-upload-1.0.0.jar" FileUploadApplication "%domain%" "%clientId%" "%clientPwd%" "%brandId%" "%filePath%"

endlocal