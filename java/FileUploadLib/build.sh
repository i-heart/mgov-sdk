#!/bin/sh

# mvn 과 pom.xml을 사용해 의존성을 포함한 라이브러리 빌드
mvn --offline clean package

# 빌드된 파일을 현재 경로로 복사
cp target/nirs-file-upload-1.0.0.jar .

# 빌드된 파일 삭제
rm -rf target
