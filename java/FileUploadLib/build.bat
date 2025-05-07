@echo off
REM Windows batch file equivalent of build.sh

REM mvn 과 pom.xml을 사용해 의존성을 포함한 라이브러리 빌드
call mvn --offline clean package

REM 빌드된 파일을 현재 경로로 복사
copy target\nirs-file-upload-1.0.0.jar .

REM 빌드된 파일 삭제
rmdir /s /q target
