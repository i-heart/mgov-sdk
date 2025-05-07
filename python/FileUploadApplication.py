"""
MGOV RCS File Upload Library
Version: 1.0.0

This file is part of the i-heart library.

Author   : 정의진(jej8076@i-heart.co.kr), 박민호(minoflower@i-heart.co.kr)
Date     : 2025-04-29
License  : MIT License
Website  : https://i-heart.co.kr

Copyright (c) 2025 i-heart. All rights reserved.
"""

# -*- coding: utf-8 -*-

# FileUploadClient 모듈 import
from FileUploadClient import FileUploadClient


def main():
  # FileUploadClient 인스턴스 생성
  uploader = FileUploadClient()

  # 필요한 파라미터 설정
  domain = "https://api.example.com"
  client_id = "your-client-id"
  client_pwd = "your-client-password"
  brand_id = ""  # 선택적 파라미터, 공백 가능
  file_path = "/path/to/your/file.jpg"

  # 파일 업로드 수행
  try:
    print("파일 업로드 시작: {}".format(file_path))
    result = uploader.handle_file_upload(domain, client_id, client_pwd,
                                         brand_id, file_path)

    # 성공 결과 처리
    print("업로드 성공!")
    print("응답 코드: {}".format(result.get('code')))

    # 응답 데이터 출력
    if 'data' in result:
      data = result.get('data')
      if isinstance(data, dict):
        print("업로드 결과 데이터:")
        for key, value in data.items():
          print("  {}: {}".format(key, value))

  except Exception as e:
    # 오류 처리
    print("업로드 오류 발생: {}".format(str(e)))

    # 필요한 경우 추가 오류 디버깅 정보 출력
    import traceback
    print("상세 오류 정보:")
    traceback.print_exc()


# 직접 실행 시 main 함수 호출
if __name__ == "__main__":
  main()
