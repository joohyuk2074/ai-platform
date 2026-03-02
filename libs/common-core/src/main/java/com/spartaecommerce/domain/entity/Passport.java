package com.spartaecommerce.domain.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spartaecommerce.domain.vo.UserId;

import java.util.Collections;
import java.util.List;

/**
 * API Gateway에서 전달되는 인증된 사용자 정보
 * Netflix Passport 패턴: JSON 직렬화하여 X-Passport 헤더로 전달
 */
public record Passport(
    UserId userId,
    String username,
    List<String> roles
) {

  /**
   * Jackson 역직렬화를 위한 생성자
   * API Gateway에서 JSON으로 전달된 값을 파싱할 때 사용
   */
  @JsonCreator
  public Passport(
      @JsonProperty("userId") Long userIdValue,
      @JsonProperty("username") String username,
      @JsonProperty("roles") List<String> roles
  ) {
    this(
        new UserId(userIdValue),
        username,
        roles != null ? roles : Collections.emptyList()
    );
  }

  /**
   * 기본 생성자
   */
  public Passport(UserId userId, String username, List<String> roles) {
    this.userId = userId;
    this.username = username;
    this.roles = roles != null ? List.copyOf(roles) : Collections.emptyList();
  }

  /**
   * roles 없이 생성하는 편의 생성자
   */
  public Passport(UserId userId, String username) {
    this(userId, username, Collections.emptyList());
  }

  /**
   * 특정 역할을 가지고 있는지 확인
   */
  public boolean hasRole(String role) {
    return roles.contains(role);
  }

  /**
   * ADMIN 역할을 가지고 있는지 확인
   */
  public boolean isAdmin() {
    return hasRole("ADMIN");
  }

  /**
   * JSON 직렬화를 위한 userId 값 반환
   */
  @JsonProperty("userId")
  public Long getUserIdValue() {
    return userId.getValue();
  }
}
