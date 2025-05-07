/*
 * Copyright (c) 2025 i-heart. All rights reserved.
 *
 * MGOV RCS File Upload Library
 * Version: 1.0.0
 *
 * This file is part of the i-heart library.
 * Created by: 정의진 (jej8076@i-heart.co.kr),  박민호 (minoflower@i-heart.co.kr)
 * Date: 2025-04-30
 * License: MIT License
 *
 * https://i-heart.co.kr
 */

const path = require("path");
const fs = require("fs");

// 로컬 스토리지 키
const TOKEN_KEY_PREFIX = "auth_token_";

/**
 * 인증 시도 요청이 포함되어 있는 파일 업로드 요청 함수
 * @param domain
 * @param clientId
 * @param clientPwd
 * @param brandId
 * @param filepath // {file: File, filePath: "/path/to/image.jpeg"} 웹브라우저 환경: File 객체, Node.js 환경: 파일 경로
 * @returns {Promise<*>}
 */
async function handleFileUpload(
    domain,
    clientId,
    clientPwd,
    brandId = "",
    filepath,
) {
  let token = tokenManager.getToken(clientId, clientPwd);

  // 1. 토큰이 없는 경우: 인증 필요
  if (!token) {
    try {
      const authResult = await requestAuth(domain, clientId, clientPwd);
      tokenManager.storeToken(clientId, clientPwd, authResult.token);
      token = authResult.token;
    } catch (error) {
      // 인증 실패 시 오류 반환
      return Promise.reject(parseError(error));
    }
  }

  // 2. 토큰으로 업로드 시도
  try {
    return await uploadFile(domain, token, filepath, {brandId});
  } catch (error) {
    const errorObj = parseError(error);

    // 3. 토큰 유효성 검사 실패의 경우 한 번만 재시도
    // 토큰이 만료 등 토큰이 유효하지 않음 -> code:"29011", message:"권한 없음"
    if (errorObj?.code === "29011") {
      try {
        // 재인증
        const authResult = await requestAuth(domain, clientId, clientPwd);
        tokenManager.storeToken(clientId, clientPwd, authResult.token);
        token = authResult.token;

        // 새 토큰으로 업로드 재시도
        return await uploadFile(domain, token, filepath, {brandId});
      } catch (error) {
        return Promise.reject(parseError(error));
      }
    }

    // 4. 다른 오류인 경우 실패 처리
    return Promise.reject(errorObj);
  }
}

/**
 * 오류 객체 파싱 헬퍼 함수
 * @param {Error|Object} error - 처리할 오류
 * @returns {Object} 파싱된 오류 객체
 */
function parseError(error) {
  if (!error) {
    return {message: "Unknown error"};
  }

  try {
    return typeof error.message === "string" && error.message.startsWith("{")
        ? JSON.parse(error.message)
        : error;
  } catch (e) {
    return {message: error.message || "Error parsing error"};
  }
}

/**
 * API 인증 요청을 수행하는 함수
 * @param {string} domain - API 도메인 주소 (예: 'https://nirs-rcs.i-heart.kr')
 * @param {string} clientId - 클라이언트 ID
 * @param {string} clientPwd - 클라이언트 비밀번호
 * @returns {Promise} API 응답 결과를 담은 Promise 객체
 */
const requestAuth = async (domain, clientId, clientPwd) => {
  // URL 경로
  const url = `${domain}/api/v1/auth`;

  // 요청 옵션 설정
  const options = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      clientId,
      clientPwd,
    }),
  };

  let response;

  try {
    // fetch API를 사용하여 요청 보내기
    response = await fetch(url, options);
  } catch (error) {
    throw new Error(JSON.stringify(error.message));
  }

  // 요청에 대한 응답이 성공적이지 않으면 에러 발생

  if (!response?.ok) {
    const {status, statusText} = response;
    throw new Error(JSON.stringify({status, statusText}));
  }

  // 응답 데이터를 JSON으로 파싱하여 반환
  const data = await response.json();

  // 요청 성공 후 결과에 대한 응답이 성공적이지 않으면 에러 발생
  if (data?.code !== "10000") {
    throw new Error(JSON.stringify(data));
  }

  return data.data;
};

/**
 * 파일 업로드 API 요청을 수행하는 함수
 * @param {string} domain - API 도메인 주소 (예: 'https://nirs-rcs.i-heart.kr')
 * @param {string} token - 인증 토큰
 * @param {Object} filepath - 업로드할 파일 경로
 * @param {Object} reqData - 요청에 포함할 추가 데이터 (예: {brandId: ""})
 * @returns {Promise} API 응답 결과를 담은 Promise 객체
 */
const uploadFile = async (
    domain,
    token,
    filepath,
    reqData = {brandId: ""}
) => {
  // URL 경로
  const url = `${domain}/api/v1/upload`;

  // FormData 객체 생성
  const {body, headers} = await createFormData(filepath, reqData);

  // 요청 옵션 설정
  const options = {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      ...headers,
    },
    body: body,
  };

  // fetch API를 사용하여 요청 보내기
  const response = await fetch(url, options);

  // 호출에 대한 응답이 성공적이지 않으면 에러 발생
  if (!response.ok) {
    throw new Error(`HTTP error! Status: ${response.status}`);
  }

  // 응답 데이터를 JSON으로 파싱하여 반환
  const data = await response.json();

  if (data.code !== "10000") {
    throw new Error(JSON.stringify(data));
  }

  console.log(data);

  return data;
};

const createFormData = async (filepath, reqData) => {
  const filename = path.basename(filepath);
  const file = fs.readFileSync(filepath);

  const boundary = "----WebKitFormBoundary" + Math.random().toString(36);
  const mimeType = getMimeType(filename);

  // FormData 구성 수작업
  const formParts = [];

  // 1. JSON 필드
  formParts.push(Buffer.from(`--${boundary}\r\n`));
  formParts.push(
      Buffer.from(`Content-Disposition: form-data; name="reqFile"\r\n\r\n`)
  );
  formParts.push(Buffer.from(JSON.stringify(reqData) + "\r\n"));

  // 2. 파일 필드
  formParts.push(Buffer.from(`--${boundary}\r\n`));
  formParts.push(
      Buffer.from(
          `Content-Disposition: form-data; name="filePart"; filename="${filename}"\r\n`
      )
  );
  formParts.push(Buffer.from(`Content-Type: ${mimeType}\r\n\r\n`));
  formParts.push(file);
  formParts.push(Buffer.from("\r\n"));

  // 3. 종료
  formParts.push(Buffer.from(`--${boundary}--\r\n`));

  const body = Buffer.concat(formParts);

  return {
    body,
    headers: {
      "Content-Type": `multipart/form-data; boundary=${boundary}`,
      "Content-Length": body.length,
    },
  };
};

/**
 * 파일 확장자 기반 MIME 타입 추출
 * @param {string} filename - 파일 이름
 * @returns {string} MIME 타입
 */
const getMimeType = (filename = "") => {
  const ext = path.extname(filename).toLowerCase();

  const mimeTypes = {
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".png": "image/png",
    ".gif": "image/gif",
    ".webp": "image/webp",
    ".pdf": "application/pdf",
    ".txt": "text/plain",
    ".csv": "text/csv",
    ".json": "application/json",
    ".zip": "application/zip",
    ".mp4": "video/mp4",
    ".mp3": "audio/mpeg",
    ".doc": "application/msword",
    ".docx":
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  };

  return mimeTypes[ext] || "application/octet-stream"; // 기본값
};

// 사용 예시

/**
 * 토큰 관리를 위한 유틸리티 객체
 */
const TokenManager = () => {
  const tokenStorage = {};

  // 기본값 1시간
  /**
   * 토큰 저장 키 생성
   * @param {string} clientId - 클라이언트 ID
   * @param {string} clientPwd - 클라이언트 비밀번호
   * @returns {string} 저장 키
   */
  const generateKey = (clientId, clientPwd) => {
    // 보안을 위해 clientId와 clientPwd를 해싱
    return btoa(`${clientId}:${clientPwd}`).replace(/=/g, "_");
  };

  /**
   * 토큰 저장
   * @param {string} clientId - 클라이언트 ID
   * @param {string} clientPwd - 클라이언트 비밀번호
   * @param {string} token - 저장할 토큰
   */
  const storeToken = (clientId, clientPwd, token) => {
    try {
      const key = generateKey(clientId, clientPwd);

      // 토큰 저장
      tokenStorage[`${TOKEN_KEY_PREFIX}${key}`] = token;

      return true;
    } catch (error) {
      console.error("토큰 저장 중 오류 발생:", error);
      return false;
    }
  };

  /**
   * 저장된 토큰 가져오기
   * @param {string} clientId - 클라이언트 ID
   * @param {string} clientPwd - 클라이언트 비밀번호
   * @returns {string|null} 토큰 또는 null(토큰이 없는 경우)
   */
  const getToken = (clientId, clientPwd) => {
    try {
      const key = generateKey(clientId, clientPwd);
      const tokenKey = `${TOKEN_KEY_PREFIX}${key}`;
      const token = tokenStorage[tokenKey];

      if (!token) {
        return null;
      }

      return token;
    } catch (error) {
      console.error("토큰 조회 중 오류 발생:", error);
      return null;
    }
  };

  // 공개 메서드 반환
  return {
    storeToken,
    getToken,
  };
};

const tokenManager = TokenManager();

module.exports = {
  handleFileUpload,
};
