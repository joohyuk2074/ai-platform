package me.joohyuk.datarex.domain.exception;

import com.spartaecommerce.exception.DomainErrorCode;

public enum DatarexErrorCode implements DomainErrorCode {

  // 유효성 에러 (400)
  INVALID_FILE_KEY("INVALID_FILE_KEY"),

  // 파일 에러 (404)
  FILE_NOT_FOUND("FILE_NOT_FOUND"),

  // 스토리지 에러 (422)
  FILE_STORAGE_FAILED("FILE_STORAGE_FAILED"),

  // 청킹 에러 (422)
  CHUNKING_FAILED("CHUNKING_FAILED"),

  // 직렬화 에러 (500)
  SERIALIZATION_FAILED("SERIALIZATION_FAILED"),

  // 알 수 없는 에러 (500)
  UNKNOWN_ERROR("UNKNOWN_ERROR"),
  ;

  private final String code;

  DatarexErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String code() {
    return code;
  }
}
