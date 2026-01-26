package com.spartaecommerce.exception;


public enum ErrorCode {

  // common
  INVALID_REQUEST(400, "INVALID_REQUEST", "The bad request."),
  INVALID_INPUT_VALUE(400, "INVALID_INPUT_VALUE", "Invalid input value."),
  UNAUTHORIZED(401, "UNAUTHORIZED", "Authentication is required."),
  INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "Internal server error."),

  // jpa
  ENTITY_NOT_FOUND(404, "ENTITY_NOT_FOUND", "The requested entity does not exist."),
  ENTITY_ALREADY_EXISTS(409, "ENTITY_ALREADY_EXISTS", "The entity already exists."),

  // order
  ORDER_INVALID_STATE_TRANSITION(400, "ORDER_INVALID_STATE_TRANSITION",
      "The order status cannot be changed to the requested status."),

  // refund
  REFUND_INVALID_STATE_TRANSITION(400, "REFUND_INVALID_STATE_TRANSITION",
      "The refund status cannot be changed to the requested status."),

  // coupon
  COUPON_NOT_FOUND(404, "COUPON_NOT_FOUND", "Coupon not found."),
  INVALID_COUPON_STATUS(400, "INVALID_COUPON_STATUS", "Invalid coupon status."),
  COUPON_ALREADY_ISSUED(409, "COUPON_ALREADY_ISSUED",
      "This coupon has already been issued to another user."),

  // lock
  LOCK_ACQUISITION_FAILED(409, "LOCK_ACQUISITION_FAILED", "다른 요청 처리 중입니다"),

  // external api
  EXTERNAL_API_ERROR(500, "EXTERNAL_API_ERROR", "External API call failed."),

  // message queue
  MESSAGE_CONSUME_ERROR(500, "MESSAGE_CONSUME_ERROR", "Message consume failed.");

  private final int httpStatus;
  private final String code;
  private final String message;

  ErrorCode(int httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.message = message;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
