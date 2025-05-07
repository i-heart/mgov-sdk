package kr.i_heart.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 멀티파트 폼 데이터 구성을 위한 빌더 클래스
 */
public class MultipartEntityBuilder {
    private final String boundary;
    private final List<Part> parts = new ArrayList<>();

    /**
     * 빌더 생성 팩토리 메서드
     */
    public static MultipartEntityBuilder create() {
        return new MultipartEntityBuilder();
    }

    /**
     * 생성자
     */
    private MultipartEntityBuilder() {
        // 고유한 경계 문자열 생성
        this.boundary = UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 텍스트 본문 파트 추가
     * @param name 파트 이름
     * @param value 텍스트 값
     * @return 빌더 인스턴스
     */
    public MultipartEntityBuilder addTextBody(String name, String value) {
        parts.add(new TextPart(name, value));
        return this;
    }

    /**
     * 파일 파트 추가
     * @param name 파트 이름
     * @param fileBody 파일 본문 객체
     * @return 빌더 인스턴스
     */
    public MultipartEntityBuilder addPart(String name, FileBody fileBody) {
        parts.add(new FilePart(name, fileBody));
        return this;
    }

    /**
     * 멀티파트 엔티티 빌드
     * @return 멀티파트 형식의 HttpEntity
     * @throws IOException 입출력 예외
     */
    public HttpEntity build() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 각 파트를 outputStream에 작성
        for (Part part : parts) {
            // 경계 작성
            outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));

            // 파트 헤더와 내용 작성
            part.writeTo(outputStream);

            // 개행 추가
            outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        // 최종 경계 작성
        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        // 멀티파트 형식의 HttpEntity 반환
        return new MultipartHttpEntity(outputStream.toByteArray(), boundary);
    }

    /**
     * 멀티파트 엔티티 클래스
     */
    private static class MultipartHttpEntity implements HttpEntity {
        private final byte[] content;
        private final String boundary;

        public MultipartHttpEntity(byte[] content, String boundary) {
            this.content = content;
            this.boundary = boundary;
        }

        @Override
        public byte[] getContent() {
            return content;
        }

        @Override
        public String getContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }
    }

    /**
     * 멀티파트 요청의 한 파트를 나타내는 인터페이스
     */
    private interface Part {
        void writeTo(ByteArrayOutputStream outputStream) throws IOException;
    }

    /**
     * 텍스트 파트 구현
     */
    private static class TextPart implements Part {
        private final String name;
        private final String value;

        public TextPart(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void writeTo(ByteArrayOutputStream outputStream) throws IOException {
            // Content-Disposition 헤더
            String header = String.format("Content-Disposition: form-data; name=\"%s\"\r\n", name);
            outputStream.write(header.getBytes(StandardCharsets.UTF_8));

            // Content-Type 헤더 추가
            outputStream.write("Content-Type: text/plain; charset=UTF-8\r\n\r\n".getBytes(StandardCharsets.UTF_8));

            // 텍스트 값
            outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 파일 파트 구현
     */
    private static class FilePart implements Part {
        private final String name;
        private final FileBody fileBody;

        public FilePart(String name, FileBody fileBody) {
            this.name = name;
            this.fileBody = fileBody;
        }

        @Override
        public void writeTo(ByteArrayOutputStream outputStream) throws IOException {
            // Content-Disposition 헤더 (파일명 포함)
            String header = String.format(
                "Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"\r\n",
                name, fileBody.getFilename());
            outputStream.write(header.getBytes(StandardCharsets.UTF_8));

            // Content-Type 헤더
            String contentTypeHeader = "Content-Type: " + fileBody.getContentType().getMimeType() + "\r\n\r\n";
            outputStream.write(contentTypeHeader.getBytes(StandardCharsets.UTF_8));

            // 파일 내용
            outputStream.write(fileBody.getContent());
        }
    }
}
