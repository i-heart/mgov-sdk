package kr.i_heart;

import java.util.List;

/**
 * 파일 업로드 응답을 위한 DTO 클래스
 */
public class FileUploadResponse {

    // 이미지 사전 등록 요청 결과 코드(성공: 10000, 그외 실패)
    private String code;

    // 이미지 사전 등록 요청 결과 코드 설명
    private String message;

    // 이미지 사전 등록 요청 결과 데이터 목록
    private FileData data;

    // 기본 생성자
    public FileUploadResponse() {
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

    public FileData getData() {
        return data;
    }

    public void setData(FileData data) {
        this.data = data;
    }

    /**
     * 업로드된 파일 데이터를 담는 DTO 클래스
     */
    public static class FileData {
        private String ch;
        private String imgUrl;
        private List<String> imgUrlLst;

        // 파일 ID
        private String fileId;

        //파일 만료일시
        private String fileExpDt;

        // 기본 생성자
        public FileData() {
        }

        // Getter/Setter
        public String getCh() {
            return ch;
        }

        public void setCh(String ch) {
            this.ch = ch;
        }

        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public List<String> getImgUrlLst() {
            return imgUrlLst;
        }

        public void setImgUrlLst(List<String> imgUrlLst) {
            this.imgUrlLst = imgUrlLst;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public String getFileExpDt() {
            return fileExpDt;
        }

        public void setFileExpDt(String fileExpDt) {
            this.fileExpDt = fileExpDt;
        }

        @Override
        public String toString() {
            return "FileData{" +
                "ch='" + ch + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", imgUrlLst=" + imgUrlLst +
                ", fileId='" + fileId + '\'' +
                ", fileExpDt='" + fileExpDt + '\'' +
                '}';
        }
    }

    @Override
    public String toString() {
        return "FileUploadResponse{" +
            "code='" + code + '\'' +
            ", message='" + message + '\'' +
            ", data=" + (data != null ? data.toString() : "null") +
            '}';
    }
}
