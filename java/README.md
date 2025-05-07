# JAVA

```
FileUploadApp/
├── src/                          
│   └── main/                     
│       └── java/
│           └── FileUploadApplication.java  # 실행 예제 메인 애플리케이션 클래스
│
├── lib/                          # FileUploadLib를 빌드하여 생성된 jar 파일을 업로드하는 부분
│   └── nirs-file-upload-1.0.0.jar
│
├── run.bat                       # Windows 실행 스크립트
├── run.sh                        # Linux/Mac 실행 스크립트
├── run-interactive.bat           # Windows 대화형 실행 스크립트
├── run-interactive.sh            # Linux/Mac 대화형 실행 스크립트
└── setup.conf                    # run.sh 혹은 run.bat 설정 파일

FileUploadLib/
├── src/                          
│   └── main/                     
│       └── java/                  
│           └── kr.i_heart/       # 패키지 기본 구조
│               ├── http/         # HTTP 관련 클래스 (HTTP request에 대한 라이브러리 의존 제거를 위함)
│               │   ├── ContentType.java
│               │   ├── CustomHttpClient.java
│               │   ├── EntityUtils.java
│               │   ├── FileBody.java
│               │   ├── HttpEntity.java
│               │   ├── HttpRequest.java
│               │   ├── HttpResponse.java
│               │   ├── MultipartEntityBuilder.java
│               │   └── StringEntity.java
│               │
│               ├── AuthResponse.java           # 인증 응답 DTO
│               ├── FileUploadClient.java       # handleFileUpload 함수로 인증과 업로드를 한번에 제공하는 클래스
│               └── FileUploadResponse.java     # 파일 업로드 응답 DTO
│
├── lib/                          # 외부 라이브러리 디렉토리
│   ├── byte-buddy-1.14.9.jar
│   ├── jackson-annotations-2.17.0.jar
│   ├── jackson-core-2.17.0.jar
│   └── jackson-databind-2.17.0.jar
│
├── build.sh                      # nirs-file-upload-1.0.0.jar 를 빌드하는 스크립트
├── nirs-file-upload-1.0.0.jar    # build.sh을 사용해 컴파일된 라이브러리
└── pom.xml                       # Maven 프로젝트 설정 파일
```

--- 

## 빌드
### Linux | Mac
```
$ cd $PROJECT/FileUploadLib 
$ ./build.sh
```

### Windows
```
$ cd $PROJECT/FileUploadLib
$ build.bat
```

---

## 샘플 실행
1. FileUploadLib/build.sh 혹은 FileUploadLib/build.bat 파일 실행
2. 생성된 FileUploadLib/nirs-file-upload-1.0.0.jar 파일을 FileUploadApp/lib 안에 이동/복사
3. setup.conf 파일에 정보 입력 후 run.sh 혹은 run.bat 실행
4. (또는 setup.conf 입력 없이) run-interactive.sh 혹은 run-interactive.bat 실행  
### Linux | Mac
```
$ cd $PROJECT/FileUploadApp
$ (파일 편집 후 저장) setup.conf 
$ ./run.sh
OR
$ ./run-interactive.sh
```
### Windows
```
$ cd $PROJECT/FileUploadApp
$ (파일 편집 후 저장) setup.conf 
$ run.bat
OR
$ run-interactive.bat
```
