package com.spartaecommerce.api.response;

import com.spartaecommerce.exception.ErrorCode;

public class CommonResponse<T> {

  private final String code;

  private final String message;

  private final T data;

  private CommonResponse(String code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static CommonResponse<IdResponse> create(Long id) {
    return new CommonResponse<>("CREATED", "Created successfully", new IdResponse(id));
  }

  public static <T> CommonResponse<T> success(T data) {
    return new CommonResponse<>("OK", "Success", data);
  }

  public static <T> CommonResponse<T> success(String message, T data) {
    return new CommonResponse<>("OK", message, data);
  }

  public static <T> CommonResponse<T> error(String errorCode, String message) {
    return new CommonResponse<>(errorCode, message, null);
  }

  public static <T> CommonResponse<T> error(ErrorCode errorCode, String message) {
    return new CommonResponse<>(errorCode.getCode(), message, null);
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }
}