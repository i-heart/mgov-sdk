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

require_once 'FileUploadClient.php';

$uploadClient = new FileUploadClient();
// 클라이언트 초기화

try {
    // 파일 업로드 요청
    $result = $uploadClient->handleFileUpload(
        'https://api.example.com',    // API 도메인
        'your-client-id',            // 클라이언트 ID
        'your-client-password',      // 클라이언트 비밀번호
        '',                          // 브랜드 ID (선택적)
        '/path/to/your/file.jpg'     // 업로드할 파일 경로
    );

    // 성공 결과 출력
    print_r($result);
} catch (Exception $e) {
    // 오류 처리
    echo "Error: " . $e->getMessage() . "\n";
}
