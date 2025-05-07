package kr.i_heart.http;

import java.nio.charset.StandardCharsets;
import kr.i_heart.http.HttpEntity;

/**
 * 문자열 기반 HTTP 엔티티 구현
 */
public class StringEntity implements HttpEntity {
    private final byte[] content;
    private final String contentType;

    /**
     * 생성자
     * @param content 문자열 내용
     * @param contentType 컨텐츠 타입
     */
    public StringEntity(String content, String contentType) {
        this.content = content.getBytes(StandardCharsets.UTF_8);
        this.contentType = contentType;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
