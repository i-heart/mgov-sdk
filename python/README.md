# NIRS Sample Code - Python

## 디렉토리 구조

```text
nirs/python/
├── FileUploadApplication.py      # 실행 예제 파일
├── FileUploadClient.py           # 파일 업로드 모듈
└── README.md
```

<br/>

## 모듈 사용 가이드

1. `FileUploadClient.py` 파일을 import

```python
from FileUploadClient import FileUploadClient
```

<br/>

2. `FileUploadClient` 객체를 생성하여 사용

```python
uploader = FileUploadClient()  # 객체 생성

uploader.handle_file_upload()  # 파일 업로드 메소드 호출
```
