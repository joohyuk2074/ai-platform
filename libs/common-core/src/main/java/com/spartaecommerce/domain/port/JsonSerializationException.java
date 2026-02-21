package com.spartaecommerce.domain.port;

/**
 * JSON 직렬화/역직렬화 실패 시 발생하는 예외
 * <p>
 * Unchecked Exception으로 설계하여 호출자가 명시적으로 처리하지 않아도 되도록 합니다.
 */
public class JsonSerializationException extends RuntimeException {

  public JsonSerializationException(String message) {
    super(message);
  }

  public JsonSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
