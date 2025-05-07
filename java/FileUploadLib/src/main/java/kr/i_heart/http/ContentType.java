package kr.i_heart.http;

/**
 * 콘텐츠 타입을 나타내는 클래스
 */
public class ContentType {
    private final String mimeType;

    /**
     * 생성자
     * @param mimeType MIME 타입
     */
    private ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * 지정된 MIME 타입으로 ContentType 생성
     * @param mimeType MIME 타입
     * @return ContentType 인스턴스
     */
    public static ContentType create(String mimeType) {
        return new ContentType(mimeType);
    }

    /**
     * 미리 정의된 APPLICATION_JSON 타입
     */
    public static final ContentType APPLICATION_JSON = create("application/json");

    /**
     * MIME 타입 반환
     * @return MIME 타입
     */
    public String getMimeType() {
        return mimeType;
    }
}
