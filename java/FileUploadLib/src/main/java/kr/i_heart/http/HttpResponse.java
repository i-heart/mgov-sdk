package kr.i_heart.http;

import java.util.Map;

/**
 * HTTP 응답을 나타내는 클래스
 */
public class HttpResponse implements AutoCloseable {
    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    /**
     * 생성자
     * @param statusCode HTTP 상태 코드
     * @param body 응답 본문
     * @param headers 응답 헤더
     */
    public HttpResponse(int statusCode, String body, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers;
    }

    /**
     * 상태 코드 반환 메서드
     * @return HTTP 상태 코드
     */
    public int getCode() {
        return statusCode;
    }

    /**
     * 응답 엔티티 반환 메서드
     * @return HTTP 응답 엔티티
     */
    public HttpEntity getEntity() {
        return new StringEntity(body, "application/json");
    }

    /**
     * 헤더 반환 메서드
     * @return HTTP 응답 헤더 맵
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void close() {
        // 리소스 정리가 필요한 경우 여기에 구현
    }
}
