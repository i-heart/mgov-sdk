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

import json
import base64
import mimetypes
import os
import sys
from threading import Lock

if (sys.version_info[0] < 3):  # Python 2.x
  import sys
  from urlparse import urlparse
  import httplib as httpClient

  reload(sys)
  sys.setdefaultencoding('utf8')
else:
  import http.client as httpClient
  from urllib.parse import urlparse


class FileUploadClient:
  """
  파일 업로드 클라이언트
  인증 토큰을 관리하고 파일 업로드를 처리합니다.
  """

  REQUEST_SUCCESS_CODE = "10000"  # API 비즈니스 로직 처리 성공에 대한 응답 코드
  TOKEN_INVALID_CODE = "29011"  # 토큰 유효성 검사 실패에 대한 응답 코드
  TOKEN_KEY_PREFIX = "token_"

  def __init__(self):
    self._token_storage = {}
    self._lock = Lock()  # 스레드 안전성을 위한 락

  def handle_file_upload(self, domain, client_id, client_pwd, brand_id,
      file_path):
    """
    인증 요청을 포함한 파일 업로드 요청 함수

    Args:
        domain: API 도메인 URL
        client_id: 클라이언트 ID
        client_pwd: 클라이언트 비밀번호
        brand_id: 브랜드 ID (선택적 파라미터)
        file_path: 업로드할 파일 경로

    Returns:
        업로드 결과를 담은 딕셔너리 객체

    Raises:
        Exception: 인증 또는 업로드 과정에서 발생한 예외
    """
    token = self.get_token(client_id, client_pwd)

    # 1. 토큰이 없는 경우: 인증 필요
    if token is None:
      try:
        auth_result = self.request_auth(domain, client_id, client_pwd)
        token = auth_result.get("token")
        self.store_token(client_id, client_pwd, token)
      except Exception as auth_error:
        error_info = self.parse_error(auth_error)
        raise RuntimeError(
            "Authentication failed: {}".format(error_info.get('message')))

    # 2. 토큰으로 업로드 시도
    try:
      return self.upload_file(domain, token, file_path, brand_id)
    except Exception as e:
      error_obj = self.parse_error(e)

      # 3. 토큰 유효성 검사 실패의 경우 한 번만 재시도
      if self.TOKEN_INVALID_CODE == error_obj.get("code"):
        try:
          # 재인증
          auth_result = self.request_auth(domain, client_id, client_pwd)
          token = auth_result.get("token")
          self.store_token(client_id, client_pwd, token)

          # 새 토큰으로 업로드 재시도
          return self.upload_file(domain, token, file_path, brand_id)
        except Exception as retry_error:
          error_info = self.parse_error(retry_error)
          raise RuntimeError(
              "Retry failed: {}".format(error_info.get('message')))

      # 4. 다른 오류인 경우 실패 처리
      raise e

  def request_auth(self, domain, client_id, client_pwd):
    """
    인증 요청을 수행하는 메서드
    """
    url_parts = urlparse(domain)
    hostname = url_parts.netloc
    is_https = url_parts.scheme == 'https'

    if is_https:
      conn = httpClient.HTTPSConnection(hostname, timeout=8)
    else:
      conn = httpClient.HTTPConnection(hostname, timeout=8)

    headers = {"Content-type": "application/json"}
    request_body = {
      "clientId": client_id,
      "clientPwd": client_pwd
    }

    try:
      conn.request("POST", "/api/v1/auth", json.dumps(request_body), headers)
      response = conn.getresponse()
      status_code = response.status
      response_body = response.read().decode('utf-8')

      if status_code != 200:
        raise RuntimeError("HTTP error code: {}".format(status_code))

      response_data = json.loads(response_body)

      if self.REQUEST_SUCCESS_CODE != response_data.get("code"):
        raise RuntimeError(response_body)

      return response_data.get("data", {})
    finally:
      conn.close()

  def upload_file(self, domain, token, file_path, brand_id=None):
    """
    파일 업로드를 수행하는 메서드
    """
    url_parts = urlparse(domain)
    hostname = url_parts.netloc
    is_https = url_parts.scheme == 'https'

    if is_https:
      conn = httpClient.HTTPSConnection(hostname, timeout=15)
    else:
      conn = httpClient.HTTPConnection(hostname, timeout=15)

    # 경계선(boundary) 생성
    boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW"

    # 요청 헤더 설정
    headers = {
      "Content-Type": "multipart/form-data; boundary={}".format(boundary),
      "Authorization": "Bearer {}".format(token)
    }

    # JSON 요청 데이터 생성
    req_file_json = {"brandId": brand_id if brand_id else ""}
    req_file_content = json.dumps(req_file_json)

    # 파일 정보 준비
    file_name = os.path.basename(file_path)
    mime_type = mimetypes.guess_type(file_path)[0]
    if not mime_type:
      mime_type = "application/octet-stream"  # fallback MIME type

    # 멀티파트 요청 본문 구성
    body_parts = []

    # reqFile 추가
    body_parts.append("--{}".format(boundary))
    body_parts.append('Content-Disposition: form-data; name="reqFile"')
    body_parts.append('')
    body_parts.append(req_file_content)

    # 파일 추가
    body_parts.append("--{}".format(boundary))
    body_parts.append(
        'Content-Disposition: form-data; name="filePart"; filename="{}"'.format(
            file_name))
    body_parts.append('Content-Type: {}'.format(mime_type))
    body_parts.append('')

    # 본문 텍스트 부분 결합
    body_text = '\r\n'.join(body_parts) + '\r\n'
    body_bytes = body_text.encode('utf-8')

    # 파일 바이너리 데이터 읽기
    with open(file_path, 'rb') as f:
      file_data = f.read()

    # 종료 경계선 추가
    end_boundary = "\r\n--{}--\r\n".format(boundary).encode('utf-8')

    # 최종 요청 본문 구성
    body = body_bytes + file_data + end_boundary

    try:
      conn.request("POST", "/api/v1/upload", body, headers)
      response = conn.getresponse()
      status_code = response.status
      response_body = response.read().decode('utf-8')

      if status_code != 200:
        raise RuntimeError("HTTP error code: {}".format(status_code))

      response_data = json.loads(response_body)

      if self.REQUEST_SUCCESS_CODE != response_data.get("code"):
        raise RuntimeError(response_body)

      return response_data
    finally:
      conn.close()

  def generate_key(self, client_id, client_pwd):
    """
    클라이언트 ID와 비밀번호로부터 토큰 저장소 키를 생성하는 메서드
    """
    combined = "{}:{}".format(client_id, client_pwd)
    encoded = base64.b64encode(combined.encode('utf-8')).decode('utf-8')
    return encoded.replace("=", "_")

  def store_token(self, client_id, client_pwd, token):
    """
    토큰을 저장하는 메서드
    """
    key = self.generate_key(client_id, client_pwd)
    with self._lock:
      self._token_storage[self.TOKEN_KEY_PREFIX + key] = token

  def get_token(self, client_id, client_pwd):
    """
    저장된 토큰을 가져오는 메서드
    """
    key = self.generate_key(client_id, client_pwd)
    with self._lock:
      return self._token_storage.get(self.TOKEN_KEY_PREFIX + key)

  def parse_error(self, error):
    """
    예외로부터 오류 정보를 파싱하는 메서드
    """
    error_map = {}

    if error is None:
      error_map["message"] = "Unknown error"
      return error_map

    try:
      error_message = str(error)
      if error_message.startswith("{"):
        return json.loads(error_message)
    except Exception:
      pass  # JSON 파싱 실패 시

    error_map["message"] = str(error) if error else "Error parsing error"
    return error_map


# 사용 예시
if __name__ == "__main__":
  client = FileUploadClient()

  try:
    # 파라미터 설정
    domain = "http://example.com"
    client_id = "your_client_id"
    client_pwd = "your_client_password"
    brand_id = "optional_brand_id"  # 선택적 파라미터
    file_path = "path/to/your/file.txt"

    # 파일 업로드 실행
    result = client.handle_file_upload(domain, client_id, client_pwd, brand_id,
                                       file_path)
    print("Upload success:", result)
  except Exception as e:
    print("Error:", str(e))
