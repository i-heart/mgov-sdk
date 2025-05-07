package kr.i_heart.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 파일 본문을 나타내는 클래스
 */
public class FileBody {
    private final File file;
    private final ContentType contentType;
    private final String filename;

    /**
     * 생성자
     * @param file 업로드할 파일
     * @param contentType 파일의 콘텐츠 타입
     * @param filename 파일명
     */
    public FileBody(File file, ContentType contentType, String filename) {
        this.file = file;
        this.contentType = contentType;
        this.filename = filename;
    }

    /**
     * 파일 내용을 바이트 배열로 반환
     * @return 파일 내용
     * @throws IOException 입출력 예외
     */
    public byte[] getContent() throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();
        }
    }

    /**
     * 파일의 콘텐츠 타입 반환
     * @return 콘텐츠 타입
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * 파일명 반환
     * @return 파일명
     */
    public String getFilename() {
        return filename;
    }
}
