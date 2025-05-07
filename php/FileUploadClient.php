<?php

/**
 * MGOV RCS File Upload Library
 * Version: 1.0.0
 *
 * This file is part of the i-heart library.
 *
 * Author   : 정의진 <jej8076@i-heart.co.kr>
 * Date     : 2025-04-29
 * License  : MIT License
 * Website  : https://i-heart.co.kr
 *
 * Copyright (c) 2025 i-heart. All rights reserved.
 */

/**
 * 파일 업로드 클라이언트
 * 인증 토큰을 관리하고 파일 업로드를 처리합니다.
 */

// SimpleHttpClient 의존성 로드
require_once __DIR__ . '/SimpleHttpClient.php';

class FileUploadClient {
    private static $TOKEN_KEY_PREFIX = "token_";
    private $REQUEST_SUCCESS_CODE = "10000"; // API 비즈니스 로직 처리 성공에 대한 응답 코드
    private $TOKEN_INVALID_CODE = "29011"; // 토큰 유효성 검사 실패에 대한 응답 코드

    private $tokenStorage = array();

    /**
     * 인증 요청을 포함한 파일 업로드 요청 함수
     * @param string $domain API 도메인 URL
     * @param string $clientId 클라이언트 ID
     * @param string $clientPwd 클라이언트 비밀번호
     * @param string $brandId 브랜드 ID (선택적 파라미터)
     * @param string $filePath 업로드할 파일 경로
     * @return array 업로드 결과를 담은 배열
     * @throws Exception 인증 또는 업로드 과정에서 발생한 예외
     */
    public function handleFileUpload($domain, $clientId, $clientPwd, $brandId, $filePath) {

        // 파일 존재 및 접근성 검사
        if (!file_exists($filePath)) {
            error_log("File does not exist: $filePath");
            throw new Exception("File not found: " . basename($filePath));
        }

        if (!is_readable($filePath)) {
            error_log("File is not readable: $filePath");
            throw new Exception("Cannot read file: " . basename($filePath));
        }

        $token = $this->getToken($clientId, $clientPwd);

        // 1. 토큰이 없는 경우: 인증 필요
        if ($token === null) {
            try {
                $authResult = $this->requestAuth($domain, $clientId, $clientPwd);
                $token = $authResult['token'];
                $this->storeToken($clientId, $clientPwd, $token);
            } catch (Exception $authError) {
                $errorInfo = $this->parseError($authError);
                throw new Exception("Authentication failed: " . $errorInfo['message']);
            }
        }

        // 2. 토큰으로 업로드 시도
        try {
            return $this->uploadFile($domain, $token, $filePath, $brandId);
        } catch (Exception $e) {
            $errorObj = $this->parseError($e);

            // 3. 토큰 유효성 검사 실패의 경우 한 번만 재시도
            if (isset($errorObj['code']) && $this->TOKEN_INVALID_CODE === $errorObj['code']) {
                try {
                    // 재인증
                    $authResult = $this->requestAuth($domain, $clientId, $clientPwd);
                    $token = $authResult['token'];
                    $this->storeToken($clientId, $clientPwd, $token);

                    // 새 토큰으로 업로드 재시도
                    return $this->uploadFile($domain, $token, $filePath, $brandId);
                } catch (Exception $retryError) {
                    $errorInfo = $this->parseError($retryError);
                    throw new Exception("Retry failed: " . $errorInfo['message']);
                }
            }

            // 4. 다른 오류인 경우 실패 처리
            throw $e;
        }
    }

    /**
     * 저장된 토큰을 가져오는 메서드
     */
    private function getToken($clientId, $clientPwd) {
        $key = $this->generateKey($clientId, $clientPwd);
        return isset($this->tokenStorage[self::$TOKEN_KEY_PREFIX . $key]) ?
            $this->tokenStorage[self::$TOKEN_KEY_PREFIX . $key] : null;
    }

    /**
     * 클라이언트 ID와 비밀번호로부터 토큰 저장소 키를 생성하는 메서드
     */
    private function generateKey($clientId, $clientPwd) {
        $combined = $clientId . ":" . $clientPwd;
        return str_replace('=', '_', base64_encode($combined));
    }

    /**
     * 인증 요청을 수행하는 메서드
     */
    private function requestAuth($domain, $clientId, $clientPwd) {
        $url = $domain . "/api/v1/auth";

        // HTTP 클라이언트 초기화 (타임아웃 설정)
        $httpClient = new SimpleHttpClient(array(
            'timeout' => 8, // Java의 총 타임아웃과 유사하게 설정
            'followRedirects' => true
        ));

        // 요청 바디 구성
        $requestBody = array(
            'clientId' => $clientId,
            'clientPwd' => $clientPwd
        );

        // 요청 헤더 설정
        $headers = array(
            'Content-Type' => 'application/json'
        );

        // 인증 요청 실행
        list($statusCode, $responseBody, $responseHeaders) = $httpClient->postJson($url, $requestBody, $headers);

        if ($statusCode != 200) {
            throw new Exception("HTTP error code: " . $statusCode);
        }

        $responseData = json_decode($responseBody, true);

        if ($responseData === null) {
            throw new Exception("Failed to parse JSON response");
        }

        if ($this->REQUEST_SUCCESS_CODE !== $responseData['code']) {
            throw new Exception($responseBody);
        }

        return $responseData['data'];
    }

    /**
     * 토큰을 저장하는 메서드
     */
    private function storeToken($clientId, $clientPwd, $token) {
        $key = $this->generateKey($clientId, $clientPwd);
        $this->tokenStorage[self::$TOKEN_KEY_PREFIX . $key] = $token;
    }

    /**
     * 예외로부터 오류 정보를 파싱하는 메서드
     */
    private function parseError($error) {
        $errorMap = array(
            'code' => '',
            'message' => ''
        );

        if ($error === null) {
            $errorMap['message'] = 'Unknown error';
            return $errorMap;
        }

        try {
            $message = $error->getMessage();
            if ($message !== null && strpos($message, '{') === 0) {
                $jsonData = json_decode($message, true);
                if (is_array($jsonData)) {
                    // JSON 디코딩 성공 시, code 키가 없으면 빈 문자열 설정
                    if (!isset($jsonData['code'])) {
                        $jsonData['code'] = '';
                    }
                    return $jsonData;
                }
            }
        } catch (Exception $e) {
            // JSON 파싱 실패 시
        }

        $errorMap['message'] = $error->getMessage() !== null ? $error->getMessage() : 'Error parsing error';
        return $errorMap;
    }

    /**
     * 파일 업로드를 수행하는 메서드
     */
    private function uploadFile($domain, $token, $filePath, $brandId) {
        $url = $domain . "/api/v1/upload";

        // HTTP 클라이언트 초기화 (타임아웃 설정)
        $httpClient = new SimpleHttpClient(array(
            'timeout' => 15, // Java의 총 타임아웃과 유사하게 설정
            'followRedirects' => true
        ));

        // 요청 헤더 설정
        $headers = array(
            'Authorization' => 'Bearer ' . $token
        );

        // JSON 요청 데이터 생성
        $reqFileJson = array(
            'brandId' => $brandId !== null ? $brandId : ''
        );

        // 멀티파트 폼 데이터로 요청 실행
        list($statusCode, $responseBody, $responseHeaders) = $httpClient->postMultipart(
            $url,
            array('reqFile' => json_encode($reqFileJson)),  // 일반 필드
            array('filePart' => $filePath),  // 파일
            $headers
        );

        // 로깅 추가
        error_log("Status code: " . ($statusCode ?: 'empty'));
        error_log("Response length: " . (strlen($responseBody) ?: 'empty'));
        error_log("Response headers: " . (empty($responseHeaders) ? 'empty' : print_r($responseHeaders, true)));

        if ($statusCode != 200) {
            throw new Exception("HTTP error code: " . $statusCode);
        }

        $responseData = json_decode($responseBody, true);

        if ($responseData === null) {
            throw new Exception("Failed to parse JSON response");
        }

        if ($this->REQUEST_SUCCESS_CODE !== $responseData['code']) {
            throw new Exception($responseBody);
        }

        return $responseData;
    }
}
