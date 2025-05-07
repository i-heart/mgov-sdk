package kr.i_heart.http;

import java.io.IOException;

/**
 * HTTP 요청 실행에 사용되는 유틸리티 클래스
 */
public class EntityUtils {
    /**
     * HttpEntity에서 문자열 추출 메서드
     * @param entity HTTP 엔티티
     * @param charset 문자 인코딩
     * @return 추출된 문자열
     * @throws IOException 입출력 예외
     */
    public static String toString(HttpEntity entity, java.nio.charset.Charset charset) throws IOException {
        return new String(entity.getContent(), charset);
    }
}
