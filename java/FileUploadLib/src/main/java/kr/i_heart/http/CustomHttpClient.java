package kr.i_heart.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 커스텀 HttpClient 클래스
 * Apache HttpClient의 의존성 없이 비슷한 인터페이스를 제공
 */
public class CustomHttpClient implements AutoCloseable {
    private int connectTimeout = 5000; // 기본 연결 타임아웃 5초
    private int readTimeout = 8000;    // 기본 읽기 타임아웃 8초

    /**
     * 타임아웃 설정을 위한 빌더 클래스
     */
    public static class Builder {
        private int connectTimeout = 5000;
        private int readTimeout = 8000;

        public Builder setConnectTimeout(int timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        public Builder setReadTimeout(int timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public CustomHttpClient build() {
            CustomHttpClient client = new CustomHttpClient();
            client.connectTimeout = this.connectTimeout;
            client.readTimeout = this.readTimeout;
            return client;
        }
    }

    /**
     * 빌더 생성 메서드
     */
    public static Builder custom() {
        return new Builder();
    }

    /**
     * HTTP 요청 실행 메서드
     * @param request 실행할 HTTP 요청
     * @return HTTP 응답 객체
     * @throws IOException 요청 처리 중 발생할 수 있는 입출력 예외
     */
    public HttpResponse execute(HttpRequest request) throws IOException {
        URL url = new URL(request.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 타임아웃 설정
        connection.setConnectTimeout(this.connectTimeout);
        connection.setReadTimeout(this.readTimeout);

        // 요청 메서드 설정
        connection.setRequestMethod(request.getMethod());

        // 헤더 설정
        for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        // 요청 바디가 있는 경우
        if (request.getEntity() != null) {
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(request.getEntity().getContent());
            }
        }

        // 응답 상태 코드와 본문 읽기
        int statusCode = connection.getResponseCode();
        String responseBody;

        try (InputStream in = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream() : connection.getErrorStream()) {
            responseBody = readInputStream(in);
        }

        // 헤더 정보 수집
        Map<String, String> responseHeaders = new HashMap<>();
        for (String key : connection.getHeaderFields().keySet()) {
            if (key != null) { // getHeaderFields()는 null 키를 포함할 수 있음
                responseHeaders.put(key, connection.getHeaderField(key));
            }
        }

        // 연결 종료
        connection.disconnect();

        // HTTP 응답 객체 생성 및 반환
        return new HttpResponse(statusCode, responseBody, responseHeaders);
    }

    /**
     * InputStream에서 문자열을 읽는 유틸리티 메서드
     */
    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    @Override
    public void close() {
        // 리소스 정리가 필요한 경우 여기에 구현
    }
}

