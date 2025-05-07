package kr.i_heart;

/*
 * Copyright (c) 2025 i-heart. All rights reserved.
 *
 * MGOV RCS File Upload Library
 * Version: 1.0.0
 *
 * This file is part of the i-heart library.
 * Created by: 정의진 (jej8076@i-heart.co.kr)
 * Date: 2025-04-29
 * License: MIT License
 *
 * https://i-heart.co.kr
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import kr.i_heart.http.CustomHttpClient;
import kr.i_heart.http.EntityUtils;
import kr.i_heart.http.HttpEntity;
import kr.i_heart.http.HttpRequest;
import kr.i_heart.http.HttpResponse;
import kr.i_heart.http.MultipartEntityBuilder;
import kr.i_heart.http.StringEntity;
import kr.i_heart.http.ContentType;
import kr.i_heart.http.FileBody;

/**
 * 파일 업로드 클라이언트
 * 인증 토큰을 관리하고 파일 업로드를 처리합니다.
 */
public class FileUploadClient {
    private static final String TOKEN_KEY_PREFIX = "token_";
    private final String REQUEST_SUCCESS_CODE = "10000"; // API 비즈니스 로직 처리 성공에 대한 응답 코드
    private final String TOKEN_INVALID_CODE = "29011"; // 토큰 유효성 검사 실패에 대한 응답 코드

    private final Map<String, String> tokenStorage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증 요청을 포함한 파일 업로드 요청 함수
     * @param domain API 도메인 URL
     * @param clientId 클라이언트 ID
     * @param clientPwd 클라이언트 비밀번호
     * @param brandId 브랜드 ID (선택적 파라미터)
     * @param file 업로드할 파일 객체
     * @return 업로드 결과를 담은 Map 객체
     * @throws Exception 인증 또는 업로드 과정에서 발생한 예외
     */
    public FileUploadResponse handleFileUpload(String domain, String clientId, String clientPwd, String brandId, File file) throws Exception {

        // 1. 매개변수 유효성 검사
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("domain cannot be null or empty.");
        }
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be null or empty.");
        }
        if (clientPwd == null || clientPwd.trim().isEmpty()) {
            throw new IllegalArgumentException("clientPwd cannot be null or empty.");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }

        String token = getToken(clientId, clientPwd);

        // 2. 토큰이 없는 경우: 인증 필요
        if (token == null) {
            try {
                AuthResponse authResponse = requestAuth(domain, clientId, clientPwd);
                token = authResponse.getData().getToken();
                storeToken(clientId, clientPwd, token);
            } catch (Exception authError) {
                throw new RuntimeException("Authentication failed: " + parseError(authError).get("message"));
            }
        }

        // 3. 토큰으로 업로드 시도
        try {
            return uploadFile(domain, token, file, brandId);
        } catch (Exception e) {
            Map<String, Object> errorObj = parseError(e);

            // 3. 토큰 유효성 검사 실패의 경우 한 번만 재시도
            if (TOKEN_INVALID_CODE.equals(errorObj.get("code"))) {
                try {
                    // 재인증
                    AuthResponse authResponse = requestAuth(domain, clientId, clientPwd);
                    if (authResponse.getData() == null || authResponse.getData().getToken() == null) {
                        throw new RuntimeException("Retry authentication failed: Received invalid auth response.");
                    }
                    token = authResponse.getData().getToken();
                    storeToken(clientId, clientPwd, token);

                    // 새 토큰으로 업로드 재시도
                    return uploadFile(domain, token, file, brandId);
                } catch (Exception retryError) {
                    throw new RuntimeException("Retry failed: " + parseError(retryError).get("message"));
                }
            }

            // 5. 다른 오류인 경우 실패 처리
            throw e;
        }
    }

    /**
     * 인증 요청을 수행하는 메서드
     * 기존 Apache HttpClient 대신 커스텀 HttpClient 사용
     */
    private AuthResponse requestAuth(String domain, String clientId, String clientPwd) throws Exception {
        String url = domain + "/api/v1/auth";

        // 타임아웃 설정
        CustomHttpClient httpClient = CustomHttpClient.custom()
            .setConnectTimeout(5000)  // 연결 타임아웃 (5초)
            .setReadTimeout(8000)     // 읽기 타임아웃 (8초)
            .build();

        try {
            // POST 요청 생성
            HttpRequest.HttpPost httpPost = new HttpRequest.HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");

            // 요청 바디 생성
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("clientId", clientId);
            requestBody.put("clientPwd", clientPwd);

            // JSON 변환 및 엔티티 설정
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            StringEntity entity = new StringEntity(requestBodyJson, "application/json");
            httpPost.setEntity(entity);

            // 요청 실행 및 응답 처리
            try (HttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    throw new RuntimeException("HTTP error code: " + statusCode);
                }

                // 응답 본문을 객체로 변환
                AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
                if (!REQUEST_SUCCESS_CODE.equals(authResponse.getCode())) {
                    throw new RuntimeException(responseBody);
                }
                return authResponse;
            }
        } finally {
            httpClient.close();
        }
    }

    /**
     * 파일 업로드를 수행하는 메서드
     * @param domain API 도메인
     * @param token 인증 토큰
     * @param file 업로드할 파일
     * @param brandId 브랜드 ID
     * @return FileUploadResponse 업로드 결과 DTO
     * @throws Exception 업로드 중 발생한 예외
     */
    private FileUploadResponse uploadFile(String domain, String token, File file, String brandId) throws Exception {
        String url = domain + "/api/v1/upload";

        // 타임아웃 설정
        CustomHttpClient httpClient = CustomHttpClient.custom()
            .setConnectTimeout(7000)  // 연결 타임아웃 (7초)
            .setReadTimeout(15000)    // 읽기 타임아웃 (15초)
            .build();

        try {
            // POST 요청 생성
            HttpRequest.HttpPost httpPost = new HttpRequest.HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + token);

            // 명시적으로 Content-Type 헤더를 설정하지 않음 (MultipartEntity에서 제공하는 값 사용)

            // JSON 요청 데이터 생성
            Map<String, String> reqFileJson = new HashMap<>();
            reqFileJson.put("brandId", brandId != null ? brandId : "");
            String reqFileContent = objectMapper.writeValueAsString(reqFileJson);

            // 멀티파트 요청 구성
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("reqFile", reqFileContent);

            // 파일 내용 추가
            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // fallback MIME type
            }
            ContentType contentType = ContentType.create(mimeType);
            builder.addPart("filePart", new FileBody(file, contentType, file.getName()));

            HttpEntity multipartEntity = builder.build();
            httpPost.setEntity(multipartEntity);

            // Content-Type 헤더를 MultipartEntity에서 제공하는 값으로 설정
            httpPost.setHeader("Content-Type", multipartEntity.getContentType());

            // 디버깅용 로그 (필요시 활성화)
            // System.out.println("Content-Type: " + multipartEntity.getContentType());

            // 요청 실행 및 응답 처리
            try (HttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                // 디버깅용 응답 내용 출력 (필요시 활성화)
                // System.out.println("Response Code: " + statusCode);
                // System.out.println("Response Body: " + responseBody);

                if (statusCode != 200) {
                    throw new RuntimeException("HTTP error code: " + statusCode + ", Response: " + responseBody);
                }

                FileUploadResponse uploadResponse = objectMapper.readValue(responseBody, FileUploadResponse.class);
                if (!REQUEST_SUCCESS_CODE.equals(uploadResponse.getCode())) {
                    throw new RuntimeException(responseBody);
                }
                return uploadResponse;
            }
        } finally {
            httpClient.close();
        }
    }

    /**
     * 클라이언트 ID와 비밀번호로부터 토큰 저장소 키를 생성하는 메서드
     */
    private String generateKey(String clientId, String clientPwd) {
        String combined = clientId + ":" + clientPwd;
        return Base64.getEncoder().encodeToString(combined.getBytes(StandardCharsets.UTF_8))
                .replace("=", "_");
    }

    /**
     * 토큰을 저장하는 메서드
     */
    private void storeToken(String clientId, String clientPwd, String token) {
        String key = generateKey(clientId, clientPwd);
        tokenStorage.put(TOKEN_KEY_PREFIX + key, token);
    }

    /**
     * 저장된 토큰을 가져오는 메서드
     */
    private String getToken(String clientId, String clientPwd) {
        String key = generateKey(clientId, clientPwd);
        return tokenStorage.get(TOKEN_KEY_PREFIX + key);
    }

    /**
     * 예외로부터 오류 정보를 파싱하는 메서드
     */
    private Map<String, Object> parseError(Exception error) {
        Map<String, Object> errorMap = new HashMap<>();

        if (error == null) {
            errorMap.put("message", "Unknown error");
            return errorMap;
        }

        try {
            if (error.getMessage() != null && error.getMessage().startsWith("{")) {
                return objectMapper.readValue(error.getMessage(), Map.class);
            }
        } catch (Exception e) {
            // JSON 파싱 실패 시
        }

        errorMap.put("message", error.getMessage() != null ? error.getMessage() : "Error parsing error");
        return errorMap;
    }

}
