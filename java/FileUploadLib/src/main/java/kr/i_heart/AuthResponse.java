package kr.i_heart;

/**
 * 인증/로그인 응답을 위한 DTO 클래스
 */
public class AuthResponse {

    // 인증 결과 코드 (인증성공: 10000, 그외 실패코드)
    private String code;

    // 인증 결과 코드 설명 (인증성공: 성공, 그외 실패설명)
    private String message;

    private TokenData data;

    // 기본 생성자
    public AuthResponse() {
    }

    // Getter/Setter
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TokenData getData() {
        return data;
    }

    public void setData(TokenData data) {
        this.data = data;
    }

    /**
     * 인증 토큰 데이터를 담는 DTO 클래스
     */
    public static class TokenData {

        // 사용자 인증토큰
        private String token;

        // 토큰 재발급을 위한 토큰(사용하지 않음)
        private String refreshToken;

        // 기본 생성자
        public TokenData() {
        }

        // Getter/Setter
        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
