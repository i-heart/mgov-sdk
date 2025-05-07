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
 * 간단한 HTTP 클라이언트 클래스
 * 외부 의존성 없이 PHP 내장 함수만 사용
 * PHP 5.3 이상부터 PHP 8.x까지 호환
 */
class SimpleHttpClient {
    // 기본 옵션
    private $timeout = 30;
    private $followRedirects = true;
    private $maxRedirects = 5;
    private $lastResponseInfo = array();
    private $lastError = null;

    /**
     * 생성자
     *
     * @param array $options 옵션 배열
     */
    public function __construct($options = array()) {
        if (isset($options['timeout'])) {
            $this->timeout = (int)$options['timeout'];
        }
        if (isset($options['followRedirects'])) {
            $this->followRedirects = (bool)$options['followRedirects'];
        }
        if (isset($options['maxRedirects'])) {
            $this->maxRedirects = (int)$options['maxRedirects'];
        }
    }

    /**
     * GET 요청 수행
     *
     * @param string $url 요청 URL
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function get($url, $headers = array()) {
        $context = $this->createContext('GET', null, $headers);
        return $this->sendRequest($url, $context);
    }

    /**
     * 스트림 컨텍스트 생성
     *
     * @param string $method HTTP 메소드
     * @param mixed $data 요청 데이터
     * @param array $headers 요청 헤더
     * @return resource 스트림 컨텍스트
     */
    private function createContext($method, $data, $headers) {
        $options = array(
            'http' => array(
                'method' => $method,
                'ignore_errors' => true,  // 에러 응답도 내용 반환
                'timeout' => $this->timeout,
                'follow_location' => $this->followRedirects ? 1 : 0,
                'max_redirects' => $this->maxRedirects,
                'protocol_version' => 1.1,
            )
        );

        // 헤더 설정
        if (!empty($headers)) {
            $options['http']['header'] = $this->formatHeaders($headers);
        }

        // 데이터 설정
        if ($data !== null) {
            $options['http']['content'] = $data;
        }

        return stream_context_create($options);
    }

    /**
     * 헤더 배열을 문자열로 포맷팅
     *
     * @param array $headers 헤더 배열
     * @return string 포맷팅된 헤더 문자열
     */
    private function formatHeaders($headers) {
        $formatted = array();
        foreach ($headers as $name => $value) {
            $formatted[] = "$name: $value";
        }
        return implode("\r\n", $formatted);
    }

    /**
     * HTTP 요청 전송
     *
     * @param string $url 요청 URL
     * @param resource $context 스트림 컨텍스트
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    private function sendRequest($url, $context) {
        // 오류 핸들링을 위한 이전 설정 저장
        $previousErrorReporting = error_reporting();
        $previousDisplayErrors = ini_get('display_errors');

        // 오류 표시 비활성화 (warning 방지)
        error_reporting(0);
        ini_set('display_errors', 0);

        // 요청 수행 (에러 억제 연산자 @ 사용)
        $response = @file_get_contents($url, false, $context);
        $error = error_get_last();

        // 오류 발생 시 상세 정보 로깅
        if ($response === false) {
            error_log("Request failed: " . ($error ? $error['message'] : 'Unknown error'));
            error_log("URL: $url");
        }

        // 오류 설정 복원
        error_reporting($previousErrorReporting);
        ini_set('display_errors', $previousDisplayErrors);

        // 응답 헤더
        $responseHeaders = isset($http_response_header) ? $http_response_header : array();

        // 상태 코드 추출
        $statusCode = $this->getStatusCodeFromHeaders($responseHeaders);

        // 오류 처리
        if ($response === false) {
            $errorMessage = $error ? $error['message'] : 'Unknown error';
            $this->lastError = $errorMessage;

            // 요청 정보 저장
            $this->lastResponseInfo = array(
                'url' => $url,
                'http_code' => $statusCode,
                'error' => $errorMessage,
                'headers' => $responseHeaders
            );

            // 일반적인 연결 오류는 예외를 발생시키지 않고 상태 코드 0과 빈 응답을 반환
            return array(0, '', $responseHeaders);
        }

        // 요청 정보 저장
        $this->lastResponseInfo = array(
            'url' => $url,
            'http_code' => $statusCode,
            'response_size' => strlen($response),
            'headers' => $responseHeaders
        );

        return array($statusCode, $response, $responseHeaders);
    }

    /**
     * 응답 헤더에서 HTTP 상태 코드 추출
     *
     * @param array $headers HTTP 응답 헤더
     * @return int HTTP 상태 코드
     */
    private function getStatusCodeFromHeaders($headers) {
        if (empty($headers) || !isset($headers[0])) {
            return 0;
        }

        preg_match('|HTTP/\d\.\d\s+(\d+)|', $headers[0], $matches);
        return isset($matches[1]) ? (int)$matches[1] : 0;
    }

    /**
     * PUT 요청 수행
     *
     * @param string $url 요청 URL
     * @param mixed $data 전송할 데이터
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function put($url, $data, $headers = array()) {
        $context = $this->createContext('PUT', $data, $headers);
        return $this->sendRequest($url, $context);
    }

    /**
     * DELETE 요청 수행
     *
     * @param string $url 요청 URL
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function delete($url, $headers = array()) {
        $context = $this->createContext('DELETE', null, $headers);
        return $this->sendRequest($url, $context);
    }

    /**
     * JSON POST 요청 수행
     *
     * @param string $url 요청 URL
     * @param array $data JSON으로 변환할 데이터
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function postJson($url, $data, $headers = array()) {
        $headers['Content-Type'] = 'application/json';
        $jsonData = json_encode($data);
        return $this->post($url, $jsonData, $headers);
    }

    /**
     * POST 요청 수행
     *
     * @param string $url 요청 URL
     * @param mixed $data 전송할 데이터
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function post($url, $data, $headers = array()) {
        $context = $this->createContext('POST', $data, $headers);
        return $this->sendRequest($url, $context);
    }

    /**
     * 멀티파트 폼 데이터 POST 요청 (파일 업로드)
     *
     * @param string $url 요청 URL
     * @param array $fields 폼 필드 데이터
     * @param array $files 업로드할 파일 (name => filepath 형식)
     * @param array $headers 요청 헤더
     * @return array [응답 코드, 응답 내용, 응답 헤더]
     */
    public function postMultipart($url, $fields = array(), $files = array(), $headers = array()) {

        $boundary = '----WebKitFormBoundary' . bin2hex(custom_random_bytes(16));
        $data = $this->buildMultipartData($boundary, $fields, $files);

        $headers['Content-Type'] = 'multipart/form-data; boundary=' . $boundary;

        // 디버깅 (데이터 길이 확인)
        error_log("Multipart data length: " . strlen($data));
        // 헤더 확인
        error_log("Headers: " . print_r($headers, true));

        return $this->post($url, $data, $headers);
    }

    /**
     * 멀티파트 폼 데이터 생성
     *
     * @param string $boundary 경계 문자열
     * @param array $fields 폼 필드 데이터
     * @param array $files 파일 데이터
     * @return string 멀티파트 폼 데이터
     */
    private function buildMultipartData($boundary, $fields, $files) {
        $data = '';
        $eol = "\r\n";

        // 일반 필드 추가
        foreach ($fields as $name => $content) {
            $data .= "--$boundary$eol";
            $data .= "Content-Disposition: form-data; name=\"$name\"$eol$eol";
            $data .= "$content$eol";
        }

        // 파일 추가 부분
        foreach ($files as $name => $filePath) {
            if (!file_exists($filePath) || !is_readable($filePath)) {
                error_log("File not accessible: $filePath");
                continue;
            }

            $data .= "--$boundary$eol";
            $data .= "Content-Disposition: form-data; name=\"$name\"; filename=\"" . basename($filePath) . "\"$eol";
            $data .= "Content-Type: " . $this->getMimeType($filePath) . "$eol$eol";

            // 바이너리 모드로 파일 읽기
            $fileContent = file_get_contents($filePath);
            if ($fileContent === false) {
                error_log("Failed to read file: $filePath");
                continue;
            }

            $data .= $fileContent . "\r\n";
        }

        $data .= "--$boundary--\r\n";
        return $data;
    }

    /**
     * 파일의 MIME 타입 감지
     *
     * @param string $filePath 파일 경로
     * @return string MIME 타입
     */
    private function getMimeType($filePath) {
        // finfo_file을 사용할 수 있으면 사용 (PHP 5.3+)
        if (function_exists('finfo_file')) {
            $finfo = finfo_open(FILEINFO_MIME_TYPE);
            $mime = finfo_file($finfo, $filePath);
            finfo_close($finfo);
            return $mime;
        }

        // mime_content_type 함수가 있으면 사용 (일부 PHP 구성)
        if (function_exists('mime_content_type')) {
            return mime_content_type($filePath);
        }

        // 확장자로 판단 (최후의 수단)
        $extension = strtolower(pathinfo($filePath, PATHINFO_EXTENSION));
        $mimeTypes = array(
            'jpg' => 'image/jpeg',
            'jpeg' => 'image/jpeg',
            'png' => 'image/png',
            'gif' => 'image/gif',
            'pdf' => 'application/pdf',
            'txt' => 'text/plain',
            'html' => 'text/html',
            'csv' => 'text/csv',
            'json' => 'application/json',
            'xml' => 'application/xml',
            'zip' => 'application/zip',
            'doc' => 'application/msword',
            'docx' => 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
            'xls' => 'application/vnd.ms-excel',
            'xlsx' => 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        );

        return isset($mimeTypes[$extension]) ? $mimeTypes[$extension] : 'application/octet-stream';
    }

    /**
     * 마지막 요청 정보 가져오기
     *
     * @return array 마지막 요청 정보
     */
    public function getLastResponseInfo() {
        return $this->lastResponseInfo;
    }

    /**
     * 마지막 오류 메시지 가져오기
     *
     * @return string|null 마지막 오류 메시지
     */
    public function getLastError() {
        return $this->lastError;
    }

    /**
     * 응답 디코딩 (JSON)
     *
     * @param string $response JSON 응답
     * @param bool $assoc 연관 배열로 변환할지 여부
     * @return mixed 디코딩된 데이터
     */
    public function decodeJson($response, $assoc = true) {
        return json_decode($response, $assoc);
    }
}

/**
 * PHP 5.3.29용 random_bytes() 구현
 * PHP 7의 random_bytes()와 유사하게 동작하도록 설계
 *
 * @param int $length 생성할 바이트 수
 * @return string 지정된 길이의 무작위 바이트 문자열
 * @throws Exception 안전한 난수를 생성할 수 없는 경우
 */
function custom_random_bytes($length) {
    // 길이 검증
    if (!is_int($length)) {
        throw new Exception('Length must be an integer');
    }

    if ($length < 1) {
        throw new Exception('Length must be greater than 0');
    }

    // 1. /dev/urandom이 있는지 확인 (리눅스/유닉스 환경)
    $use_urandom = false;
    $output = '';

    if (@is_readable('/dev/urandom')) {
        $fp = @fopen('/dev/urandom', 'rb');
        if ($fp !== false) {
            $use_urandom = true;
        }
    }

    if ($use_urandom) {
        $remaining = $length;
        $buf = '';

        // /dev/urandom에서 바이트 읽기
        while ($remaining > 0) {
            $read = @fread($fp, $remaining);
            if ($read === false) {
                @fclose($fp);
                throw new Exception('Unable to read from /dev/urandom');
            }
            $buf .= $read;
            $remaining -= strlen($read);
        }
        @fclose($fp);
        $output = $buf;
    } else {
        // 2. 윈도우 환경이거나 /dev/urandom이 없는 경우 대체 방법 사용
        // mt_rand()를 기반으로 하지만 더 많은 엔트로피 추가

        // 시드 설정
        mt_srand(microtime(true) * 1000000 + memory_get_usage(true));

        $bytes = '';
        $state = session_id() . uniqid(mt_rand(), true) . microtime(true);
        $key = sha1(mt_rand() . microtime(true) . $state . uniqid('', true), true);
        $counter = 0;

        for ($i = 0; $i < $length; $i++) {
            // 여러 소스에서 엔트로피 수집
            $seed = mt_rand() . memory_get_usage() . getmypid() . $state . microtime(true) . $counter++;

            // sha1을 사용하여 의사 난수 바이트 생성
            $digest = sha1($key . $seed, true);
            $key = sha1($key . $digest . $counter, true);

            $bytes .= $digest[0];
        }

        $output = $bytes;
    }

    if (strlen($output) < $length) {
        throw new Exception('Unable to generate sufficient random data');
    }

    return substr($output, 0, $length);
}
