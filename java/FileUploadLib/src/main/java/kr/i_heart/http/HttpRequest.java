package kr.i_heart.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 요청을 나타내는 클래스
 */
public class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private HttpEntity entity;

    /**
     * HTTP POST 요청 구현
     */
    public static class HttpPost extends HttpRequest {
        public HttpPost(String url) {
            super(url, "POST");
        }
    }

    /**
     * HTTP GET 요청 구현
     */
    public static class HttpGet extends HttpRequest {
        public HttpGet(String url) {
            super(url, "GET");
        }
    }

    /**
     * 생성자
     * @param url 요청 URL
     * @param method HTTP 메서드
     */
    protected HttpRequest(String url, String method) {
        this.url = url;
        this.method = method;
        this.headers = new HashMap<>();
    }

    /**
     * 헤더 설정 메서드
     * @param name 헤더 이름
     * @param value 헤더 값
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    /**
     * 엔티티(요청 본문) 설정 메서드
     * @param entity HTTP 요청 본문 엔티티
     */
    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public HttpEntity getEntity() {
        return entity;
    }
}
