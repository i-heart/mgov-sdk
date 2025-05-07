# NIRS Sample Code - Javascript

## 디렉토리 구조

```text
nirs/javascript/
├── examples/                     # 샘플 프로젝트
│   ├── cjs-package/              # CommonJS 기반 예제
│   │   ├── public/
│   │   │   └── index.html
│   │   ├── package.json
│   │   └── server.js
│   └── esm-package/              # ESM 기반 예제
│       ├── public/
│       │   └── index.html
│       ├── package.json
│       └── server.js
│
├── mgov-uploader/                # 파일 업로드 라이브러리
│   ├── dist/                     # 
│   │   └── api.mjs               # ESM 기반 코드
│   ├── node_modules/
│   ├── src/
│   │   └── api.js                # CJS 기반 코드
│   ├── package.json              
│   ├── rollup.config.js          # Rollup 설정 파일 (develop 전용, 영향 없음)
│   └── README.md                 
```

<br/>

## 라이브러리 사용 가이드

1. package.json 이 존재하는 디렉토리로 이동
2. `$ npm install ${mgov-uploader 디렉토리 경로}` 설치
    - e.g. `$ npm install C:/example/modules/mgov-uploader` (실제 경로 아님)
3. pakage.json 의 dependencies 에 mgov-uploader가 추가되어있는지 확인

```json
{
  "name": "example",
  "dependencies": {
    "express": "^5.1.0",
    "mgov-uploader": "file:../../mgov-uploader"
  }
}
```

4. 라이브러리를 사용할 파일에서 import

**CJS 환경**

```javascript
const mgov = require('mgov-uploader'); // 라이브러리 선언

mgov.handleFileUpload // 사용
```

**ESM 환경**

```javascript
import mgov from 'mgov-uploader'; // 라이브러리 선언

mgov.handleFileUpload // 사용
```

<br/>

## 샘플 실행 가이드

- 샘플 프로젝트는 CommonJS(CJS)와 ESM 두 가지 방식으로 제공됩니다.
- 환경 요건
    - Node.js, npm 또는 yarn (본 예제는 npm 기반으로 작성됨)
    - express
- server.js 파일 내에 domain, clientId, password, brandId(선택), filepath 를 변경하여 사용합니다.

1. 서버 실행

**CJS 환경**

```bash
$ cd examples/cjs-package
$ npm install
$ node server.js
```

**ESM 환경**

```bash
$ cd examples/esm-package
$ npm install
$ node server.js
```

2. 웹 브라우저에서 localhost:3000 접속

<img width="500" alt="Image" src="https://github.com/user-attachments/assets/b6570571-cf45-47a3-a614-f4f87c9e6d10" />

3. `fileId 발급받기` 버튼 클릭

4. fileId 발급 확인

<img width="500" alt="Image" src="https://github.com/user-attachments/assets/4553c0ab-410a-4d9f-9e4e-cd03032e9f21" />




