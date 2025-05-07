package kr.i_heart.http;

/**
 * HTTP 엔티티(요청/응답 본문)를 나타내는 클래스
 */
public interface HttpEntity {
    /**
     * 엔티티 내용을 바이트 배열로 반환
     * @return 엔티티 내용
     */
    byte[] getContent();

    /**
     * 엔티티의 컨텐츠 타입 반환
     * @return 컨텐츠 타입
     */
    String getContentType();
}
