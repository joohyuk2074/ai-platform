package me.joohyuk.datahub.domain.entity;

import com.spartaecommerce.domain.vo.UserId;
import lombok.Getter;

/**
 * User 서비스의 사용자 정보를 Datahub 컨텍스트에서 표현하는 엔티티
 * 문서 소유자 및 작업 주체로서의 최소한의 정보만 유지
 */
@Getter
public class User {

  private final UserId userId;

  /**
   * 사용자 계정명 (표시용)
   */
  private final String username;

  private User(UserId userId, String username) {
    if (userId == null) {
      throw new IllegalArgumentException("UserId cannot be null");
    }
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    this.userId = userId;
    this.username = username;
  }

  /**
   * User 서비스로부터 받은 정보로 User 생성
   */
  public static User from(Long userId, String username) {
    return new User(new UserId(userId), username);
  }
}
