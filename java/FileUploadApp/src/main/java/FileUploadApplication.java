import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.Scanner;
import kr.i_heart.FileUploadClient;
import kr.i_heart.FileUploadResponse;

public class FileUploadApplication {

  private final Map<String, String> tokenStorage; // In-memory storage

  public FileUploadApplication() {
    this.tokenStorage = new HashMap<>();
  }

  public static void main(String[] args) {

    FileUploadClient nirsFileUploadClient = new FileUploadClient();

    // setup.sh 설정으로 실행
    if (args.length > 0) {
      if (args.length < 5) {
        System.out.println("인자가 부족합니다. 다음 형식으로 실행하세요:");
        System.out.println("java (options) FileUploadApplication <domain> <clientId> <clientPwd> <brandId> <file>");
        return;
      }

      System.out.println("\nsetup.conf 파일에 입력한 정보로 파일 업로드를 시도합니다");

      String arg1 = args[0]; // domain
      String arg2 = args[1]; // clientId
      String arg3 = args[2]; // clientPwd
      String arg4 = args[3]; // brandId
      String arg5 = args[4]; // filePath

      System.out.println("domain: " + arg1);
      System.out.println("clientId: " + arg2);
      System.out.println("clientPwd: " + arg3);
      System.out.println("brandId: " + arg4);
      System.out.println("file: " + arg5);

      try{
        File setupfile = new File(arg5);

        // 파일 존재 여부 확인
        if (!setupfile.exists()) {
          System.err.println("파일이 존재하지 않습니다: " + arg5);
          return;
        }

        FileUploadResponse result = nirsFileUploadClient.handleFileUpload(arg1, arg2, arg3, arg4, setupfile);
        System.out.println("업로드 성공: " + result);
      }catch (Exception e){
        System.err.println("업로드 실패: " + e.getMessage());
        e.printStackTrace();
      }
      return;
    }

    Scanner scanner = new Scanner(System.in);

    try {

      // 대화형
      System.out.print("API 호출 도메인 정보를 입력하세요 (예: https://nirs-rcs.i-heart.kr): ");
      String domain = scanner.nextLine().trim();

      if (domain.isEmpty()){
        System.err.println("도메인은 필수 값입니다.");
        scanner.close();
        return;
      }

      System.out.print("Client ID를 입력하세요: ");
      String clientId = scanner.nextLine().trim();

      if (clientId.isEmpty()){
        System.err.println("clientId는 필수 값입니다.");
        scanner.close();
        return;
      }

      System.out.print("Client Password를 입력하세요: ");
      String clientPwd = scanner.nextLine().trim();

      if (clientPwd.isEmpty()){
        System.err.println("clientPwd는 필수 값입니다.");
        scanner.close();
        return;
      }

      System.out.print("Brand ID를 입력하세요 (선택사항, 공백 가능): ");
      String brandId = scanner.nextLine().trim();

      System.out.print("업로드할 파일 경로를 입력하세요: ");
      String filePath = scanner.nextLine().trim();

      File file = new File(filePath);

      // 파일 존재 여부 확인
      if (!file.exists()) {
        System.err.println("파일이 존재하지 않습니다: " + filePath);
        scanner.close();
        return;
      }

      System.out.println("\n입력한 정보로 파일 업로드를 시작합니다...");
      FileUploadResponse result = nirsFileUploadClient.handleFileUpload(domain, clientId, clientPwd, brandId, file);
      System.out.println("업로드 성공: " + result.toString());

    } catch (Exception e) {
      System.err.println("업로드 실패: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }
}
